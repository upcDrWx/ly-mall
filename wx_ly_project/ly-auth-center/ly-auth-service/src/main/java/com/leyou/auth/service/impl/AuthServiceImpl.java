package com.leyou.auth.service.impl;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceImpl implements AuthService{
    @Autowired
    private UserClient userClient;

    @Autowired
    private  JwtProperties prop;
    @Override
    public String login(String username, String password) {
        try{
            //校验用户名和密码
            User user = userClient.queryUserByUsernameAndPassword(username, password);
            //判断
            if(user == null){
                throw  new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
            //生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), username),
                    prop.getPrivateKey(), prop.getExpire());
            return token;

        }catch(Exception e){
            log.error("[授权中心] 用户名或密码有误，用户名称：{}",username,e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    @Override
    public Boolean adminLogin(String username, String password) {
            //判断是否为管理员
            Boolean queryAdmin = userClient.queryAdmin(username, password);
            if(queryAdmin){
                return true;
            }
            log.error("[授权中心] 该用户不是管理员，用户名称：{}",username);
            return false;
    }
}
