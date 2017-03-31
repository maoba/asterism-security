package com.maoba.config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import com.maoba.dao.RedisSessionDao;
import com.maoba.service.SessionListenerService;
import com.maoba.util.RedisUtil;
/**
 * @author kitty daddy
 */
@Configuration
@AutoConfigureAfter(Environment.class)
@ConditionalOnBean(RedisSessionDao.class)
@Repository
public class ShiroConfiguration {
	@Bean
	public RedisCacheManager redisCacheManager(){
		RedisCacheManager redisCacheManager = new RedisCacheManager();
		return redisCacheManager;
	}
	
	@Bean
	public RedisUtil redisUtil(){
		RedisUtil redisUtil = new RedisUtil();
		return redisUtil;
	}
	
	@Bean
	public RedisCache redisCache(){
		String prefix = "shiro_redis_cache:";
		RedisCache redisCache = new RedisCache<>(this.redisUtil(), prefix);
		return redisCache;
	}
	
	@Bean
	public AtLeastOneSuccessfulStrategy getStrategy(){
		AtLeastOneSuccessfulStrategy strategy = new AtLeastOneSuccessfulStrategy();
		return strategy;
	} 
	
	/**
	 *  配置放暴力破解
	 * @return
	 */
	@Bean
	public RetryLimitHashedCredentialsMatcher retryLimitHashedCredentialsMatcher(){
		RetryLimitHashedCredentialsMatcher retryLimitHashedCredentialsMatcher =
				  new RetryLimitHashedCredentialsMatcher(this.redisCacheManager());
		retryLimitHashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
		return retryLimitHashedCredentialsMatcher;
	}
	
	/**
	 * 注入自己定义的校验规则
	 * @param cacheManager
	 * @return
	 */
	@Bean(name = "pcShiroRealm")
	public PcShiroRealm pcShiroRealm() {
		PcShiroRealm realm = new PcShiroRealm();
		realm.setCacheManager(this.redisCacheManager());
		realm.setCredentialsMatcher(this.hashedCredentialsMatcher());
		return realm;
	}
	
	@Bean(name = "cellPhoneShiroRealm")
	public CellPhoneShiroRealm cellPhoneShiroRealm(){
		CellPhoneShiroRealm realm = new CellPhoneShiroRealm();
		realm.setCacheManager(this.redisCacheManager());
		realm.setCredentialsMatcher(this.hashedCredentialsMatcher());
		return realm;
	}

	@Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher(){
       HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
       /**采用md5的散列算法**/
       hashedCredentialsMatcher.setHashAlgorithmName("md5");
       hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
       /**散列算法md5进行加密两次**/
       hashedCredentialsMatcher.setHashIterations(2);
       return hashedCredentialsMatcher;
    }
	
