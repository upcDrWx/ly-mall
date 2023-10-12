package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import jdk.jfr.events.ThrowablesEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;


@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(
            @PathVariable("data") String data, @PathVariable(value = "type") Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> checkData(@RequestParam("phone")String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result,
                                         @RequestParam("code") String code) {
//        if(result.hasFieldErrors()){
//            throw new RuntimeException(result.getFieldErrors().stream()
//                    .map(e->e.getDefaultMessage()).collect(Collectors.joining("|")));
//        }
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 普通用户，根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        return ResponseEntity.ok( userService.queryUserByUsernameAndPassword(username, password));
    }

    /**
     * 管理员，根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/queryAdmin")
    public ResponseEntity<Boolean> queryAdmin(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        return ResponseEntity.ok( userService.isAdmin(username, password));
    }
}
