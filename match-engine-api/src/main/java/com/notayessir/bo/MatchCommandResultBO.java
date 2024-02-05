package com.notayessir.bo;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCommandResultBO {

    private transient boolean success;

//    private long globalSequence;

    private long txSequence;

    private Integer commandType;

    private OrderItemBO takerOrder;

//    private BigDecimal totalClinchNum;

//    private BigDecimal totalClinchTotalBalance;

    private List<MatchItemBO> trades;

    private Long timestamp;

}
