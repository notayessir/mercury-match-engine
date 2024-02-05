package com.notayessir;

import cn.hutool.core.util.RandomUtil;
import com.notayessir.bo.MatchCommandResultBO;
import com.notayessir.bo.OrderBookBO;
import com.notayessir.bo.OrderItemBO;
import com.notayessir.constant.EnumEntrustSide;
import com.notayessir.constant.EnumEntrustType;
import com.notayessir.constant.EnumMatchStatus;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

public class MatchServerTests {

    @BeforeAll
    static void initAll() {
    }

    @BeforeEach
    void init() {
    }


    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void tearDownAll() {
    }

    @Test
    @DisplayName("cancel a not existed order")
    void cancelOrder() {
        OrderBookBO orderBookBO = new OrderBookBO(0L);
        MatchCommandResultBO resultBO = orderBookBO.cancel(RandomUtil.randomLong());
        Assumptions.assumeTrue(!resultBO.isSuccess());
    }


    @Test
    @DisplayName("place limit order")
    void placeLimitOrder() {
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // makerOrder 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty1 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        OrderItemBO order1 = new OrderItemBO(1L, Math.abs(RandomUtil.randomLong()), entrustPrice1,
                entrustQty1, entrustAmount1, EnumEntrustSide.SELL.getCode(), EnumEntrustType.NORMAL_LIMIT.getType(), null, entrustQty1, entrustAmount1, 4, EnumMatchStatus.OPEN.getStatus());
        MatchCommandResultBO resultBO = orderBookBO.place(order1);
        Assumptions.assumeTrue(resultBO.isSuccess());

        // makerOrder 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        OrderItemBO order2 = new OrderItemBO(1L, Math.abs(RandomUtil.randomLong()), entrustPrice2,
                entrustQty2, entrustAmount2, EnumEntrustSide.SELL.getCode(), EnumEntrustType.NORMAL_LIMIT.getType(), null, entrustQty2, entrustAmount2, 4, EnumMatchStatus.OPEN.getStatus());
        resultBO = orderBookBO.place(order2);
        Assumptions.assumeTrue(resultBO.isSuccess());

        // takerOrder 1
        BigDecimal entrustPrice3 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty3 = BigDecimal.valueOf(30);
        BigDecimal entrustAmount3 = entrustPrice3.multiply(entrustQty3);
        OrderItemBO order3 = new OrderItemBO(1L, Math.abs(RandomUtil.randomLong()), entrustPrice3,
                entrustQty3, entrustAmount3, EnumEntrustSide.BUY.getCode(), EnumEntrustType.NORMAL_LIMIT.getType(), null, entrustQty3, entrustAmount3, 4, EnumMatchStatus.OPEN.getStatus());
        resultBO = orderBookBO.place(order3);
        Assumptions.assumeTrue(resultBO.isSuccess());
        Assumptions.assumeTrue(!resultBO.getTrades().isEmpty());
    }





}
