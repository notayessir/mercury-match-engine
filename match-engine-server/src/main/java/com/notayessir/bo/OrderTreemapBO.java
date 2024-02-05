package com.notayessir.bo;


import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;

public class OrderTreemapBO extends TreeMap<BigDecimal, OrderQueueBO> {

    public OrderTreemapBO(Comparator<BigDecimal> comparator) {
        super(comparator);
    }

    public void addOrder(OrderItemBO order) {
        OrderQueueBO queue = get(order.getEntrustPrice());
        if (Objects.isNull(queue)){
            queue = new OrderQueueBO();
            queue.addOrder(order);
            put(order.getEntrustPrice(), queue);
        } else {
            queue.addOrder(order);
        }
    }

    public void removeOrder(OrderItemBO order) {
        OrderQueueBO queue = get(order.getEntrustPrice());
        if (Objects.isNull(queue)){
            return;
        }
        queue.removeOrder(order.getOrderId());
        if (queue.isEmpty()){
            remove(order.getEntrustPrice());
        }
    }


}
