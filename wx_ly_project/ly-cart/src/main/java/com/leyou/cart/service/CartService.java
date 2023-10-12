package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.filters.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    static final String KEY_PREFIX = "cart:uid:";

    /**
     * 添加购物车
     * @param cart
     */
    public void addCart(Cart cart) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // Redis的key
        String key = KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = cart.getSkuId().toString();
        //记录num
        Integer num=cart.getNum();
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> opertion = redisTemplate.boundHashOps(key);

        //判断当前购物车商品，是否存在
        if(opertion.hasKey(hashKey)){
            //存在，修改数量
            String json = opertion.get(hashKey).toString();
            Cart cacheCart = JsonUtils.toBean(json, Cart.class);
            cart.setNum(cacheCart.getNum()+num);
        }
        //写回redis
        opertion.put(hashKey,JsonUtils.toString(cart));
    }


    /**
     * 查询购物车列表
     * @return
     */
    public List<Cart> quertyCartList() {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // Redis的key
        String key = KEY_PREFIX + user.getId();

        if(!redisTemplate.hasKey(key)){
            //key不存在，返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> opertion = redisTemplate.boundHashOps(key);

        List<Cart> carts = opertion.values().stream().map(o -> JsonUtils.toBean(o.toString(), Cart.class))
                .collect(Collectors.toList());
        return carts;
    }

    public void updateNum(Long skuId, Integer num) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // Redis的key
        String key = KEY_PREFIX + user.getId();
        //hashKey
        String hashKey=skuId.toString();
        //获取操作
        BoundHashOperations<String, Object, Object> opertions = redisTemplate.boundHashOps(key);

        //判断添加的商品是否存在
        if(!opertions.hasKey(skuId.toString())){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //查询购物车
        Cart cart = JsonUtils.toBean(opertions.get(skuId.toString()).toString(), Cart.class);
        cart.setNum(num);

        //写回redis
        opertions.put(hashKey,JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // Redis的key
        String key = KEY_PREFIX + user.getId();
        //删除
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
