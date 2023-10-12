package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    /**
     * 监听增加和更新
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search.item.insert.queue", durable = "true"),
            exchange = @Exchange(value = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
            ))
    public void listenInsertOrUpdate(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 处理消息，创建静态页
        pageService.creatHtml(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search.item.delete.queue", durable = "true"),
            exchange = @Exchange(value = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 处理消息，对静态页进行删除
        pageService.deleteHtml(spuId);
    }

}
