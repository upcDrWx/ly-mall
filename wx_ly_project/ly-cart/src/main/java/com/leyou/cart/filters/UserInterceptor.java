package com.leyou.cart.filters;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor extends HandlerInterceptorAdapter {

    private JwtProperties prop;

    // 定义一个线程域，存放登录用户
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try{
            // 解析成功，证明已经登录
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 放入线程域,传递user
            tl.set(user);
            //放行
            return true;
        }catch(Exception e){
           log.error("[购物车服务] 解析用户身份失败.",e);
            return false;
        }


    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getUser() {
        return tl.get();
    }
}