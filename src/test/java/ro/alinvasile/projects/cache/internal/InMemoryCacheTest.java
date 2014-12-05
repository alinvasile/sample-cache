package ro.alinvasile.projects.cache.internal;

import org.junit.Test;

import static org.junit.Assert.*;

public class InMemoryCacheTest {

    @Test
    public void testAddToCache() {
        InMemoryCache<String, String> cache = new InMemoryCache<>();

        cache.put(new CacheEntry("key1", "1"));
        cache.put(new CacheEntry("key2", "2"));

        assertEquals("1", cache.get("key1").getValue());
        assertEquals("2", cache.get("key2").getValue());

        assertEquals(2, cache.getSize());

    }

    @Test
    public void testGetNonExistingKey() {
        InMemoryCache<String, String> cache = new InMemoryCache<>();

        cache.put(new CacheEntry("key1", "1"));

        assertNull(cache.get("nonexisting"));
    }

    @Test
    public void testGetOnEmptyCache() {
        InMemoryCache<String, String> cache = new InMemoryCache<>();

        assertNull(cache.get("nonexisting"));
        assertEquals(0, cache.getSize());
    }

    @Test
    public void testSampleForEviction() {
        InMemoryCache<String, String> cache = new InMemoryCache<>();

        cache.put(new CacheEntry("key1", "1"));
        cache.put(new CacheEntry("key2", "2"));

        CacheEntry<String, String>[] sampleForEviction = cache.sampleForEviction();

        assertNotNull(sampleForEviction);
        assertEquals(2, sampleForEviction.length);
    }

    public void testRemove() {
        InMemoryCache<String, String> cache = new InMemoryCache<>();

        cache.put(new CacheEntry("key1", "1"));

        CacheEntry entry = new CacheEntry("key2", "2");
        cache.put(entry);

        cache.remove(entry);

        assertNull(cache.get("key1"));
        assertEquals(1, cache.getSize());

    }

}
