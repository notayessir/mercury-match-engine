package com.notayessir.bo;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.constant.EnumEntrustProp;
import com.notayessir.constant.EnumEntrustSide;
import com.notayessir.constant.EnumEntrustType;
import com.notayessir.constant.EnumMatchStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Getter
public class OrderBookBO implements Serializable {


    @Serial
    private static final long serialVersionUID = 3831714965938884709L;
    private Long txSequence ;

    /**
     * order who place sell direction
     */
    private final OrderTreemapBO ask = new OrderTreemapBO(Comparator.naturalOrder());

    /**
     *  order who place buy direction
     */
    private final OrderTreemapBO bid = new OrderTreemapBO(Comparator.reverseOrder());

    private final Map<Long, OrderItemBO> orders = new HashMap<>();

    public OrderBookBO(Long initSequence) {
        this.txSequence = initSequence;
    }



    private MatchResultBO placeMarketOrder(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? ask : bid;
        Set<Map.Entry<BigDecimal, OrderQueueBO>> priceMap = priceDepth.entrySet();
        Iterator<Map.Entry<BigDecimal, OrderQueueBO>> priceMapIterator = priceMap.iterator();
        List<MatchItemBO> trades = new ArrayList<>(8);
        long seq = 0;
        BREAKPOINT:
        while (priceMapIterator.hasNext()){
            Map.Entry<BigDecimal, OrderQueueBO> entry = priceMapIterator.next();

            OrderQueueBO orderQueue = entry.getValue();
            Iterator<Map.Entry<Long, OrderItemBO>> orderQueueIterator = orderQueue.entrySet().iterator();
            while (orderQueueIterator.hasNext()){
                Map.Entry<Long, OrderItemBO> orderEntry = orderQueueIterator.next();
                OrderItemBO makerOrder = orderEntry.getValue();

                // do trade
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (Objects.isNull(matchItemBO)){
                    break BREAKPOINT;
                }
                orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());

                // remove the market order if filled
                if (makerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
                    makerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
                    orderQueueIterator.remove();
                }

                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);
            }

