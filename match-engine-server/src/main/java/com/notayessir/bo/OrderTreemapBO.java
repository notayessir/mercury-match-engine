package com.notayessir.bo;


import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;

public class OrderTreemapBO extends TreeMap<BigDecimal, OrderQueueBO> implements Serializable {

    @Serial
    private static final long serialVersionUID = -3226748733011153265L;

    public OrderTreemapBO(Comparator<BigDecimal> comparator) {
        super(comparator);
    }

    public void addOrder(OrderItemBO order) {
        OrderQueueBO queue = get(order.getEntrustPrice());
        if (Objects.isNull(queue)){
            queue = new OrderQueueBO();
            put(order.getEntrustPrice(), queue);
        }
        queue.addOrder(order);
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
