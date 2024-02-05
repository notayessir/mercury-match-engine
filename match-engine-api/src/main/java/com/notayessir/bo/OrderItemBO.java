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
//    private Long coinId;

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

//    private Integer entrustProp;


//    private Long timestamp;

    private BigDecimal remainEntrustQty;
    private BigDecimal remainEntrustAmount;


    private Integer quoteScale;

    private Integer matchStatus;



}
