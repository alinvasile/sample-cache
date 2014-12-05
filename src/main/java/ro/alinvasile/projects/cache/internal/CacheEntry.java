package ro.alinvasile.projects.cache.internal;

import java.io.Serializable;

public final class CacheEntry<K, V> implements Serializable {

    private static final long serialVersionUID = 4788048073522930826L;

    private final K key;

    private final V value;

    private final long creationTimestamp;

    private long lastUsedTimestamp;

    public CacheEntry(K key, V value) {
        super();
        this.key = key;
        this.value = value;
        this.creationTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return lastUsedTimestamp;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }

    public CacheEntry<K, V> touch() {
        this.lastUsedTimestamp = System.currentTimeMillis();
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (creationTimestamp ^ (creationTimestamp >>> 32));
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CacheEntry other = (CacheEntry) obj;
        if (creationTimestamp != other.creationTimestamp)
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CacheEntry [key=" + key + ", value=" + value + ", creationTimestamp=" + creationTimestamp
                + ", lastUsedTimestamp=" + lastUsedTimestamp + "]";
    }
    
    

}
