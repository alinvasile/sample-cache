package ro.alinvasile.projects.cache.policy;

import ro.alinvasile.projects.cache.internal.CacheEntry;

import org.junit.Test;

import static org.junit.Assert.*;

public class LeastRecentlyUsedEvictionPolicyTest {

    private LeastRecentlyUsedEvictionPolicy policy = new LeastRecentlyUsedEvictionPolicy();

    @Test
    public void testEviction() throws InterruptedException {

        CacheEntry e1 = new CacheEntry("key1", "1");

        Thread.sleep(10);

        CacheEntry e2 = new CacheEntry("key2", "2");

        Thread.sleep(10);

        CacheEntry e3 = new CacheEntry("key3", "3");

        CacheEntry[] entries = new CacheEntry[] { e1, e2, e3 };
        CacheEntry selectedForEviction = policy.selectForEviction(entries);

        assertEquals(e1, selectedForEviction);

        e1.touch();

        selectedForEviction = policy.selectForEviction(entries);

        assertEquals(e2, selectedForEviction);

    }

    @Test
    public void testCompare() throws InterruptedException {
        CacheEntry e1 = new CacheEntry("key1", "1");

        Thread.sleep(10);

        CacheEntry e2 = new CacheEntry("key2", "2");

        Thread.sleep(10);

        assertTrue(policy.compareEntries(e1, e2) < 0);

        e1.touch();

        assertTrue(policy.compareEntries(e1, e2) > 0);
    }

}
