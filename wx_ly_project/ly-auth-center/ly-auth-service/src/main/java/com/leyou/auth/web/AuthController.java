package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<String> login(
            @RequestParam("username") String username,@RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response
    ){
        //登录
        String token=authService.login(username,password);
        //TODO 写入cookie
        CookieUtils.newBuilder(response).httpOnly().request(request)
        .build(cookieName,token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 校验用户登录状态
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request, HttpServletResponse response
    ) {
        try {
            // 解析token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 刷新token,重新生成token
            String newToken = JwtUtils.generateToken(userInfo,
                    prop.getPrivateKey(), prop.getExpire());
            // 然后写入cookie中
            // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
            CookieUtils.newBuilder(response).httpOnly().request(request)
                    .build(cookieName,token);
            // 已登录，返回用户信息
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // token已过期，或者 token被篡改
            throw new LyException(ExceptionEnum.UN_AUTHORIZED);
        }
    }

/*校验管理员登录*/
    @PostMapping("adminLogin")
    public ResponseEntity<String> adminLogin(
            @RequestParam("username") String username,@RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response
    ){
        //登录
        String token=authService.login(username,password);
        //管理员判断
        Boolean adminLogin = authService.adminLogin(username, password);
        if(!adminLogin){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        //TODO 写入cookie
        CookieUtils.newBuilder(response).httpOnly().request(request)
                .build(cookieName,token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
