package com.notayessir.bo;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;


@Getter
public class OrderQueueBO extends LinkedHashMap<Long, OrderItemBO> {

    private BigDecimal remainEntrustQty = BigDecimal.ZERO;

    public void addOrder(OrderItemBO order) {
        put(order.getOrderId(), order);
        remainEntrustQty = remainEntrustQty.add(order.getRemainEntrustQty());
    }

    public void removeOrder(Long orderId){
        OrderItemBO order = remove(orderId);
        remainEntrustQty = remainEntrustQty.subtract(order.getRemainEntrustQty());
    }


    public void subtractRemainEntrustNum(BigDecimal num){
        remainEntrustQty = remainEntrustQty.subtract(num);
    }






}
