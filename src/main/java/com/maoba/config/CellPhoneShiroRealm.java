package com.maoba.config;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.maoba.common.enums.StatusEnum;
import com.maoba.facade.dto.responsedto.UserResponse;
import com.maoba.service.RolePermissionService;
import com.maoba.service.UserRoleService;
import com.maoba.service.UserService;
import com.maoba.system.domain.UserRoleEntity;
/**
 * @author kitty daddy
 * 手机登入校验
 */
public class CellPhoneShiroRealm extends AuthorizingRealm {
	/**
	 * 用户服务
	 */
	@Autowired
	private UserService userService;
	
	/**
	 * 用户角色服务
	 */
	@Autowired
	private UserRoleService userRoleService;
	
	/**
	 * 角色权限服务
	 */
	@Autowired
	private RolePermissionService rolePermissionService;
	
	/**
	 * 此方法调用 hasRole,hasPermission的时候才会进行回调.
	 *  权限信息.(授权): 
	 *  1、如果用户正常退出，缓存自动清空；
	 *  2、如果用户非正常退出，缓存自动清空； 
	 *  3、如果我们修改了用户的权限，而用户不退出系统，修改的权限无法立即生效。
	 * （需要手动编程进行实现；放在service进行调用）
	 * 在权限修改后调用realm中的方法，realm已经由spring管理，所以从spring中获取realm实例， 调用clearCached方法；
	 * :Authorization 是授权访问控制，用于对用户进行的操作授权，证明该用户是否允许进行当前操作，如访问某个链接，某个资源文件等。
	 * @param principals
	 * @return
	 */
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
		/** 获取当前的用户登录名 **/
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		long userId = (long) session.getAttribute("userId");
		long tenantId = (long) session.getAttribute("tenantId");
		/**权限信息对象info,用来存放查出的用户的所有的角色（role）及权限（permission**/
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setRoles(this.getRoleCode(userId, tenantId));
		info.setStringPermissions(this.getPermissions(userId, tenantId));
		return info;
	}

	/**
	 * 根据loginName判断是手机登录还是邮箱登入获取BaseUserDto
	 * @param loginName
	 * @return
	 */
	private UserResponse getUserResponse(String loginName,Integer terminalType) {
		UserResponse response = null;
		if (StringUtils.isNotEmpty(loginName) && (loginName.indexOf("@") != -1)) {// 表示邮箱登入
			response = userService.queryUserByEmail(loginName,terminalType);
			
		} else if (StringUtils.isNotEmpty(loginName)) {// 表示手机登入
			response = userService.queryUserByCellPhone(loginName,terminalType);
			
		} else {
			throw new UnknownAccountException();
		}
		return response;
	}

	/**
	 * 校验用户的相关的登录的信息
	 */
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		SystemToken token = (SystemToken) authenticationToken;
		//获取登录名称
		String loginName = token.getUsername();
		//获取终端类型
		Integer terminalType = token.getTerminalType();
		UserResponse response = this.getUserResponse(loginName,terminalType);
		if(response != null){
			if((StatusEnum.LOCKED.getValue() == response.getStatus())){
				throw new LockedAccountException(); // 帐号锁定
			}else{
				SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(loginName,
						response.getUserPwd(), getName());
				authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(loginName + response.getSalt()));
				return authenticationInfo;
			}
		}
		return null;
	}

	/**
	 * 获取相关的权限的名字
	 * @param userId
	 * @return
	 */
	private Set<String> getPermissions(long userId, long tenantId) {
		Set<String> permissions = new HashSet<String>();
		List<UserRoleEntity> userRoles = userRoleService.queryUserRole(userId, tenantId);
		permissions = rolePermissionService.queryRolePermission(userRoles);
		return permissions;
	}

	/**
	 * 根据user
	 * @param userId
	 * @return
	 */
	private Set<String> getRoleCode(long userId, long tenantId) {
		Set<String> roleCodes = userRoleService.queryRoleCodes(userId,tenantId);
		return roleCodes;
	}

	@Override
	public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
		super.clearCachedAuthorizationInfo(principals);
	}

	@Override
	public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
		super.clearCachedAuthenticationInfo(principals);
	}

	@Override
	public void clearCache(PrincipalCollection principals) {
		super.clearCache(principals);
	}

	public void clearAllCachedAuthorizationInfo() {
		getAuthorizationCache().clear();
	}

	public void clearAllCachedAuthenticationInfo() {
		getAuthenticationCache().clear();
	}

	public void clearAllCache() {
		clearAllCachedAuthenticationInfo();
		clearAllCachedAuthorizationInfo();
	}
}
