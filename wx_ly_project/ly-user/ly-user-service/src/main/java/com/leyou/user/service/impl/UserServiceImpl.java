package com.leyou.user.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.user.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class UserServiceImpl  implements UserService{
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone";

    private static final String KEY_PREFIX2 = "leyou:user:info";

    @Override
    public Boolean checkData(String data, Integer type) {
        //判断数据类型
        User user = new User();
        switch (type){
            case 1 :
                user.setUsername(data);
                break;
            case 2 :
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user) == 0;
    }

    @Override
    public void sendCode(String phone) {
        String key=KEY_PREFIX+phone;
        //生成验证码
        String code=NumberUtils.generateCode(6);
        Map<String,String> msg=new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5,TimeUnit.MINUTES);
    }

    @Override
    public void register(User user, String code) {
        //从Redis取出验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        //校验验证码
        if(!StringUtils.equals(code,cacheCode)){
            throw  new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        //写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);

    }

    @Override
    public User queryUserByUsernameAndPassword(String username, String password) {
        //查询用户
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);

        //校验
        if(user == null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //校验密码
        if(!StringUtils.equals(user.getPassword(),CodecUtils.md5Hex(password,user.getSalt()))){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //用户名和密码正确
        return user;
    }

    /**
     * 判断是否为管理员
     * @param username
     * @param password
     * @return
     */
    @Override
    public Boolean isAdmin(String username, String password) {
        //查询用户
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);

        Long id = user.getId();//获取用户id

        //在tb_role_user中查询是否存在
        //这里没有进行查询，单纯的写了一个id判断，如果id不为31 ，就不是管理员
        if(id == 31){
            return true;
        }
        return false;
    }


}
