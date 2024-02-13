package com.notayessir.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemBO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1489819214314889774L;

    /**
     * order id
     */
    private Long orderId;

    /**
     * entrust unit price
     */
    private BigDecimal entrustPrice;

    /**
     * buy by qty
     */
    private BigDecimal entrustQty;
    private BigDecimal remainEntrustQty;

    /**
     * buy by amount
     */
    private BigDecimal entrustAmount;
    private BigDecimal remainEntrustAmount;

    /**
     * sell or buy
     */
    private Integer entrustSide;

    /**
     * MARKET,LIMIT...
     */
    private Integer entrustType;


    private Integer quoteScale;

    private Integer matchStatus;



}
