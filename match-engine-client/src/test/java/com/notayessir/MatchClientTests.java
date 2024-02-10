package com.notayessir;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.MatchCommandBO;
import com.notayessir.constant.EnumEntrustSide;
import com.notayessir.constant.EnumEntrustType;
import com.notayessir.constant.EnumMatchCommand;
import com.notayessir.ex.MatchEngineException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.protocol.RaftClientReply;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


@Slf4j
public class MatchClientTests {

    private static MatchClient matchClient;
    @BeforeAll
    static void initAll() {
        String groupId = "02511d47-d67c-49a3-9011-abb3109a44c1";
        List<String> addresses = Arrays.asList("127.0.0.1:18080","127.0.0.1:18081","127.0.0.1:18082");
        MatchClientConfig config = MatchClientConfig.builder()
                .addresses(addresses).groupId(groupId)
                .build();
        matchClient = new MatchClient(config);
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        matchClient.close();
    }

    @BeforeEach
    void beforeEach() {}
    @AfterEach
    void afterEach() {}


    @Test
    @DisplayName("placeLimitOrder")
    void placeLimitOrder() throws Exception {
        Long coinId = 30L;
        MatchCommandBO commandBO = new MatchCommandBO();
        commandBO.setCoinId(coinId);
        commandBO.setCommand(EnumMatchCommand.PLACE.getCode());
        commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());
        BigDecimal entrustPrice = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty = BigDecimal.valueOf(20);
        BigDecimal entrustAmount = entrustPrice.multiply(entrustQty);
        commandBO.setEntrustAmount(entrustAmount);
        commandBO.setEntrustQty(entrustQty);
        commandBO.setEntrustPrice(entrustPrice);
        commandBO.setQuoteScale(4);
        commandBO.setEntrustType(EnumEntrustType.NORMAL_LIMIT.getType());
        long reqId = Math.abs(RandomUtil.randomLong());
        for (long i = 0; i < 2; i++) {
            commandBO.setOrderId(Math.abs(RandomUtil.randomLong()));
            commandBO.setRequestId(reqId);
            Long id = matchClient.sendSync(commandBO);
            System.out.println(id);
            Assumptions.assumeTrue(id > 0);
        }
    }

    @Test
    @DisplayName("placeLimitOrderAsync")
    void placeLimitOrderAsync() throws Exception {
        Long coinId = 30L;
        MatchCommandBO commandBO = new MatchCommandBO();
        commandBO.setCoinId(coinId);
        commandBO.setCommand(EnumMatchCommand.PLACE.getCode());
        commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());
        BigDecimal entrustPrice = BigDecimal.valueOf(20.3);
        BigDecimal entrustQty = BigDecimal.valueOf(20);
        BigDecimal entrustAmount = entrustPrice.multiply(entrustQty);
        commandBO.setEntrustAmount(entrustAmount);
        commandBO.setEntrustQty(entrustQty);
        commandBO.setEntrustPrice(entrustPrice);
        commandBO.setQuoteScale(4);
        commandBO.setEntrustType(EnumEntrustType.NORMAL_LIMIT.getType());
        long reqId = Math.abs(RandomUtil.randomLong());
        for (long i = 0; i < 2; i++) {
            commandBO.setOrderId(Math.abs(RandomUtil.randomLong()));
            commandBO.setRequestId(reqId);
            matchClient.sendSync(commandBO);
//            CompletableFuture<RaftClientReply> future = matchClient.sendAsync(commandBO);
//            future.whenComplete(new BiConsumer<RaftClientReply, Throwable>() {
//                @Override
//                public void accept(RaftClientReply reply, Throwable throwable) {
//
//                    if (Objects.isNull(throwable)){
//                        System.out.println(reply);
//                    } else {
//                        System.out.println(throwable);
//                        System.out.println(throwable.getMessage());
//                        System.out.println(throwable.getCause().getClass().getName());
//                        System.out.println(throwable.getCause().getMessage());
//                        System.out.println(JSONObject.toJSONString(throwable.getStackTrace()));
//
//                    }
//                }
//            });

            Thread.sleep(3000L);
        }
    }



    @Test
    @DisplayName("checkIdempotent")
    void checkIdempotent() throws Exception {
        MatchCommandBO commandBO = new MatchCommandBO();
        commandBO.setCommand(EnumMatchCommand.CANCEL.getCode());
        Long requestId = RandomUtil.randomLong();
        Long orderId = RandomUtil.randomLong();
        Long coinId = 1L;
        commandBO.setCoinId(coinId);
        commandBO.setOrderId(orderId);
        commandBO.setRequestId(requestId);
        Long id = matchClient.sendSync(commandBO);
        Assumptions.assumeTrue(id == -1L);
        for (long i = 0; i < 10; i++) {
            commandBO.setOrderId(orderId);
            commandBO.setRequestId(requestId);
            id = matchClient.sendSync(commandBO);
            Assumptions.assumeTrue(id == -2L);
        }
    }






}