	/**
	 * 自动代理
	 * @return
	 */
	@Bean
	public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
		daap.setProxyTargetClass(true);
		return daap;
	}

	/**
	 * shiro生命周期管理器
	 * @return
	 */
	@Bean(name = "lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	@Bean(name = "sessionIdCookie")
	public SimpleCookie getSessionIdCookie(int age) {
		SimpleCookie cookie = new SimpleCookie("sid");
		cookie.setHttpOnly(true);
		// 单位秒
		cookie.setMaxAge(age);
		return cookie;
	}

	@Bean(name = "rememberMeCookie")
	public SimpleCookie getRememberMeCookie() {
		SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
		simpleCookie.setHttpOnly(true);
		simpleCookie.setMaxAge(2592000);
		return simpleCookie;
	}

	@Bean(name = "rememberMeManager")
	public CookieRememberMeManager rememberMeManager() {
		CookieRememberMeManager meManager = new CookieRememberMeManager();
		meManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
		meManager.setCookie(getRememberMeCookie());
		return meManager;
	}
    
	@Bean
	public RedisSessionDao getRedisSessionDao(){
		return new RedisSessionDao();
	}
	
	@Bean
	public SessionListenerService getSessionListenerService(){
		return new SessionListenerService();
	}
	
	@Bean(name = "sessionManager")
	public DefaultWebSessionManager getDefaultWebSessionManager() {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		/** 设置session的有效时间 **/
		sessionManager.setGlobalSessionTimeout(604800000);
		/** 设置session的定时检查是否有效的时间 **/
		sessionManager.setSessionValidationInterval(1800000);
		sessionManager.setCacheManager(this.redisCacheManager());
		sessionManager.setDeleteInvalidSessions(false);
		sessionManager.setSessionValidationSchedulerEnabled(true);
		RedisSessionDao sessionDAO = getRedisSessionDao();
		sessionManager.setSessionDAO(sessionDAO);
		List<SessionListener> sessionList = new ArrayList<SessionListener>();
		sessionList.add(getSessionListenerService());
		sessionManager.setSessionListeners(sessionList);
		sessionManager.setSessionIdCookie(getSessionIdCookie(sessionDAO.getRedisExpireTime()));
		sessionManager.setSessionIdCookieEnabled(true);
		return sessionManager;
	}
    
	/**
	 * 获取当前的securityManager
	 * @param myShiroRealm
	 * @return
	 */
	@Bean(name = "securityManager")
	public DefaultWebSecurityManager getDefaultWebSecurityManager(SessionListenerService sessionListenerService) {
		DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
		dwsm.setAuthenticator(this.getAuthenticator());
		/** 设置具体的sessionManager **/
		dwsm.setSessionManager(getDefaultWebSessionManager());
		/** 用户授权/认证信息Cache, 采用EhCache 缓存 **/
		dwsm.setCacheManager(this.redisCacheManager());
		dwsm.setRememberMeManager(rememberMeManager());
		return dwsm;
	}
	
	@Bean
	public ModularRealmAuthenticator getAuthenticator(){
		RouterRealmAuthenticator authenticator = new RouterRealmAuthenticator();
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("pcShiroRealm", this.pcShiroRealm());
		map.put("cellPhoneShiroRealm", this.cellPhoneShiroRealm());
		authenticator.setDefinedRealms(map);
		return authenticator;
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(
			DefaultWebSecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
		aasa.setSecurityManager(securityManager);
		return aasa;
	}

	/**
	 * 加载shiroFilter权限控制规则(从数据库读取然后配置)
	 */
	private void loadShiroFilterChain(ShiroFilterFactoryBean shiroFilterFactoryBean) {
		/////////////////////// 下面这些规则配置最好配置到配置文件中 ///////////////////////
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
		// authc：该过滤器下的页面必须验证后才能访问，它是Shiro内置的一个拦截器org.apache.shiro.web.filter.authc.FormAuthenticationFilter
		// anon：它对应的过滤器里面是空的,什么都没做
		/** ##################从数据库读取权限规则，加载到shiroFilter中##################" **/
		filterChainDefinitionMap.put("/login", "anon");
		filterChainDefinitionMap.put("/logout", "anon");
		filterChainDefinitionMap.put("/**", "anon");//anon 可以理解为不拦截
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
	}

	/**
	 * ShiroFilter 注意这里参数中的 StudentService 和 IScoreDao
	 * 只是一个例子，因为我们在这里可以用这样的方式获取到相关访问数据库的对象， 然后读取数据库相关配置，配置到
	 * shiroFilterFactoryBean 的访问规则中。实际项目中，请使用自己的Service来处理业务逻辑。
	 * @param myShiroRealm
	 * @param stuService
	 * @param scoreDao
	 * @return
	 */
	@Bean(name = "shiroFilter")
	public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new MShiroFilterFactoryBean();
		// 必须设置 SecurityManager
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		loadShiroFilterChain(shiroFilterFactoryBean);
		return shiroFilterFactoryBean;
	}
}
