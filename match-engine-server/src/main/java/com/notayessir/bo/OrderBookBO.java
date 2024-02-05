package com.notayessir.bo;

import com.notayessir.constant.EnumEntrustProp;
import com.notayessir.constant.EnumEntrustSide;
import com.notayessir.constant.EnumEntrustType;
import com.notayessir.constant.EnumMatchStatus;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Getter
public class OrderBookBO implements Serializable {


    @Serial
    private static final long serialVersionUID = 3831714965938884709L;
    private Long txSequence ;

    /**
     * order to sell
     */
    private final OrderTreemapBO ask = new OrderTreemapBO(Comparator.naturalOrder());

    /**
     * buy to buy
     */
    private final OrderTreemapBO bid = new OrderTreemapBO(Comparator.reverseOrder());

    private final Map<Long, OrderItemBO> orders = new HashMap<>();

    public OrderBookBO(Long initSequence) {
        this.txSequence = initSequence;
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
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (matchItemBO.isMatch()){
                    orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

                // remove the market order if filled
                if (makerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
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
                if (matchItemBO.isMatch()){
                    orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);

                // remove the market order if filled
                if (makerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
                    orderQueueIterator.remove();
                }

                if (takerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
                    break;
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
            takerOrder.setMatchStatus(EnumMatchStatus.OPEN.getStatus());
            priceDepth = takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? bid : ask;
            priceDepth.addOrder(takerOrder);
            orders.put(takerOrder.getOrderId(), takerOrder);
        }

        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
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
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (matchItemBO.isMatch()){
                    orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);

                // remove the market order if filled
                if (makerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
                    orderQueueIterator.remove();
                }

                if (takerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
                    break;
                }
            }

            // no more order, remove queue
            if (orderQueue.isEmpty()) {
                priceMapIterator.remove();
            }
        }
        takerOrder.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());
        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
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
                MatchItemBO matchItemBO = match(takerOrder, makerOrder);
                if (matchItemBO.isMatch()){
                    orderQueue.subtractRemainEntrustQty(matchItemBO.getClinchQty());
                } else {
                    break BREAKPOINT;
                }

                matchItemBO.setSequence(seq++);
                trades.add(matchItemBO);

                // remove the market order if filled
                if (makerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
                    orderQueueIterator.remove();
                }

                if (takerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus()){
                    break;
                }

            }

            // no more order, remove queue
            if (orderQueue.isEmpty()) {
                priceMapIterator.remove();
            }
        }
        takerOrder.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());
        return MatchResultBO.builder()
                .matchItems(trades).takerOrder(takerOrder)
                .build();
    }

    private OrderItemBO buildOrderItem(MatchCommandBO commandBO) {
        OrderItemBO orderItemBO = new OrderItemBO();

        orderItemBO.setOrderId(commandBO.getOrderId());
        orderItemBO.setEntrustType(commandBO.getEntrustType());
        orderItemBO.setEntrustSide(commandBO.getEntrustSide());
        orderItemBO.setEntrustPrice(commandBO.getEntrustPrice());
        orderItemBO.setEntrustQty(commandBO.getEntrustQty());
        orderItemBO.setEntrustAmount(commandBO.getEntrustAmount());
        orderItemBO.setMatchStatus(EnumMatchStatus.OPEN.getStatus());
        orderItemBO.setRemainEntrustQty(commandBO.getEntrustQty());
        orderItemBO.setRemainEntrustAmount(commandBO.getEntrustAmount());
        orderItemBO.setQuoteScale(commandBO.getQuoteScale());
        return orderItemBO;
    }

    public MatchResultBO place(MatchCommandBO command) {
        MatchResultBO resultBO;
        OrderItemBO order = buildOrderItem(command);
        Integer entrustType = command.getEntrustType();
        Integer entrustProp = command.getEntrustProp();
        if (entrustType == EnumEntrustType.NORMAL_LIMIT.getType()){

            resultBO = placeNormalLimitOrder(order);
        } else if (entrustType == EnumEntrustType.PREMIUM_LIMIT.getType()){
            if (entrustProp == EnumEntrustProp.FOK.getType()){

                resultBO = placePremiumLimitOrderOfFOK(order);
            } else {

                // IOC
                resultBO = placePremiumLimitOrderOfIOC(order);
            }
        } else {

            // default entrustType: market order
            resultBO = placeMarketOrder(order);
        }
        resultBO.setTxSequence(++txSequence);
        return resultBO;


    }


    private MatchItemBO match(OrderItemBO takerOrder, OrderItemBO makerOrder) {
        BigDecimal makerEntrustPrice = makerOrder.getEntrustPrice();
        int quoteScale = takerOrder.getQuoteScale();
        BigDecimal clinchQty;
        if (takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()
                && takerOrder.getEntrustType() == EnumEntrustType.MARKET.getType()){
            clinchQty = takerOrder.getRemainEntrustAmount().divide(makerEntrustPrice, quoteScale, RoundingMode.DOWN);
        } else {
            clinchQty = takerOrder.getRemainEntrustQty();
        }

        if (clinchQty.compareTo(BigDecimal.ZERO) == 0) {
            return new MatchItemBO();
        }

        // minimum of clinch qty
        clinchQty = clinchQty.min(makerOrder.getRemainEntrustQty());
        takerOrder.setRemainEntrustQty(takerOrder.getRemainEntrustQty().subtract(clinchQty));
        makerOrder.setRemainEntrustQty(makerOrder.getRemainEntrustQty().subtract(clinchQty));

        // calc clinch amount
        BigDecimal clinchAmount = clinchQty.multiply(makerEntrustPrice);
        if (takerOrder.getEntrustSide() == EnumEntrustSide.BUY.getCode()) {
            takerOrder.setRemainEntrustAmount(takerOrder.getRemainEntrustAmount().subtract(clinchAmount));
        } else {
            makerOrder.setRemainEntrustAmount(makerOrder.getRemainEntrustAmount().subtract(clinchAmount));
        }

        if (makerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0){
            makerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        }
        if (takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0) {
            takerOrder.setMatchStatus(EnumMatchStatus.FILLED.getStatus());
        }

        MatchItemBO matchItemBO = new MatchItemBO();
        matchItemBO.setClinchQty(clinchQty);
        matchItemBO.setClinchAmount(clinchAmount);
        matchItemBO.setMakerOrder(makerOrder);
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
        OrderTreemapBO depth = orderItemBO.getEntrustSide() == EnumEntrustSide.BUY.getCode() ? bid : ask;
        depth.removeOrder(orderItemBO);
        orderItemBO.setMatchStatus(EnumMatchStatus.CLOSE.getStatus());

        return MatchResultBO.builder()
                .takerOrder(orderItemBO).txSequence(++txSequence)
                .build();

    }



}
