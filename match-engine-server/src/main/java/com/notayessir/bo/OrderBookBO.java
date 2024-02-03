package com.notayessir.bo;

import cn.hutool.core.collection.CollectionUtil;
import com.notayessir.constant.EnumEntrustProp;
import com.notayessir.constant.EnumEntrustSide;
import com.notayessir.constant.EnumEntrustType;
import com.notayessir.constant.EnumMatchStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Getter
public class OrderBookBO {


    private long txSequence ;

    /**
     * order to sell
     */
    private final OrderTreemapBO ask = new OrderTreemapBO(Comparator.naturalOrder());

    /**
     * buy to buy
     */
    private final OrderTreemapBO bid = new OrderTreemapBO(Comparator.reverseOrder());

    private final Map<Long, OrderItemBO> orders = new HashMap<>();

    public OrderBookBO(long initSequence) {
        this.txSequence = initSequence;
    }

    public long addAndGetSequence(long value){
        txSequence += value;
        return txSequence;
    }

    private MatchResultBO placeMarketOrder(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth =
                takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ?
                ask : bid;
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
                MatchItemBO matchItemBO = trade(takerOrder, makerOrder);
                if (Objects.nonNull(matchItemBO)){
                    orderQueue.subtractRemainEntrustNum(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

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
        if (takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0) {
            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        } else {
            takerOrder.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());
        }

        return MatchResultBO.builder()
                .trades(trades).takerOrder(takerOrder)
                .build();
    }

    private MatchResultBO placeNormalLimitOrder(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ?
                ask : bid;
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
                MatchItemBO matchItemBO = trade(takerOrder, makerOrder);
                if (Objects.nonNull(matchItemBO)){
                    orderQueue.subtractRemainEntrustNum(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

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

        if (takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0) {
            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        } else {
            if (takerOrder.getRemainEntrustQty().compareTo(takerOrder.getEntrustQty()) == 0) {
                takerOrder.setMatchStatus(EnumMatchStatus.OPEN.getStatus());
            }
            priceDepth.addOrder(takerOrder);
            orders.put(takerOrder.getOrderId(), takerOrder);
        }

        return MatchResultBO.builder()
                .trades(trades).takerOrder(takerOrder)
                .build();

    }

    private MatchResultBO placePremiumLimitOrderOfIOC(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth =
                takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ?
                ask : bid;
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
                MatchItemBO matchItemBO = trade(takerOrder, makerOrder);
                if (Objects.nonNull(matchItemBO)){
                    orderQueue.subtractRemainEntrustNum(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

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
        takerOrder.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());
        return MatchResultBO.builder()
                .trades(trades).takerOrder(takerOrder)
                .build();
    }

    private MatchResultBO placePremiumLimitOrderOfFOK(OrderItemBO takerOrder){
        OrderTreemapBO priceDepth =
                takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ?
                        ask : bid;
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
            if (orderQueue.getRemainEntrustQty().compareTo(takerOrder.getEntrustQty()) < 0){
                break;
            }

            Iterator<Map.Entry<Long, OrderItemBO>> orderQueueIterator = orderQueue.entrySet().iterator();
            while (orderQueueIterator.hasNext()){
                Map.Entry<Long, OrderItemBO> orderEntry = orderQueueIterator.next();
                OrderItemBO makerOrder = orderEntry.getValue();
                // do trade
                MatchItemBO matchItemBO = trade(takerOrder, makerOrder);
                if (Objects.nonNull(matchItemBO)){
                    orderQueue.subtractRemainEntrustNum(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

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
        takerOrder.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());
        return MatchResultBO.builder()
                .trades(trades).takerOrder(takerOrder)
                .build();
    }

    public MatchResultBO place(OrderItemBO takerOrder) {
        MatchResultBO event;
        Integer entrustType = takerOrder.getEntrustType();
        if (entrustType.equals(EnumEntrustType.NORMAL_LIMIT.getType())){

            event = placeNormalLimitOrder(takerOrder);
        } else if (entrustType.equals(EnumEntrustType.PREMIUM_LIMIT.getType())){
            if (takerOrder.getEntrustProp().equals(EnumEntrustProp.FOK.getType())){

                event = placePremiumLimitOrderOfFOK(takerOrder);
            } else {

                // IOC
                event = placePremiumLimitOrderOfIOC(takerOrder);
            }
        } else {

            // default entrustType: market order
            event = placeMarketOrder(takerOrder);
        }

        BigDecimal totalClinchNum = BigDecimal.ZERO;
        BigDecimal totalClinchTotalBalance = BigDecimal.ZERO;
        List<MatchItemBO> trades = event.getTrades();
        if (!CollectionUtil.isEmpty(trades)){
            for (MatchItemBO trade : trades) {
                totalClinchNum = totalClinchNum.add(trade.getClinchQty());
                totalClinchTotalBalance = totalClinchTotalBalance.add(trade.getClinchAmount());
            }
        }

        return event;


    }


    private MatchItemBO trade(OrderItemBO takerOrder, OrderItemBO makerOrder) {
        BigDecimal marketOrderPrice = makerOrder.getEntrustPrice();
        BigDecimal takerNum;
        if (takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()
                && takerOrder.getEntrustType() == EnumEntrustType.MARKET.getType()){

            takerNum = takerOrder.getRemainEntrustAmount().divide(marketOrderPrice, takerOrder.getQuoteScale(), RoundingMode.DOWN);

        } else {

            takerNum = takerOrder.getRemainEntrustQty();

        }

        if (takerNum.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // minimum of trade number
        BigDecimal clinchNum = takerNum.min(makerOrder.getRemainEntrustQty());
        takerOrder.setRemainEntrustQty(takerOrder.getRemainEntrustQty().subtract(clinchNum));
        makerOrder.setRemainEntrustQty(makerOrder.getRemainEntrustQty().subtract(clinchNum));

        // calc trade balance
        BigDecimal clinchTotalBalance = clinchNum.multiply(marketOrderPrice);
        if (takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()) {
            takerOrder.setRemainEntrustAmount(takerOrder.getRemainEntrustAmount().subtract(clinchTotalBalance));
        } else {
            makerOrder.setRemainEntrustAmount(makerOrder.getRemainEntrustAmount().subtract(clinchTotalBalance));
        }

//        if (makerOrder.getRemainEntrustNum().compareTo(BigDecimal.ZERO) == 0){
//            makerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
//        }
//        if (takerOrder.getRemainEntrustNum().compareTo(BigDecimal.ZERO) == 0) {
//            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
//        }

        MatchItemBO matchItemBO = new MatchItemBO();
        matchItemBO.setProductId(takerOrder.getProductId());
        matchItemBO.setClinchQty(clinchNum);
        matchItemBO.setClinchAmount(clinchTotalBalance);
        matchItemBO.setMakerOrder(makerOrder);
        matchItemBO.setTimestamp(System.currentTimeMillis());
        return matchItemBO;
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
            return MatchResultBO.builder().build();
        }

        // cancel is same direction
        OrderTreemapBO depth = orderItemBO.getEntrustSide()
                == EnumEntrustSide.BUY.getCode() ?
                bid : ask;
        depth.removeOrder(orderItemBO);

        orderItemBO.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());
        return MatchResultBO.builder()
                .takerOrder(orderItemBO)
                .build();
    }



}
