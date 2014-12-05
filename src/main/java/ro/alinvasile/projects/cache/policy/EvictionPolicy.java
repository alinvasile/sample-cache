package ro.alinvasile.projects.cache.policy;

import ro.alinvasile.projects.cache.internal.CacheEntry;

/**
 * A cache eviction policy is a way of deciding which element to evict when the
 * cache is full.
 * 
 * @author Alin Vasile
 *
 */
public interface EvictionPolicy {

    int compareEntries(CacheEntry e1, CacheEntry e2);

    CacheEntry selectForEviction(CacheEntry[] entrySet);

}
