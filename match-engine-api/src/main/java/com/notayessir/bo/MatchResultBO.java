package com.notayessir.bo;

import cn.hutool.core.collection.CollectionUtil;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultBO {


    private Long globalSequence;
    private Long txSequence;
    private Long timestamp;
    private Long coinId;

    private Integer commandType;

    private OrderItemBO takerOrder;
    private List<MatchItemBO> matchItems;


    public BigDecimal addUpClinchQty(){
        BigDecimal qty = BigDecimal.ZERO;
        if (CollectionUtil.isEmpty(matchItems)){
            return qty;
        }
        for (MatchItemBO matchItem : matchItems) {
            qty = qty.add(matchItem.getClinchQty());
        }
        return qty;
    }

    public BigDecimal addUpClinchAmount(){
        BigDecimal amount = BigDecimal.ZERO;
        if (CollectionUtil.isEmpty(matchItems)){
            return amount;
        }
        for (MatchItemBO matchItem : matchItems) {
            amount = amount.add(matchItem.getClinchQty().multiply(matchItem.getClinchPrice()));
        }
        return amount;
    }

}
