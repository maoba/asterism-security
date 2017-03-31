package com.maoba.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
/**
 * @author kitty daddy
 *  为了防止密码遭到暴力破解，这里重写HashedCredentialsMatcher内部的doCredentialsMatch方法
 *  限制用户单位时间内用户密码重试的次数
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher{
	private Cache<String, AtomicInteger> passwordRetryCache;

	public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager) {
		passwordRetryCache = cacheManager.getCache("passwordRetryCache");
	}
	/**
	 * 如果在1个小时内密码最多重试5次，如果尝试次数超过5次，那么此时当前的用户就会被锁定一个小时，
	 * 1小时后可再次重试，如果还是重试失败，可以锁定如1天，以此类推，防止密码被暴力破解
	 */
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		String username = (String) token.getPrincipal();
		// retry count + 1
		AtomicInteger retryCount = passwordRetryCache.get(username);
		if (retryCount == null) {
			retryCount = new AtomicInteger(0);
			passwordRetryCache.put(username, retryCount);
		}
		if (retryCount.incrementAndGet() > 5) {
			// if retry count > 5 throw
			throw new ExcessiveAttemptsException();
		}
		boolean matches = super.doCredentialsMatch(token, info);
		if (matches) {
			// clear retry count
			passwordRetryCache.remove(username);
		}
		return matches;
	}
}
