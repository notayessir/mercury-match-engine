package com.notayessir.bo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;


@Getter
@Setter
public class MatchCommandBO implements Serializable {

    private static final long serialVersionUID = 20000L;

    private Integer entrustSide;

    /**
     * entrust price
     */
    private BigDecimal entrustPrice;

    /**
     * entrust qty
     */
    private BigDecimal entrustQty;

    private BigDecimal entrustAmount;

    private Integer entrustType;

    private Integer entrustProp;

//    private Long timestamp;

    private Long coinId;

    private Long requestId;

    private Long orderId;

    private int quoteScale;
    private Integer command;


}
