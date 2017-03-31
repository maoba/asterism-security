package com.maoba.dao;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.dao.DataAccessException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import com.maoba.util.RedisUtil;
import com.maoba.util.SerializeUtils;
import com.maoba.util.SessionRKGenerateUtil;

/**
 * @author kitty daddy
 * 实现会话的redis的增删改
 */
@Repository
public class RedisSessionDao extends AbstractSessionDAO{

	@Autowired
	private RedisUtil redisUtil;
    
	@Value("${spring.profiles.redisExpireTime}")
	private long redisExpireTime;
	
	public int getRedisExpireTime() {
		return (int) redisExpireTime;
	}
	
	public void update(Session session) throws UnknownSessionException {
		/**如果会话过期或者停止，那么此时没有必要进行更新*/
		if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
			return; 
		}
		this.saveSession(session);
	}

	/**
	 * 存储序列化的对象
	 * 
	 * @param key
	 * @param session
	 * @param l
	 */
	public boolean setSerializeKeyValue(final String name, Session session, final long expireTime) {
		boolean result = false;
		try {
			RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisUtil.getRedisTemplate()
					.getStringSerializer();
			final byte[] key = keySerializer.serialize(name);
			final byte[] v = SerializeUtils.serialize(session);
			redisUtil.getRedisTemplate().execute(new RedisCallback<Object>() {
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					connection.setEx(key, expireTime, v);
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * save session
	 * @param session
	 */
	private void saveSession(Session session) throws UnknownSessionException {
		if (session == null || session.getId() == null) {
			return;
		}
		String key = SessionRKGenerateUtil.getByteKey(session.getId());
		session.setTimeout(redisExpireTime*1000);
		this.setSerializeKeyValue(key, session, redisExpireTime);
	}

	public void delete(Session session) {
		if (session == null || session.getId() == null) {
			return;
		}
		redisUtil.remove(SessionRKGenerateUtil.getByteKey(session.getId()));
	}

	/**
	 * 统计当前的所有的session的活动数量
	 */
	public Collection<Session> getActiveSessions() {
		Set<Session> sessions = new HashSet<Session>();
		Set<String> keys = redisUtil.keys(SessionRKGenerateUtil.keyPrefix + "*");
		if (keys != null && keys.size() > 0) {
			for (String key : keys) {
				Session s = (Session) redisUtil.getAttribute(key);
				sessions.add(s);
			}
		}
		return sessions;
	}
	
	protected Serializable doCreate(Session session) {
		Serializable sessionId = this.generateSessionId(session);
		this.assignSessionId(session, sessionId);
		this.saveSession(session);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if (sessionId == null) {
			return null;
		}
		Session session = (Session) redisUtil.getAttribute(SessionRKGenerateUtil.getByteKey(sessionId));
		return session;
	}
}
