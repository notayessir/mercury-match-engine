package com.notayessir.bo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;


@Getter
@Setter
public class MatchItemBO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6120849803225177460L;

    private BigDecimal clinchQty;
//    private BigDecimal clinchAmount;
    private BigDecimal clinchPrice;


    private Long sequence;

    private OrderItemBO makerOrder;

    public boolean isMatch(){
        return Objects.nonNull(makerOrder);
    }















}
