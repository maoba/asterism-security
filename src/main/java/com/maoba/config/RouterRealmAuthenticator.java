package com.maoba.config;
import java.util.Collection;
import java.util.Map;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.CollectionUtils;
import com.maoba.common.enums.TerminalTypeEnum;
/**
 * @author kitty daddy
 * pc端以及手机端选择不同的校验认证方式进行登入
 */
public class RouterRealmAuthenticator extends ModularRealmAuthenticator {
	private Map<String, Object> definedRealms;

	/**
	 * 多个realm实现
	 */
	@Override
	protected AuthenticationInfo doMultiRealmAuthentication(Collection<Realm> realms, AuthenticationToken token) {
		return super.doMultiRealmAuthentication(realms, token);
	}

	/**
	 * 调用单个realm执行操作
	 */
	@Override
	protected AuthenticationInfo doSingleRealmAuthentication(Realm realm, AuthenticationToken token) {
		// 如果该realms不支持(不能验证)当前token
		if (!realm.supports(token)) {
			throw new ShiroException("token错误!");
		}
		AuthenticationInfo info = null;
		try {
			info = realm.getAuthenticationInfo(token);

			if (info == null) {
				throw new ShiroException("token不存在!");
			}
		} catch (Exception e) {
			throw new ShiroException("用户名或者密码错误!");
		}
		return info;
	}

	/**
	 * 判断登入的类型执行相关的操作
	 */
	protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		this.assertRealmsConfigured();
		Realm realm = null;
		SystemToken token = (SystemToken) authenticationToken;
		if (token.getTerminalType().equals(TerminalTypeEnum.TERMINAL_CELL_PHONE.getValue())) {
			realm = (Realm) this.definedRealms.get("cellPhoneShiroRealm");
		}
		if (token.getTerminalType().equals(TerminalTypeEnum.TERMINAL_PC.getValue())) {
			realm = (Realm) this.definedRealms.get("pcShiroRealm");
		}
		if (realm == null) {
			return null;
		}
		return this.doSingleRealmAuthentication(realm, authenticationToken);
	}

	/**
	 * 判断realm是否为空
	 */
	@Override
	protected void assertRealmsConfigured() throws IllegalStateException {
		this.definedRealms = this.getDefinedRealms();
		if (CollectionUtils.isEmpty(this.definedRealms)) {
			throw new ShiroException("值传递错误!");
		}
	}

	public Map<String, Object> getDefinedRealms() {
		return definedRealms;
	}

	public void setDefinedRealms(Map<String, Object> definedRealms) {
		this.definedRealms = definedRealms;
	}
}
