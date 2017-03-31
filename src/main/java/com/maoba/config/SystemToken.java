package com.maoba.config;
import org.apache.shiro.authc.UsernamePasswordToken;
/**
 * @author lujianhao
 * 自定义类型，校验登录的类型
 */
public class SystemToken extends UsernamePasswordToken {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * 终端类型
     */
	private Integer terminalType;
	
	
	public Integer getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(Integer terminalType) {
		this.terminalType = terminalType;
	}

	/**
	 * 构造方法继承父类
	 * @param username
	 * @param password
	 */
	public SystemToken(String username,String password) {
		super(username,password);
	}
	
	public SystemToken(){}
}
