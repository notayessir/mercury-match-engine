中文 | [English](README_EN.md)

**学习用途，不具备投入生产的参考价值。**

# 介绍

![process.png](asset%2Fprocess.png)

一个基于 Raft 的高可用撮合引擎，支持操作：
- 放置订单；
- 取消订单；

支持订单类型：
- 市价单；
- 限价单；
- 高级限价单 IOC 和 FOK；

版本要求：
- JDK 17+
# 快速开始
下载并解压：

启动服务器：

```
cd bin
# 启动 
sh command.sh start
# 停止
sh command.sh stop
```

引入客户端：

maven：
```
TODO
```

初始化：
```
String groupId = "02511d47-d67c-49a3-9011-abb3109a44c1";  
List<String> addresses = Arrays.asList("127.0.0.1:18080","127.0.0.1:18081","127.0.0.1:18082");  
MatchClientConfig config = MatchClientConfig.builder()  
        .addresses(addresses).groupId(groupId)  
        .build();  
MatchClient matchClient = new MatchClient(config);
```

放置普通限价单：
```
Long coinId = 1L;  
Long orderId = 1000L;
Long requestId = 2000L;
MatchCommandBO commandBO = new MatchCommandBO();  
// 订单簿 id
commandBO.setCoinId(coinId);  
commandBO.setCommand(EnumMatchCommand.PLACE.getCode());  
// 买卖方向
commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());  
// 委托数量
commandBO.setEntrustQty(BigDecimal.valueOf(20)); 
// 委托价格
commandBO.setEntrustPrice(BigDecimal.valueOf(20.3));  
// quote/bid price 精度
commandBO.setQuoteScale(4);
// 普通限价
commandBO.setEntrustType(EnumEntrustType.NORMAL_LIMIT.getType());  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId);  
matchClient.sendSync(commandBO);
```

取消订单：
```
MatchCommandBO commandBO = new MatchCommandBO();  
commandBO.setCommand(EnumMatchCommand.CANCEL.getCode());  
Long requestId = 3000L;  
commandBO.setCoinId(coinId);  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId);  
matchClient.sendSync(commandBO);
```

同步请求：
```
Long sendSync(MatchCommandBO command) throws Exception
```

异步请求：
```
CompletableFuture<Long> sendAsync(MatchCommandBO command)
```
# 详尽信息

## 服务端
#### 配置文件
通过配置 application.properties 达到预期要求。

集群运行方式及配置：
```
// 1. 运行模式：single 或 group
// single：单机模式，所有撮合节点运行在一个 Spring Boot 进程内
// group：撮合节点分布运行在多个 Spring Boot 进程内
raft.configs[0].mode=group|single
// 2. 集群组 id
raft.configs[0].groupId=02511d47-d67c-49a3-9011-abb3109a44c1
// 3. 集群节点地址和端口，根据 Raft 协议，至少配置 3 个节点
raft.configs[0].addresses=127.0.0.1:28080,127.0.0.1:28081,127.0.0.1:28082
// 4. 当前节点运行的地址和端口
raft.configs[0].targetAddress=127.0.0.1:28080
// 5. raft 日志存储位置
raft.configs[0].storage=/Users/geek/IdeaProjects/mercury-match-engine/dir
```

撮合生产者：
生产者通过实现 Publisher 发送消息，当前内置的生产者：
- log
- Kafka(KafkaTemplate)

```
// 实现 Publisher 接口
public class LogPublisher implements Publisher {  
    @Override  
    public void publish(MatchResultBO matchResultBO) {  
        // 轻量生产逻辑，例如推送到 mq，日志等...
    }  
}
```

撮合结果 MatchResultBO：撮合引擎将撮合的结果异步发布到设定的队列中，数据字段信息如下：
```
{
  "matchItems":[    // 成交的 maker 订单和数据，数组 
    {
      "clinchPrice":20.3,   // 成交单价
      "clinchQty":20,       // 成交数量
      "makerOrder":{        // maker 订单信息
        "entrustAmount":406,    // 委托总价
        "entrustPrice":20.3,    // 委托单价
        "entrustQty":20,        // 委托数量
        "entrustSide":0,        // 委托方向，0 卖 1 买
        "entrustType":2,        // 委托订单类型，限价单
        "matchStatus":20,       // 订单状态
        "orderId":1532783699234128978,  // 订单 id
        "quoteScale":4,                 // 出价货币精度
        "remainEntrustAmount":406,      // 剩余待成交总金额
        "remainEntrustQty":0            // 剩余待成交数量
      },
      "match":true, // 是否成交，冗余字段，均为 true
      "sequence":0  // 在 matchItems 中的索引
    },
    // more items...
  ],
  "takerOrder":{    // taker 订单信息
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
  "txSequence":5,    // 订单簿全局唯一 id（递增）
  "globalSequence": 10,     // 撮合引擎全局唯一 id（递增）
  "timestamp": 166778687576, // 撮合时间戳
  "commandType": 20 // 撮合命令类型，10 cancel，20 place
  "coinId": 20 // 对应某一标的 id
}
```
## 客户端
#### 放置订单

市价单：
```
MatchCommandBO commandBO = new MatchCommandBO();  
commandBO.setCommand(EnumMatchCommand.PLACE.getCode());  
// 订单簿 id
commandBO.setCoinId(coinId);  
// 买卖方向
commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());  
// 购买金额
commandBO.setEntrustAmount(BigDecimal.valueOf(500));
// quote/bid price 精度
commandBO.setQuoteScale(4);  
commandBO.setEntrustType(EnumEntrustType.MARKET.getType());  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId); 
```

IOC 限价单：
```
...
// IOC 高级限价
commandBO.setEntrustType(EnumEntrustType.PREMIUM_LIMIT.getType());  
commandBO.setEntrustProp(EnumEntrustProp.IOC.getType());  
... 
matchClient.sendSync(commandBO);
```

FOK 限价单：
```
...
// FOK 高级限价
commandBO.setEntrustType(EnumEntrustType.PREMIUM_LIMIT.getType());  
commandBO.setEntrustProp(EnumEntrustProp.FOK.getType());  
... 
matchClient.sendSync(commandBO);
```

## 部署建议
- 根据 Raft 算法，至少 3 个节点组成一个撮合集群，这些节点生产上要部署在不同的机器上；
- 若标的交易量很大，可以将多个标的分配在不同的撮合集群上，例如撮合集群 A 负责撮合标的 aa,bb，撮合集群 B 负责撮合标的 dd,ee；
# 参考信息
[Bybit 焦点](https://www.aicoin.com/article/128773.html)

[The Raft Consensus](https://raft.github.io/)