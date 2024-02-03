package com.notayessir.bo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class MatchResultBO {

    private long globalSequence;

    private long txSequence;

    private Integer commandType;

    private OrderItemBO takerOrder;

    private BigDecimal totalClinchNum;

    private BigDecimal totalClinchTotalBalance;

    private List<MatchItemBO> trades;

    private Long timestamp;

}
