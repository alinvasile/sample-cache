package ro.alinvasile.projects.cache.policy;

import ro.alinvasile.projects.cache.internal.CacheEntry;

/**
 * Elements are evicted in the same order as they come in. When a put call is
 * made for a new element
 * 
 * @author Alin Vasile
 *
 */

public class FirstInFirstOutEvictionPolicy extends AbstractEvitionPolicy {

    @Override
    public int compareEntries(CacheEntry e1, CacheEntry e2) {
        return e1.getCreationTimestamp() > e2.getCreationTimestamp() ? 1 : (e1.getCreationTimestamp() == e2
                .getCreationTimestamp() ? 0 : -1);
    }

}
