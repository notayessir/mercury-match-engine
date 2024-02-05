package com.notayessir.bo;

import lombok.*;

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

    private Integer commandType;

    private OrderItemBO takerOrder;
    private List<MatchItemBO> matchItems;



}
