package ro.alinvasile.projects.cache.internal;

public interface CacheStorage<K, T> {

    void put(CacheEntry<K, T> entry);

    CacheEntry<K, T> get(K key);

    CacheEntry<K, T>[] sampleForEviction();

    int getSize();

    void remove(CacheEntry<K, T> key);
    
    boolean containsKey(K key);
    
    CacheEntry<K, T>[] getEntries();

    void close();

}
