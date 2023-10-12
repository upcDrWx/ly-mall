package com.leyou.auth.service;


public interface AuthService {

    /**
     * 根据用户名和密码进行登录授权工作
     * @param username
     * @param password
     * @return
     */
    String login(String username, String password);

    /**
     * 根据用户名和密码进行管理员登录授权工作
     * @param username
     * @param password
     * @return
     */
    Boolean adminLogin(String username, String password);
}
