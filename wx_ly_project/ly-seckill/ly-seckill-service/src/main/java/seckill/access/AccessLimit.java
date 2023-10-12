package seckill.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Feature: 接口限流注解
 */
@Retention(RetentionPolicy.RUNTIME)  // 定义注解的生命周期
@Target(ElementType.METHOD)    // 限定自定义注解可以使用在哪些地方
public @interface AccessLimit {

    /**
     * 限流时间
     *
     * @return
     */
    int seconds();

    /**
     * 最大请求次数
     *
     * @return
     */
    int maxCount();

    /**
     * 是否需要登录
     *
     * @return
     */
    boolean needLogin() default true;
}
