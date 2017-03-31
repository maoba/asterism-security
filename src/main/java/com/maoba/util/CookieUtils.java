package com.maoba.util;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author kitty daddy
 */
public class CookieUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(CookieUtils.class);
	
	/**
	 * 添加cookie
	 * @param response
	 * @param key
	 * @param value
	 * @param maxAge
	 * @return
	 */
	public Cookie addCookie(HttpServletResponse response, String key,String value) {
		if(StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
			return null;
		}
		Cookie cookie = new Cookie(key, value);
		response.addCookie(cookie);
		return cookie;
	}

	/**
	 * 添加cookie
	 * @param response
	 * @param key
	 * @param value
	 * @param maxAge 超时时间（单位秒）
	 * @return
	 */
	public static Cookie addCookie(HttpServletResponse response, String key,String value, int maxAge) {
		if(StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
			return null;
		}
		if(maxAge == 0) {
			// cookie默认有效期30分钟
			maxAge = 60 * 30;
		}
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
		return cookie;
	}
	
	/**
	 * 添加cookie
	 * @param response
	 * @param key
	 * @param value
	 * @param maxAge 超时时间（单位秒）
	 * @param path cookie uri路径
	 * @return
	 */
	public Cookie addCookie(HttpServletResponse response, String key,String value, int maxAge,String path) {
		if(StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
			return null;
		}
		if(maxAge == 0) {
			// cookie默认有效期30分钟
			maxAge = 60 * 30;
		}
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		response.addCookie(cookie);
		return cookie;
	}

	/**
	 * 读取Cookie
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static String getCookie(HttpServletRequest request,String key) {
		if(StringUtils.isBlank(key)) {
			return null;
		}
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			//日志打印
			for(Cookie cookie : cookies){
				logger.debug("cookie信息：name = "+cookie.getName()+"； value = "+cookie.getValue());
			}
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 删除cookie
	 * 
	 * @param request
	 * @param key
	 * @return boolean true:删除成功  false:删除失败
	 */
	public static boolean deleteCookie(HttpServletRequest request,String key) {
		if(StringUtils.isBlank(key)) {
			return false;
		}
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					cookie.setValue("");
					cookie.setMaxAge(0);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 删除cookie
	 * @param key
	 * @param path
	 * @param response
	 */
	public void deleteCookie(String key,String path,HttpServletResponse response){
		Cookie cookie=new Cookie(key,null);
		cookie.setMaxAge(0);
		cookie.setPath(path);
		response.addCookie(cookie);
	}
}
