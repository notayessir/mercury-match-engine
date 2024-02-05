package com.notayessir.bo;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;


@Getter
public class OrderQueueBO extends LinkedHashMap<Long, OrderItemBO> implements Serializable {

    @Serial
    private static final long serialVersionUID = -3633885741760251789L;
    private BigDecimal remainEntrustQty = BigDecimal.ZERO;

    public void addOrder(OrderItemBO order) {
        put(order.getOrderId(), order);
        remainEntrustQty = remainEntrustQty.add(order.getRemainEntrustQty());
    }

    public void removeOrder(Long orderId){
        OrderItemBO order = remove(orderId);
        remainEntrustQty = remainEntrustQty.subtract(order.getRemainEntrustQty());
    }


    public void subtractRemainEntrustQty(BigDecimal qty){
        remainEntrustQty = remainEntrustQty.subtract(qty);
    }






}
