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
public class MatchCommandBO implements Serializable {


    @Serial
    private static final long serialVersionUID = 3447953295099318138L;
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

    private Long coinId;

    private Long requestId;

    private Long orderId;

    private int quoteScale;
    private Integer command;


}
