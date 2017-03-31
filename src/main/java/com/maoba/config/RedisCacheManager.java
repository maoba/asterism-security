package com.maoba.config;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maoba.util.RedisUtil;
/**
 * @author kitty daddy
 */
@Service
public class RedisCacheManager implements CacheManager{
   @Autowired
   private RedisUtil redisUtil;
   
   private String keyPrefix = "shiro_redis_cache:";    
	
	// fast lookup by name map    
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		Cache c = caches.get(name);
		if(c==null){
			c = new RedisCache<>(redisUtil,keyPrefix);
			caches.put(name, c);
		}
		return c;
	}

}
