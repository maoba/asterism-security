package com.maoba.service;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maoba.common.enums.LoginTypeEnum;
import com.maoba.config.SystemToken;
import com.maoba.facade.dto.requestdto.UserLoginRequest;
import com.maoba.facade.dto.responsedto.UserResponse;
/**
 * @author kitty daddy
 */
@Service
public class SecurityService {
	@Autowired
	private UserService userService;
	private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
	
	/**
	 * pc端进行登入
	 * @param request
	 * @return
	 */
	public String login(UserLoginRequest request) {
		SystemToken token = null;
		UserResponse resp = null;
		if (request != null) {
			//邮件的形式进行登入
			if (request.getLoginType() == LoginTypeEnum.SYSTEM_EMAIL_LOGIN.getType()) {
				token = new SystemToken(request.getEmail(), request.getPassword());
				resp = userService.queryUserByEmail(request.getEmail(), request.getTerminalType()); 
				
			//手机的形式进行登入	
			} else if (request.getLoginType() == LoginTypeEnum.SYSTEM_CELLPHONE_LOGIN.getType()) {
				token = new SystemToken(request.getCellPhoneNum(), request.getPassword());
				resp = userService.queryUserByCellPhone(request.getCellPhoneNum(), request.getTerminalType()); 
			}
			
			token.setTerminalType(request.getTerminalType());
		}
		
		/** 获取当前的subject **/
		Subject currentUser = SecurityUtils.getSubject();
		Session session = null;
		try {
			currentUser.login(token);
			if(currentUser.isAuthenticated()){
				
				//获取session
				session = currentUser.getSession();
				if(resp!=null){
					
					//设置session中内容
					session.setAttribute("userName", resp.getUserName());
					session.setAttribute("userId", resp.getId());
					session.setAttribute("tenantId", resp.getTenantId());
					session.setAttribute("tenantName", resp.getTenantName());
					
					//如果是手机号码登入，那么则设置手机号码为session中的登录密码
					if(request.getLoginType() == LoginTypeEnum.SYSTEM_CELLPHONE_LOGIN.getType()){
						session.setAttribute("loginName", request.getCellPhoneNum());
					}else{
						session.setAttribute("loginName", request.getEmail());
					}
					session.setAttribute("sessionId", session.getId().toString());
				}
				return session.getId().toString();
			}
		} catch (UnknownAccountException uae) {
			logger.info("当前用户不存在");
			
		} catch (IncorrectCredentialsException ice) {
			logger.info("用户名/密码不正确");
			
		} catch (LockedAccountException lae) {
			logger.info("登入验证未通过，账户已经锁定");
			
		} catch (ExcessiveAttemptsException eae) {
			logger.info("登入错误次数太多");
			
		} catch (AuthenticationException ae) {
			logger.info("用户名或者密码错误");
		}
		return null;
	}

	/**
	 * 用户登出
	 */
	public void logout() {
		if (SecurityUtils.getSubject().isAuthenticated()) {
			SecurityUtils.getSubject().logout();
		}
	}
}
