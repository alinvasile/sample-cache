package ro.alinvasile.projects.cache;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.alinvasile.projects.cache.internal.CacheEntry;
import ro.alinvasile.projects.cache.internal.CacheStorage;
import ro.alinvasile.projects.cache.internal.InMemoryCache;
import ro.alinvasile.projects.cache.io.InvalidDatabaseFileException;
import ro.alinvasile.projects.cache.io.SimpleFileDiskStoreImpl;
import ro.alinvasile.projects.cache.policy.EvictionPolicy;
import ro.alinvasile.projects.cache.policy.LeastRecentlyUsedEvictionPolicy;

public class CacheManager<K, V> implements Cache<K, V> {

    private long maxSize;

    private final CacheStorage<K, V> levelOneCache;

    private final CacheStorage<K, V> levelTwoCache;

    private EvictionPolicy policy;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private static Logger logger = LoggerFactory.getLogger(CacheManager.class);

    public CacheManager(String databaseFile, long desiredMaxSize) throws IOException, InvalidDatabaseFileException,
            ClassNotFoundException {
        this(databaseFile, desiredMaxSize, new LeastRecentlyUsedEvictionPolicy());
    }

    public CacheManager(long maxSize, CacheStorage<K, V> levelOneCacheImpl, CacheStorage<K, V> levelTwoCacheImpl,
            EvictionPolicy policy) {
        super();
        this.maxSize = maxSize;
        this.policy = policy;
        this.levelOneCache = levelOneCacheImpl;
        this.levelTwoCache = levelTwoCacheImpl;

        // feed L1 cache at startup
        CacheEntry[] entries = levelTwoCache.getEntries();
        for (CacheEntry entry : entries) {
            if (levelOneCache.getSize() == maxSize) {
                evict();
            }

            logger.debug("Loading {} into memory", entry);
            levelOneCache.put(entry);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                levelOneCache.close();
                levelTwoCache.close();
            }

        });
    }

    public CacheManager(String databaseFile, long desiredMaxSize, EvictionPolicy evictionPolicy)  throws ClassNotFoundException, IOException, InvalidDatabaseFileException {
        this(desiredMaxSize,new InMemoryCache<K,V>(), new SimpleFileDiskStoreImpl<K, V>(databaseFile),evictionPolicy);
    }
    
    

    @Override
    public void put(K key, V value) {
        Lock writeLock = lock.writeLock();

        try {
            writeLock.lock();

            CacheEntry<K, V> entry = new CacheEntry<K, V>(key, value);

            if (levelOneCache.getSize() == maxSize) {
                evict();
            }

            levelOneCache.put(entry);

            if (levelTwoCache.containsKey(key)) {
                levelTwoCache.remove(levelTwoCache.get(key));
            }

            levelTwoCache.put(entry);

        } finally {
            writeLock.unlock();
        }

    }

    private void evict() {
        CacheEntry selectedForEviction = policy.selectForEviction(levelOneCache.sampleForEviction());
        if (selectedForEviction != null) {
            levelOneCache.remove(selectedForEviction);
            // shold we remove stuff from L2 cache?
        }
    }

    @Override
    public V get(K key) {
        Lock readLock = lock.readLock();

        try {
            readLock.lock();

            CacheEntry<K, V> cacheEntry = levelOneCache.get(key);

            if (cacheEntry != null) {
                // no need to update L2 - cache with last access time
                // overcomplicates stuff
                return cacheEntry.getValue();
            } else {
                // check L2
                CacheEntry<K, V> cacheEntry2 = levelTwoCache.get(key);
                
                if(cacheEntry2!=null){
                    //  Update L1 & L2
                    readLock.unlock();
                    readLock = null;
                    put(cacheEntry2.getKey(),cacheEntry2.getValue());
                    
                    return cacheEntry2.getValue();
                    
                }
                
                return null;
            }

        } finally {
            if(readLock != null){
                readLock.unlock();
            }
        }

    }

    public int getSize() {
        Lock readLock = lock.readLock();

        try {
            readLock.lock();
            return levelOneCache.getSize();
        } finally {
            readLock.unlock();
        }

    }

}
