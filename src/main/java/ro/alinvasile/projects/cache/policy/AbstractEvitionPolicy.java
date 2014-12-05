package ro.alinvasile.projects.cache.policy;

import java.util.Arrays;
import java.util.Comparator;

import ro.alinvasile.projects.cache.internal.CacheEntry;

public abstract class AbstractEvitionPolicy implements EvictionPolicy {

    @Override
    public CacheEntry selectForEviction(CacheEntry[] entrySet) {
        Arrays.sort(entrySet, new Comparator<CacheEntry>() {

            @Override
            public int compare(CacheEntry o1, CacheEntry o2) {
                return compareEntries(o1, o2);
            }

        });
        return entrySet[0];
    }

}
