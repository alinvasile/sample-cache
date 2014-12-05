package ro.alinvasile.projects.cache.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.alinvasile.projects.cache.internal.CacheEntry;
import ro.alinvasile.projects.cache.internal.CacheStorage;

public class SimpleFileDiskStoreImpl<K,V> implements CacheStorage<K,V>  {

    private static final int MAGIC_VALUE_HEADER = 0x44;

    private static final int MAGIC_MAJOR_VERSION = 0;

    private static final int MAGIC_MINOR_VERSION = 0;

    private static final int MAGIC_INCREMENT_VERSION = 1;

    private static final byte RECORD_VALID = 0;

    private static final byte RECORD_DELETED = 1;

    // Reserve 1 kb for file header area
    private final int FILE_HEADER_SIZE = 1024;

    // this is the actual database file
    private RandomAccessFile file;

    private Map<CacheEntry<K,V>,Long> entries = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(SimpleFileDiskStoreImpl.class);

    public SimpleFileDiskStoreImpl(String databaseFile) throws IOException, InvalidDatabaseFileException,
            ClassNotFoundException {
        File f = new File(databaseFile);

        boolean newFile = false;

        if (!f.exists()) {
            logger.info("Database file {} does not exist, creating new", databaseFile);

            newFile = true;

            createNewDatabaseFile(f);
        }

        file = new RandomAccessFile(f, "rw");

        if (newFile) {
            writeFileHeader();
        }

        validateDatabaseFile();

        logger.debug("Database {} is valid", databaseFile);

        readAllEntries();

    }

    @Override
    public void close() {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                logger.info("Exception closing storage",e);
            }
        }
    }

    private void createNewDatabaseFile(File f) throws IOException {
        boolean newFileCreated = f.createNewFile();

        if (!newFileCreated) {
            logger.error("Unable to create database file, aborting");
            throw new IllegalStateException();
        }
    }

    private void writeFileHeader() throws IOException {
        file.seek(0);

        file.writeInt(MAGIC_VALUE_HEADER);
        file.writeInt(MAGIC_MAJOR_VERSION);
        file.writeInt(MAGIC_MINOR_VERSION);
        file.writeInt(MAGIC_INCREMENT_VERSION);

        while (file.getFilePointer() < FILE_HEADER_SIZE) {
            file.writeByte(5);
        }

    }

    private void validateDatabaseFile() throws IOException, InvalidDatabaseFileException {
        file.seek(0);
        int headerValue = file.readInt();
        if (headerValue != MAGIC_VALUE_HEADER) {
            logger.debug("Unexpected header value {} ", headerValue);
            throw new InvalidDatabaseFileException("Invalid database file!");
        }

        int majorVersion = file.readInt();
        if (majorVersion != MAGIC_MAJOR_VERSION) {
            logger.debug("Unexpected major version value {} ", majorVersion);
            throw new InvalidDatabaseFileException("Invalid database file!");
        }

        int minorVersion = file.readInt();
        if (minorVersion != MAGIC_MINOR_VERSION) {
            logger.debug("Unexpected minor version value {} ", minorVersion);
            throw new InvalidDatabaseFileException("Invalid database file!");
        }

        int incrementalVersion = file.readInt();
        if (incrementalVersion != MAGIC_INCREMENT_VERSION) {
            logger.debug("Unexpected incremental version value {} ", incrementalVersion);
            throw new InvalidDatabaseFileException("Invalid database file!");
        }
    }

    public synchronized CacheEntry<K,V>[] getEntries() {
        return entries.keySet().toArray(new CacheEntry[] {});
    }

    private synchronized void readAllEntries() throws IOException, ClassNotFoundException {
        file.seek(FILE_HEADER_SIZE);
        
        long filePointer = -1;

        logger.debug("Positions {} {}", file.getFilePointer(), file.length());

        if (file.getFilePointer() < file.length()) {

            while (file.getFilePointer() < file.length()) {
                filePointer = file.getFilePointer();
                
                byte valid = file.readByte();
                
                logger.debug("Record is valid {} ", valid);
                
                CacheEntry x = readCacheEntryFromDatabase();    
                
                if (valid == RECORD_VALID) {
                    entries.put(x,filePointer);
                } 
            }

            // do not close streams as they will close the RandomAccessFile
            // leave them to be GC
        }

    }

    private CacheEntry<K,V> readCacheEntryFromDatabase() throws IOException, ClassNotFoundException {
        logger.debug("Read object from database");
        
        int size = file.readInt();
        
        logger.debug("Object size {} ",size);
        byte[] buff = new byte[size];
        
        file.readFully(buff); 
        
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buff));  
        CacheEntry<K,V> x = (CacheEntry<K,V>) ois.readObject();
        return x;
    }

    public synchronized void writeEntry(CacheEntry<K,V> entry) throws IOException {
        file.seek(file.length());
        
        long pointer = file.getFilePointer();
        
        file.writeByte(RECORD_VALID);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        ObjectOutputStream oos = new ObjectOutputStream(bos);  
        oos.writeObject(entry);  
        byte[] buffer = bos.toByteArray();  
        
        file.writeInt(buffer.length);
        file.write(buffer);

        entries.put(entry,pointer);
    }

    public synchronized void deleteEntry(CacheEntry entry) throws IOException, ClassNotFoundException {
        long position = entries.get(entry);
        
        file.seek(position);
        file.writeByte(RECORD_DELETED);
        
        entries.remove(entry);
        
        /*file.seek(FILE_HEADER_SIZE);
        long lastFilePointer = FILE_HEADER_SIZE;
        while (file.getFilePointer() < file.length()) {
            lastFilePointer = file.getFilePointer();
            byte deleted = file.readByte();
            if (deleted == RECORD_VALID) {
                CacheEntry x = readCacheEntryFromDatabase();  
                if (x.equals(entry)) {
                    file.seek(lastFilePointer);
                    file.writeByte(RECORD_DELETED);

                    entries.remove(entry);

                    break;
                }
            }
        }*/
    }
    
    @Override
    public synchronized boolean containsKey(K key){
        for(CacheEntry entry : entries.keySet()) {
            if(entry.getKey().equals(key)){
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public synchronized CacheEntry<K,V> get(K key){
        for(CacheEntry entry : entries.keySet()) {
            if(entry.getKey().equals(key)){
                return entry;
            }
        }
        
        return null;
    }

    @Override
    public void put(CacheEntry entry) {
        try {
            writeEntry(entry);
        } catch (IOException e) {
            logger.error("unable to put in cache",e);
            throw new IllegalStateException(e);
        }
        
    }


    @Override
    public CacheEntry[] sampleForEviction() {
       throw new UnsupportedOperationException();
    }

    @Override
    public synchronized int getSize() {
       return entries.size();
    }

    @Override
    public void remove(CacheEntry<K,V> key) {
        try {
            deleteEntry(key);
        } catch (ClassNotFoundException | IOException e) {
            logger.error("unable to remove from cache",e);
            throw new IllegalStateException(e);
        }
        
    }

}
