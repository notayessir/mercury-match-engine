[中文](README.md) | [English](README_EN.md)
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

撮合集群运行模式：
```
// 单机模式，所有撮合节点运行在一个 Spring Boot 进程内
raft.server.mode=single
// 集群模式，撮合节点分布运行在多个 Spring Boot 进程内
raft.server.mode=group
```

撮合集群配置：
```
// 撮合集群监听端口，若存在多个集群，用标识符 "|" 分开
raft.server.list=127.0.0.1:18080,127.0.0.1:18081,127.0.0.1:18082;1| 
// 撮合集群 id，若存在多个集群，用标识符 "|" 分开
raft.group.list=02511d47-d67c-49a3-9011-abb3109a44c1|  
// raft 日志存储绝对位置
raft.storage.dir=/Users/geek/Downloads/app/dir
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