package ro.alinvasile.projects.cache.internal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.alinvasile.projects.cache.CacheManager;
import ro.alinvasile.projects.cache.policy.EvictionPolicy;

import static org.junit.Assert.*;


public class CacheManagerTest {

    @Mock
    CacheStorage levelOneCache;
    
    @Mock
    CacheStorage levelTwoCache;
    
    @Mock
    EvictionPolicy policy;
    
    CacheManager<String,String> cache;
    
    public CacheManagerTest(){
        MockitoAnnotations.initMocks(this);
    }
    
    @Before
    public void setup(){
        Mockito.when(levelOneCache.getEntries()).thenReturn(new CacheEntry[] {});
        Mockito.when(levelTwoCache.getEntries()).thenReturn(new CacheEntry[] {});
        
        cache = new CacheManager<String,String>(2, levelOneCache, levelTwoCache, policy);
    }
    
    @Test
    public void testPutUpdatesBothCacheStorages(){
        cache.put("name", "value");
        
        Mockito.verify(levelOneCache).put(Mockito.any(CacheEntry.class));
        Mockito.verify(levelTwoCache).put(Mockito.any(CacheEntry.class));
    }
    
    @Test
    public void testThatEvictionWorks(){
        
        Mockito.when(levelOneCache.getSize()).thenReturn(2);
        
        cache.put("home", "false");
        
        Mockito.verify(policy).selectForEviction(Mockito.any(CacheEntry[].class));
    }
    
    @Test
    public void testGetNonExistingKeyTouchesL2Cache(){
        Mockito.when(levelOneCache.get("x")).thenReturn(null);
        Mockito.when(levelTwoCache.get("x")).thenReturn(new CacheEntry("x","value"));
        
        assertEquals("value",cache.get("x"));
    }
    
}
