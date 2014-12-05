package ro.alinvasile.projects.cache.policy;

import ro.alinvasile.projects.cache.internal.CacheEntry;

/**
 * This is the default algorithm used in the project.
 * 
 * The oldest element is the Less Recently Used (LRU) element. The last used
 * timestamp is updated when an element is put into the cache or an element is
 * retrieved from the cache with a get call.
 *
 * 
 * @author Alin Vasile
 *
 */
public class LeastRecentlyUsedEvictionPolicy extends AbstractEvitionPolicy {

    @Override
    public int compareEntries(CacheEntry e1, CacheEntry e2) {
        return e1.getTimestamp() > e2.getTimestamp() ? 1 : (e1.getTimestamp() == e2.getTimestamp() ? 0 : -1);
    }

}
