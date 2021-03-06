package com.leyou.page.mq;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: ItemListener
 * @Author:   Administrator
 * @Date:     2018/12/14 21:01
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.leyou.page.service.GoodsService;
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
    private GoodsService goodsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insert.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void listenerUpdateOrInsert(Long spuId){
        if(spuId == null){
            return;
        }
        goodsService.createHtml(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenerDelete(Long spuId){
        if(spuId == null){
            return;
        }
        //删除静态页
        goodsService.deleteHtml(spuId);
    }
}
