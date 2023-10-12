package com.leyou.user.service;

import com.leyou.user.pojo.User;
public interface UserService {

    /**
     * 检查用户名和手机号是否可用
     * @param data
     * @param type
     * @return
     */
    Boolean checkData(String data, Integer type);

    /**
     * 发送短信
     * @param phone
     */
    void sendCode(String phone);

    /**
     * 用户注册
     * @param user
     * @param code
     */
    void register(User user, String code);

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    User queryUserByUsernameAndPassword(String username, String password);

    /**
     * 判断是否为管理员，如果是返回true
     * @param username
     * @param password
     * @return
     */
    Boolean isAdmin(String username, String password);
}
