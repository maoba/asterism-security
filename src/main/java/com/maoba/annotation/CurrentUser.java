package com.maoba.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kitty daddy
 * 自定义标签
 * [目的：从redis获取缓存的session信息]
 */
@Documented  
@Target({ElementType.PARAMETER})  
@Retention(RetentionPolicy.RUNTIME)  
public @interface CurrentUser {
	
}