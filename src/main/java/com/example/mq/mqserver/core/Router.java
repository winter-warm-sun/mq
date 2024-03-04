package com.example.mq.mqserver.core;

import com.example.mq.common.MqException;

/*
 * 使用这个类，来实现交换机的转发规则
 * 同时也借助这个类验证 bindingKey 是否合法
 */
public class Router {
    // bindingKey 的构造规则：
    // 1. 数字，字母，下划线
    // 2. 使用 . 分割成若干部分
    // 3. 允许存在 * 和 # 作为通配符，但是通配符只能作为独立的分段
    public boolean checkBindingKey(String bindingKey) {
        // todo
        return true;
    }

    public boolean checkRoutingKey(String routingKey) {
        // todo
        return true;
    }

    // 这个方法用来判定该消息是否可以转发给这个绑定对应的队列
    public boolean route(ExchangeType exchangeType,Binding binding,Message message) throws MqException {
        // 根据不同的 exchangeType 使用不同的判定转发规则
        if(exchangeType==ExchangeType.FANOUT) {
            // 如果是 FANOUT 类型，则该交换机上绑定的所有队列都需要转发
            return true;
        }else if(exchangeType==ExchangeType.TOPIC) {
            // 如果是 TOPIC 主题交换机，规则就要更复杂一些
            // todo
            return true;
        }else {
            // 其他情况是不应该存在的
            throw new MqException("[Router] 交换机类型非法！ exchangeType="+exchangeType);
        }
    }
}
