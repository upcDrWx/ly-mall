package com.leyou.auth.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

///**
// *
// * Feign实现认证传递
// */
//@Configuration
//public class FeignClientConfig {
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return new RequestInterceptor() {
//            @Override
//            public void apply(RequestTemplate template) {
//                // 添加认证信息到请求头中
//                template.header("Authorization", "Bearer " + getToken());
//            }
//        };
//    }
//
//    private String getToken() {
//        // 获取认证信息的逻辑，可以从SecurityContext或其他地方获取
//        // 返回认证信息的字符串形式
//        return "your_token";
//    }
//}
