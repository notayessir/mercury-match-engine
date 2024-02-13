package com.notayessir;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.*;
import com.notayessir.constant.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
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
    @DisplayName("case1: cancel a not existed order")
    void case1() {
        OrderBookBO orderBookBO = new OrderBookBO(0L);
        MatchResultBO resultBO = orderBookBO.cancel(RandomUtil.randomLong());
        Assumptions.assumeTrue(Objects.isNull(resultBO.getTxSequence()));
    }


    @Test
    @DisplayName("case 2: place a limit order")
    void case2() {
        Long coinId = 1L;
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // maker limit order 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty1 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        MatchCommandBO command1 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice1, entrustQty1, entrustAmount1,
                 EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        MatchResultBO resultBO = orderBookBO.place(command1);
        Assumptions.assumeTrue(Objects.nonNull(resultBO.getTxSequence()));

        // maker limit order 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        MatchCommandBO command2 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice2, entrustQty2, entrustAmount2,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        resultBO = orderBookBO.place(command2);
        Assumptions.assumeTrue(Objects.nonNull(resultBO.getTxSequence()));

        // taker limit order 1
        BigDecimal entrustPrice3 = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty3 = BigDecimal.valueOf(30);
        BigDecimal entrustAmount3 = entrustPrice3.multiply(entrustQty3);
        MatchCommandBO command3 = new MatchCommandBO( EnumEntrustSide.BUY.getCode(), entrustPrice3, entrustQty3, entrustAmount3,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        resultBO = orderBookBO.place(command3);

        OrderItemBO takerOrder = resultBO.getTakerOrder();
        Assumptions.assumeTrue(takerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus());
        List<MatchItemBO> matchItems = resultBO.getMatchItems();
    }



    @Test
    @DisplayName("case 3: place a market order")
    void case3() {
        Long coinId = 1L;
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // maker limit order 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(33.33);
        BigDecimal entrustQty1 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        MatchCommandBO command1 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice1, entrustQty1, entrustAmount1,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        MatchResultBO resultBO = orderBookBO.place(command1);

        // maker limit order 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(43.33);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        MatchCommandBO command2 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice2, entrustQty2, entrustAmount2,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        resultBO = orderBookBO.place(command2);

        // taker limit order 1
        BigDecimal entrustAmount3 = BigDecimal.valueOf(700);
        MatchCommandBO command3 = new MatchCommandBO( EnumEntrustSide.BUY.getCode(), BigDecimal.ZERO, BigDecimal.ZERO, entrustAmount3,
                EnumEntrustType.MARKET.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        resultBO = orderBookBO.place(command3);

        OrderItemBO takerOrder = resultBO.getTakerOrder();
        Assumptions.assumeTrue(takerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus());

        List<MatchItemBO> matchItems = resultBO.getMatchItems();
    }


    @Test
    @DisplayName("case 4: place a IOC order")
    void case4() {
        Long coinId = 1L;
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // maker limit order 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(33.33);
        BigDecimal entrustQty1 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        MatchCommandBO command1 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice1, entrustQty1, entrustAmount1,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        orderBookBO.place(command1);

        // maker limit order 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(33.34);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        MatchCommandBO command2 = new MatchCommandBO( EnumEntrustSide.SELL.getCode(), entrustPrice2, entrustQty2, entrustAmount2,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        orderBookBO.place(command2);

        // taker limit order 1
        BigDecimal entrustPrice3 = BigDecimal.valueOf(34);
        BigDecimal entrustQty3 = BigDecimal.valueOf(100);
        MatchCommandBO command3 = new MatchCommandBO(EnumEntrustSide.BUY.getCode(), entrustPrice3, entrustQty3, BigDecimal.ZERO,
                EnumEntrustType.PREMIUM_LIMIT.getType(), EnumEntrustProp.IOC.getType(), coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        MatchResultBO resultBO = orderBookBO.place(command3);

        OrderItemBO takerOrder = resultBO.getTakerOrder();
        Assumptions.assumeTrue(takerOrder.getMatchStatus() == EnumMatchStatus.CLOSE.getStatus());

    }


    @Test
    @DisplayName("place a FOK order: does not meet condition")
    void case5() {
        Long coinId = 1L;
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // maker limit order 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(33.33);
        BigDecimal entrustQty1 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        MatchCommandBO command1 = new MatchCommandBO(EnumEntrustSide.SELL.getCode(), entrustPrice1, entrustQty1, entrustAmount1,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        orderBookBO.place(command1);

        // maker limit order 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(43.33);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        MatchCommandBO command2 = new MatchCommandBO(EnumEntrustSide.SELL.getCode(), entrustPrice2, entrustQty2, entrustAmount2,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        orderBookBO.place(command2);

        // taker limit order 1
        BigDecimal entrustPrice3 = BigDecimal.valueOf(34);
        BigDecimal entrustQty3 = BigDecimal.valueOf(100);
        MatchCommandBO command3 = new MatchCommandBO(EnumEntrustSide.BUY.getCode(), entrustPrice3, entrustQty3, BigDecimal.ZERO,
                EnumEntrustType.PREMIUM_LIMIT.getType(), EnumEntrustProp.FOK.getType(), coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        MatchResultBO resultBO = orderBookBO.place(command3);

        OrderItemBO takerOrder = resultBO.getTakerOrder();
        Assumptions.assumeTrue(takerOrder.getMatchStatus() == EnumMatchStatus.CLOSE.getStatus());
        Assumptions.assumeTrue(takerOrder.getRemainEntrustQty().compareTo(takerOrder.getEntrustQty()) == 0);

    }


    @Test
    @DisplayName("place a FOK order: meet condition")
    void case6() {
        Long coinId = 1L;
        OrderBookBO orderBookBO = new OrderBookBO(0L);

        // maker limit order 1
        BigDecimal entrustPrice1 = BigDecimal.valueOf(33.33);
        BigDecimal entrustQty1 = BigDecimal.valueOf(100);
        BigDecimal entrustAmount1 = entrustPrice1.multiply(entrustQty1);
        MatchCommandBO command1 = new MatchCommandBO(EnumEntrustSide.SELL.getCode(), entrustPrice1, entrustQty1, entrustAmount1,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        orderBookBO.place(command1);

        // maker limit order 2
        BigDecimal entrustPrice2 = BigDecimal.valueOf(43.33);
        BigDecimal entrustQty2 = BigDecimal.valueOf(20);
        BigDecimal entrustAmount2 = entrustPrice2.multiply(entrustQty2);
        MatchCommandBO command2 = new MatchCommandBO(EnumEntrustSide.SELL.getCode(), entrustPrice2, entrustQty2, entrustAmount2,
                EnumEntrustType.NORMAL_LIMIT.getType(), null, coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        orderBookBO.place(command2);

        // taker limit order 1
        BigDecimal entrustPrice3 = BigDecimal.valueOf(33.33);
        BigDecimal entrustQty3 = BigDecimal.valueOf(100);
        MatchCommandBO command3 = new MatchCommandBO(EnumEntrustSide.BUY.getCode(), entrustPrice3, entrustQty3, BigDecimal.ZERO,
                EnumEntrustType.PREMIUM_LIMIT.getType(), EnumEntrustProp.FOK.getType(), coinId, Math.abs(RandomUtil.randomLong()), Math.abs(RandomUtil.randomLong()), 4, EnumMatchCommand.PLACE.getCode());
        MatchResultBO resultBO = orderBookBO.place(command3);

        OrderItemBO takerOrder = resultBO.getTakerOrder();
        Assumptions.assumeTrue(takerOrder.getMatchStatus() == EnumMatchStatus.FILLED.getStatus());
        Assumptions.assumeTrue(takerOrder.getRemainEntrustQty().compareTo(BigDecimal.ZERO) == 0);

    }


}
