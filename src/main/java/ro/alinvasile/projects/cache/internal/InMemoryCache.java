package ro.alinvasile.projects.cache.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryCache<K, V> implements CacheStorage<K, V> {

    private Map<K, CacheEntry<K, V>> cache = new ConcurrentHashMap<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void put(CacheEntry<K, V> entry) {
        Lock writeLock = lock.writeLock();

        try {
            writeLock.lock();
            cache.put(entry.getKey(), entry);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(CacheEntry<K, V> entry) {
        Lock writeLock = lock.writeLock();

        try {
            writeLock.lock();
            cache.remove(entry.getKey());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public CacheEntry<K, V> get(K key) {

        CacheEntry<K, V> cacheEntry = null;

        Lock readLock = lock.readLock();
        try {
            readLock.lock();

            cacheEntry = cache.get(key);

            if (cacheEntry == null) {
                return null;
            }

        } finally {
            readLock.unlock();
        }

        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();

            // this entry might be removed meanwhile
            if (cache.containsKey(key)) {
                cacheEntry.touch();
                cache.put(key, cacheEntry);
            }

        } finally {
            writeLock.unlock();
        }

        return cacheEntry;
    }

    @Override
    public int getSize() {
        Lock readLock = lock.readLock();

        try {
            readLock.lock();
            return cache.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public CacheEntry<K, V>[] sampleForEviction() {
        return getEntries();
    }

    @Override
    public boolean containsKey(K key) {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return cache.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public CacheEntry<K, V>[] getEntries() {
        Lock readLock = lock.readLock();

        try {
            readLock.lock();
            if (cache.isEmpty()) {
                return new CacheEntry[] {};
            }
            return cache.values().toArray(new CacheEntry[] {});
        } finally {
            readLock.unlock();
        }

        
    }

    @Override
    public void close() {
       // do nothing
        
    }

}
