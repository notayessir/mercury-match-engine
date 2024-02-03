package com.notayessir.bo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;


@Getter
@Setter
@Builder
public class MatchCommandBO implements Serializable {

    private static final long serialVersionUID = 20000L;

    private Integer entrustSide;

    /**
     * entrust unit price
     */
    private BigDecimal entrustPrice;

    /**
     * entrust number
     */
    private BigDecimal entrustQty;

    private BigDecimal entrustAmount;

    private Integer entrustType;

    private Integer entrustProp;

    protected Long timestamp;

    protected Long coinId;

    protected Long requestId;

    protected Long orderId;

    private int quoteScale;
    protected Integer command;

    protected Long userId;
    protected Long quoteAccountId;

}
