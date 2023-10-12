package com.leyou.auth;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.security.PublicKey;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtTest {
    private static final String pubKeyPath = "F:\\IdeaProjects\\HwjProjects\\auth\\rsa\\rsa.pub";

    private static final String priKeyPath = "F:\\IdeaProjects\\HwjProjects\\auth\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU1NjU0MDMyMH0.PbBVNa9PMbg3ZPLkG8VdNXTt_9NDSeQ57CTdqXzxlxZk_SvULt7Rz1d0MrXIoAOw_d6HyixDJKRz0f6tpC-3IdDsssOwLNE1QIpKNqJDIu4EeYDAoE63JKDUnrwF9i4ansWFZkt6AgVj5gH0idKqip0w7bGvpwQ7cSOZdnDNGho";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
