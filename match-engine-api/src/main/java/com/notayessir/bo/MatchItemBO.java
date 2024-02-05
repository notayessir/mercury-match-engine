package com.notayessir.bo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class MatchItemBO {

    private transient boolean match;

    private BigDecimal clinchQty;
    private BigDecimal clinchAmount;

    private Long sequence;

    private OrderItemBO makerOrder;















}
