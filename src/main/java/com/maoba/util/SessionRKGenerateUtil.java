package com.maoba.util;
import java.io.Serializable;

/**
 * @author kitty daddy
 * 获取相关key值
 */
public class SessionRKGenerateUtil {
	public static final String keyPrefix = "redis_session:";
	
	/**
	 * 获得byte[]型的key
	 * @param key
	 * @return
	 */
	public static String getByteKey(Serializable sessionId) {
		String preKey = keyPrefix + sessionId;
		return preKey;
	}
}
