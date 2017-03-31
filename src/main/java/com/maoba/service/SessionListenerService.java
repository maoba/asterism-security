package com.maoba.service;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maoba.dao.RedisSessionDao;
@Service
public class SessionListenerService implements SessionListener{
	@Autowired
	private RedisSessionDao redisSessionDao;
	public void onStart(Session session) {
		System.out.println(session.getId()+"当前的会话被创建");
	}
	
	/**
	 * 当退出或者会话过期时候调用
	 */
	public void onStop(Session session) {
		System.out.println(session.getId()+"当前的会话时间已经停止");
		redisSessionDao.delete(session);
	}
	
	/**
	 * 当session会话时间到期的时候调用
	 */
	public void onExpiration(Session session) {
		System.out.println(session.getId()+"当前的会话时间已经过期");
		redisSessionDao.delete(session);
	}

}
