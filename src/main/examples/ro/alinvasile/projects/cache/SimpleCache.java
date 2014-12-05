package ro.alinvasile.projects.cache;

import java.io.IOException;

import ro.alinvasile.projects.cache.io.InvalidDatabaseFileException;

public class SimpleCache {

    public static void main(String args[]) throws ClassNotFoundException, IOException, InvalidDatabaseFileException{
        CacheManager<String, String> cache = new CacheManager<>("string.db", 2);
        
        cache.put("default", "1");
        cache.put("path", "/var/opt");
        cache.put("name", "odcb");
        cache.put("admin-mail", "admin2example.org");
        
        cache.put("default", "0");
        cache.put("path", "/tmp");
        
        System.out.println("default=" + cache.get("default"));
        System.out.println("path=" + cache.get("path"));
        System.out.println("admin-mail=" + cache.get("admin-mail"));
        
        System.out.println("nonexisting=" + cache.get("bla"));
        
        System.out.println("Cache current size " + cache.getSize());
    }
    
}