            // no more order, remove queue
            if (orderQueue.isEmpty()) {
                priceMapIterator.remove();
            }
        }
        // always cancel
        takerOrder.setMatchStatus(EnumMatchStatus.CANCEL.getStatus());

        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
                .build();
    }

    private MatchResultBO placeNormalLimitOrder(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? ask : bid;
        Set<Map.Entry<BigDecimal, OrderQueueBO>> priceMap = priceDepth.entrySet();
        Iterator<Map.Entry<BigDecimal, OrderQueueBO>> priceMapIterator = priceMap.iterator();
        List<MatchItemBO> trades = new ArrayList<>(8);
        long seq = 0;
        BREAKPOINT:
        while (priceMapIterator.hasNext()){
            Map.Entry<BigDecimal, OrderQueueBO> entry = priceMapIterator.next();
            BigDecimal price = entry.getKey();

            if (!isMeetPrice(takerOrder, price)){
                continue;
            }

            OrderQueueBO orderQueue = entry.getValue();
            Iterator<Map.Entry<Long, OrderItemBO>> orderQueueIterator = orderQueue.entrySet().iterator();
            while (orderQueueIterator.hasNext()){
                Map.Entry<Long, OrderItemBO> orderEntry = orderQueueIterator.next();
                OrderItemBO makerOrder = orderEntry.getValue();
                // do trade
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (Objects.isNull(matchItemBO)){
                    break BREAKPOINT;
                }
                orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());

                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);

                // remove the market order if filled
                if (makerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
                    makerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
                    orderQueueIterator.remove();
                }
            }

            // no more order, remove queue
            if (orderQueue.isEmpty()) {
                priceMapIterator.remove();
            }
        }

        if (takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0) {
            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        } else {
            priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? bid : ask;
            priceDepth.addOrder(takerOrder);
            orders.put(takerOrder.getOrderId(), takerOrder);
        }

        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
                .build();

    }

    private MatchResultBO placePremiumLimitOrderOfIOC(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? ask : bid;
        Set<Map.Entry<BigDecimal, OrderQueueBO>> priceMap = priceDepth.entrySet();
        Iterator<Map.Entry<BigDecimal, OrderQueueBO>> priceMapIterator = priceMap.iterator();
        List<MatchItemBO> trades = new ArrayList<>(8);
        long seq = 0;
        BREAKPOINT:
        while (priceMapIterator.hasNext()){
            Map.Entry<BigDecimal, OrderQueueBO> entry = priceMapIterator.next();
            BigDecimal price = entry.getKey();

            if (!isMeetPrice(takerOrder, price)){
                continue;
            }

            OrderQueueBO orderQueue = entry.getValue();
            Iterator<Map.Entry<Long, OrderItemBO>> orderQueueIterator = orderQueue.entrySet().iterator();
            while (orderQueueIterator.hasNext()){
                Map.Entry<Long, OrderItemBO> orderEntry = orderQueueIterator.next();
                OrderItemBO makerOrder = orderEntry.getValue();
                // do trade
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (Objects.isNull(matchItemBO)){
                    break BREAKPOINT;
                }
                orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());
                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);

                // remove the market order if filled
                if (makerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
                    makerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
                    orderQueueIterator.remove();
                }

            }

            // no more order, remove queue
            if (orderQueue.isEmpty()) {
                priceMapIterator.remove();
            }
        }
        if (takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        } else {
            takerOrder.setMatchStatus(EnumMatchStatus.CANCEL.getStatus());
        }
        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
                .build();
    }

    private MatchResultBO placePremiumLimitOrderOfFOK(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? ask : bid;
        Set<Map.Entry<BigDecimal, OrderQueueBO>> priceMap = priceDepth.entrySet();
        Iterator<Map.Entry<BigDecimal, OrderQueueBO>> priceMapIterator = priceMap.iterator();
        List<MatchItemBO> trades = new ArrayList<>(8);
        long seq = 0;
        BREAKPOINT:
        while (priceMapIterator.hasNext()){
            Map.Entry<BigDecimal, OrderQueueBO> entry = priceMapIterator.next();
            BigDecimal price = entry.getKey();

            if (!isMeetPrice(takerOrder, price)){
                continue;
            }

            OrderQueueBO orderQueue = entry.getValue();
            if (orderQueue.getRemainEntrustQty().compareTo(takerOrder.getRemainEntrustQty()) < 0){
                break;
            }

            Iterator<Map.Entry<Long, OrderItemBO>> orderQueueIterator = orderQueue.entrySet().iterator();
            while (orderQueueIterator.hasNext()){
                Map.Entry<Long, OrderItemBO> orderEntry = orderQueueIterator.next();
                OrderItemBO makerOrder = orderEntry.getValue();
                // do trade
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (Objects.isNull(matchItemBO)){
                    break BREAKPOINT;
                }

                orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());
                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);

                // remove the market order if filled
                if (makerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
                    orderQueueIterator.remove();
                }
            }

            // no more order, remove queue
            if (orderQueue.isEmpty()) {
                priceMapIterator.remove();
            }
        }
        if (takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        } else {
            takerOrder.setMatchStatus(EnumMatchStatus.CANCEL.getStatus());
        }
        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
                .build();
    }

    private OrderItemBO buildOrderItem(MatchCommandBO commandBO) {
        OrderItemBO orderItemBO = new OrderItemBO();
        orderItemBO.setOrderId(commandBO.getOrderId());
        orderItemBO.setEntrustType(commandBO.getEntrustType());
        orderItemBO.setEntrustSide(commandBO.getEntrustSide());
        orderItemBO.setQuoteScale(commandBO.getQuoteScale());
        orderItemBO.setBaseScale(commandBO.getBaseScale());
        orderItemBO.setMatchStatus(EnumMatchStatus.NEW.getStatus());

        // entrust price
        orderItemBO.setEntrustPrice(commandBO.getEntrustPrice());

        // entrust qty
        orderItemBO.setEntrustQty(commandBO.getEntrustQty());
        orderItemBO.setRemainEntrustQty(commandBO.getEntrustQty());

        // entrust amount
        orderItemBO.setEntrustAmount(commandBO.getEntrustAmount());
        orderItemBO.setRemainEntrustAmount(commandBO.getEntrustAmount());

        return orderItemBO;
    }

    public MatchResultBO place(MatchCommandBO command) {
        MatchResultBO resultBO;
        OrderItemBO order = buildOrderItem(command);
        Integer entrustType = command.getEntrustType();
        if (entrustType == EnumEntrustType.NORMAL_LIMIT.getType()){
            resultBO = placeNormalLimitOrder(order);
        } else if (entrustType == EnumEntrustType.PREMIUM_LIMIT.getType()){
            Integer entrustProp = command.getEntrustProp();
            if (entrustProp == EnumEntrustProp.FOK.getType()){
                // FOK
                resultBO = placePremiumLimitOrderOfFOK(order);
            } else {
                // IOC
                resultBO = placePremiumLimitOrderOfIOC(order);
            }
        } else {
            // default: market order
            resultBO = placeMarketOrder(order);
        }
        resultBO.setTxSequence(++txSequence);
        return resultBO;


    }


    private MatchItemBO match(OrderItemBO takerOrder, OrderItemBO makerOrder) {
        BigDecimal makerEntrustPrice = makerOrder.getEntrustPrice();
        int quoteScale = takerOrder.getQuoteScale();
        BigDecimal clinchQty;
        log.info("takerOrder:{}", JSONObject.toJSONString(takerOrder));
        log.info("makerOrder:{}", JSONObject.toJSONString(makerOrder));
        if (takerOrder.getEntrustType() == EnumEntrustType.MARKET.getType()
                && takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()){
            clinchQty = takerOrder.getRemainEntrustAmount().divide(makerEntrustPrice, quoteScale, RoundingMode.DOWN);
        } else {
            clinchQty = takerOrder.getRemainEntrustQty();
        }

        if (clinchQty.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // minimum of clinch qty
        clinchQty = clinchQty.min(makerOrder.getRemainEntrustQty());

        takerOrder.setRemainEntrustQty(takerOrder.getRemainEntrustQty().subtract(clinchQty));
        makerOrder.setRemainEntrustQty(makerOrder.getRemainEntrustQty().subtract(clinchQty));

        // calc clinch amount
        if (takerOrder.getEntrustType() == EnumEntrustType.MARKET.getType()
                && takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()){
            BigDecimal clinchAmount = clinchQty.multiply(makerEntrustPrice);
            takerOrder.setRemainEntrustAmount(takerOrder.getRemainEntrustAmount().subtract(clinchAmount));
        }

        MatchItemBO itemBO = new MatchItemBO();
        itemBO.setMakerOrder(makerOrder);
        itemBO.setClinchPrice(makerEntrustPrice);
        itemBO.setClinchQty(clinchQty);
        itemBO.setSequence(++txSequence);
        return itemBO;
    }


    private boolean isMeetPrice(OrderItemBO takeOrder, BigDecimal price){
        if (takeOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()) {
            return takeOrder.getEntrustPrice().compareTo(price) >= 0;
        }
        // SELL
        return takeOrder.getEntrustPrice().compareTo(price) <= 0;
    }


    public boolean contains(Long orderId){
        return orders.containsKey(orderId);
    }


    public MatchResultBO cancel(Long orderId) {
        OrderItemBO orderItemBO = orders.remove(orderId);
        if (Objects.isNull(orderItemBO)){
            return new MatchResultBO();
        }

        // cancel is same direction
        OrderTreemapBO depth = orderItemBO.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? bid : ask;
        depth.removeOrder(orderItemBO);
        orderItemBO.setMatchStatus(EnumMatchStatus.CANCEL.getStatus());

        return MatchResultBO.builder()
                .takerOrder(orderItemBO).txSequence(++txSequence)
                .build();

    }



}
