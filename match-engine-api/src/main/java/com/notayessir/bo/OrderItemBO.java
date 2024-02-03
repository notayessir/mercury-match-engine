package com.notayessir.bo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemBO {

    private Long productId;

    /**
     * order id
     */
    private Long orderId;

    /**
     * entrust unit price
     */
    private BigDecimal entrustPrice;

    /**
     * entrust number
     */
    private BigDecimal entrustQty;

    private BigDecimal entrustAmount;

    /**
     * sell or buy
     */
    private Integer entrustSide;

    /**
     * MARKET,LIMIT...
     */
    private Integer entrustType;

    private Integer entrustProp;


    private Long timestamp;

    private BigDecimal remainEntrustQty;
    private BigDecimal remainEntrustAmount;


    private int quoteScale;

    private int matchStatus;

    private Long userId;



}
