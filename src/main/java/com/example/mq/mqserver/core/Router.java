package com.example.mq.mqserver.core;

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
        return true;
    }
}
