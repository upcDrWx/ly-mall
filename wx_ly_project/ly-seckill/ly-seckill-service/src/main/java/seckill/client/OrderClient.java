package seckill.client;

import com.leyou.order.api.OrderApi;
import com.leyou.seckill.config.OrderConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 *  订单接口
 */
@FeignClient(value = "order-service",configuration = OrderConfig.class)
public interface OrderClient extends OrderApi {
}
