package seckill.config;

import com.leyou.seckill.interceptor.AccessInterceptor;
import com.leyou.seckill.interceptor.LoginInterceptor;
import com.leyou.seckill.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: 98050
 * @create: 2018-10-27
 **/
@Configuration
@EnableWebMvc
//@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtProperties jwtProperties;

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor(jwtProperties);
    }

    @Autowired
    public AccessInterceptor accessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        List<String> excludePath = new ArrayList<>();
        excludePath.add("/list");
        excludePath.add("/addSeckill");
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**").excludePathPatterns(excludePath);
        registry.addInterceptor(accessInterceptor);
    }
//
//
//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//
//        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter =
//                new MappingJackson2HttpMessageConverter();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
//        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
//        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
//        objectMapper.registerModule(simpleModule);
//        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
//        converters.add(jackson2HttpMessageConverter);
//        converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
//    }

}
