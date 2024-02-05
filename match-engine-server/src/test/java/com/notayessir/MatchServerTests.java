package com.notayessir;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.notayessir.bo.MatchCommandBO;
import com.notayessir.bo.MatchResultBO;
import com.notayessir.bo.OrderBookBO;
import com.notayessir.bo.OrderItemBO;
import com.notayessir.constant.EnumEntrustSide;
import com.notayessir.constant.EnumEntrustType;
import com.notayessir.constant.EnumMatchCommand;
import com.notayessir.constant.EnumMatchStatus;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Objects;

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
        MatchResultBO resultBO = orderBookBO.cancel(RandomUtil.randomLong());
        Assumptions.assumeTrue(Objects.isNull(resultBO.getTxSequence()));
    }


    @Test
    @DisplayName("place limit order")
    void placeLimitOrder() {
        Long coinId = 1L;
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // makerOrder 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty1 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        MatchCommandBO command1 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice1, entrustQty1, entrustAmount1,
                 EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        MatchResultBO resultBO = orderBookBO.place(command1);
        Assumptions.assumeTrue(Objects.nonNull(resultBO.getTxSequence()));

        // makerOrder 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        MatchCommandBO command2 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice2, entrustQty2, entrustAmount2,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        resultBO = orderBookBO.place(command2);
        Assumptions.assumeTrue(Objects.nonNull(resultBO.getTxSequence()));

        // takerOrder 1
        BigDecimal entrustPrice3 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty3 = BigDecimal.valueOf(30);
        BigDecimal entrustAmount3 = entrustPrice3.multiply(entrustQty3);
        MatchCommandBO command3 = new MatchCommandBO( EnumEntrustSide.BUY.getCode(), entrustPrice3, entrustQty3, entrustAmount3,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        resultBO = orderBookBO.place(command3);
        Assumptions.assumeTrue(Objects.nonNull(resultBO.getTxSequence()));
        Assumptions.assumeTrue(CollectionUtil.isNotEmpty(resultBO.getMatchItems()));
    }





}
