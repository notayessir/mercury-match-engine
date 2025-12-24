[中文](README.md) | 英文

**The project is for learning purposes only.**

design thinking: [a match engine](https://github.com/notayessir/blog/blob/main/articles/2024-11-30-A-matching-engine.md)

# Introduction
![process.png](asset%2Fprocess.png)

A match engine based on Raft, support operation：
- place order;
- cancel order;

support order：
- market order;
- limit order;
- premium limit order IOC and FOK;

JDK version required:
- JDK 17+
# Quick Start
download and unzip:

TODO

bootstrap server:

```
cd bin
# start
sh command.sh start
# stop
sh command.sh stop
```

import client library:

maven:
```
TODO
```

initialize client:

```
String groupId = "02511d47-d67c-49a3-9011-abb3109a44c1";  
List<String> addresses = Arrays.asList("127.0.0.1:18080","127.0.0.1:18081","127.0.0.1:18082");  
MatchClientConfig config = MatchClientConfig.builder()  
        .addresses(addresses).groupId(groupId)  
        .build();  
MatchClient matchClient = new MatchClient(config);
```

place a limit order:
```
Long coinId = 1L;  
Long orderId = 1000L;
Long requestId = 2000L;
MatchCommandBO commandBO = new MatchCommandBO();  
// order book id
commandBO.setCoinId(coinId);  
commandBO.setCommand(EnumMatchCommand.PLACE.getCode());  
// entrust side
commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());  
// entrust qty
commandBO.setEntrustQty(BigDecimal.valueOf(20)); 
// entrust price
commandBO.setEntrustPrice(BigDecimal.valueOf(20.3));  
// quote/bid price scale
commandBO.setQuoteScale(4);
// claim order type
commandBO.setEntrustType(EnumEntrustType.NORMAL_LIMIT.getType());  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId);  
matchClient.sendSync(commandBO);
```

cancel a order:
```
MatchCommandBO commandBO = new MatchCommandBO();  
commandBO.setCommand(EnumMatchCommand.CANCEL.getCode());  
Long requestId = 3000L;  
commandBO.setCoinId(coinId);  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId);  
matchClient.sendSync(commandBO);
```

synchronize request:
```
Long sendSync(MatchCommandBO command) throws Exception
```

async request:
```
CompletableFuture<RaftClientReply> sendAsync(MatchCommandBO command)
```
# More Detail

## Server
#### Configuration File
edit application.properties to meet your expectation.

configure running mode and cluster：
```
// 1. running mode：single or group
// single：only for test, all nodes are running in a same process
// group：for product, nodes are running in different processes
raft.configs[0].mode=group|single
// 2. group id
raft.configs[0].groupId=02511d47-d67c-49a3-9011-abb3109a44c1
// 3. raft group addresses, at least 3
raft.configs[0].addresses=127.0.0.1:28080,127.0.0.1:28081,127.0.0.1:28082
// 4. current running address
raft.configs[0].targetAddress=127.0.0.1:28080
// 5. raft log storage position
raft.configs[0].storage=/Users/geek/IdeaProjects/mercury-match-engine/dir
```

producer for match result:

implement Publisher interface to get a producer, list below are available implementations:
- log
- Kafka(KafkaTemplate)

```
// implement Publisher interface 
public class LogPublisher implements Publisher {  
    @Override  
    public void publish(MatchResultBO matchResultBO) {  
        // light-weight implement, push result to mq, etc...
    }  
}
```

match result object, MatchResultBO: match engine publishes match result to the destination once command is executed, fields are below:
```
{
  "matchItems":[    // A arr with maker order info
    {
      "clinchPrice":20.3,   // clinch price
      "clinchQty":20,       // clinch quantity
      "makerOrder":{        // maker order info
        "entrustAmount":406,    // total entrust amount
        "entrustPrice":20.3,    // entrust price
        "entrustQty":20,        // entrust quantity
        "entrustSide":0,        // order side, 0 sell 1 buy
        "entrustType":2,        // order type, market, limit, maker order is always limit order
        "matchStatus":20,       // order status
        "orderId":1532783699234128978,  // order id
        "quoteScale":4,                 // bid currency scale
        "remainEntrustAmount":406,      // to clinch amount
        "remainEntrustQty":0            // to clinch quantity
      },
      "match":true, // always true
      "sequence":0  // index in matchItems
    },
    // more items...
  ],
  "takerOrder":{    // taker order info
    "entrustAmount":609,
    "entrustPrice":20.3,
    "entrustQty":30,
    "entrustSide":1,
    "entrustType":2,
    "matchStatus":20,
    "orderId":6369070926336026940,
    "quoteScale":4,
    "remainEntrustAmount":609,
    "remainEntrustQty":0
  },
  "txSequence":5,    // unique id in order book (incremental)
  "globalSequence": 10,     // unique id in match engine (incremental)
  "timestamp": 166778687576, // match timestamp
  "commandType": 20 // command type , 10 means cancel, 20 means place
  "coinId": 20 // coin id claim in your database
}
```
## Client
#### Place Order

Market Order：
```
MatchCommandBO commandBO = new MatchCommandBO();  
commandBO.setCommand(EnumMatchCommand.PLACE.getCode());  
commandBO.setCoinId(coinId);  
commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());  
commandBO.setEntrustAmount(BigDecimal.valueOf(500));
commandBO.setQuoteScale(4);  
commandBO.setEntrustType(EnumEntrustType.MARKET.getType());  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId); 
```

IOC Order：
```
...
commandBO.setEntrustType(EnumEntrustType.PREMIUM_LIMIT.getType());  
commandBO.setEntrustProp(EnumEntrustProp.IOC.getType());  
... 
matchClient.sendSync(commandBO);
```

FOK Order：
```
...
commandBO.setEntrustType(EnumEntrustType.PREMIUM_LIMIT.getType());  
commandBO.setEntrustProp(EnumEntrustProp.FOK.getType());  
... 
matchClient.sendSync(commandBO);
```

## Suggestions
- depends on Raft, at least 3 nodes form a match engine cluster, each node should develop on different physical machine so that we can keep a high-available system;
- if transactions are large may cause a high latency, considering separate different coins into different engine cluster; for example, cluster A match coin Z and X, cluster B match coin R and Y;
# Other Information
[Bybit Focus](https://www.aicoin.com/article/128773.html)

[The Raft Consensus](https://raft.github.io/)
