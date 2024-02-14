[中文](README.md) | [English](README_EN.md)
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

configure running mode:
```
// single mode, all nodes are running in a same jvm process, mainly for test
raft.server.mode=single
// cluster mode, nodes are running in different jvm process, seperated physically
raft.server.mode=group
```

configure engine cluster:
```
// claim cluster nodes, if there have multi clusters, use "|" to separate each cluster
raft.server.list=127.0.0.1:18080,127.0.0.1:18081,127.0.0.1:18082;1| 
// claim cluster id, use "|" to separate multi clusters, should correspond with raft.server.list
raft.group.list=02511d47-d67c-49a3-9011-abb3109a44c1|  
// raft log position
raft.storage.dir=/Users/geek/Downloads/app/dir
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