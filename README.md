# 介绍
一个基于 Raft 的高可用撮合引擎，支持操作：
- 放置订单；
- 取消订单；

支持的订单类型：
- 市价单；
- 限价单；
- 高级限价单 IOC 和 FOK；

版本要求：
- JDK 17+
# 快速开始
启动服务器：

```
// 分组 id
String groupId = "02511d47-d67c-49a3-9011-abb3109a44c1";  
// 集群地址
List<String> addresses = Arrays.asList("127.0.0.1:18080","127.0.0.1:18081","127.0.0.1:18082"); 
// Raft 日志存储位置
String dirname = "/Users/geek/IdeaProjects/mercury-match-engine/dir";  
for (int i = 0; i < 3; i++) {  
    MatchServerConfig config = MatchServerConfig.builder()  
            .addresses(addresses).dirname(dirname).groupId(groupId).index(i)  
            .publisher(new LogPublisher())  // 撮合结果的异步消费者
            .build();  
    MatchServer matchServer = new MatchServer(config);  
    matchServer.start();  
}
```
启动客户端：
```
// 分组 id
String groupId = "02511d47-d67c-49a3-9011-abb3109a44c1"; 
// 集群地址
List<String> addresses = Arrays.asList("127.0.0.1:18080","127.0.0.1:18081","127.0.0.1:18082");  
MatchClientConfig config = MatchClientConfig.builder()  
        .addresses(addresses).groupId(groupId)  
        .build();  
matchClient = new MatchClient(config);
```
放置订单：
```
Long coinId = 1L;  
MatchCommandBO commandBO = new MatchCommandBO();  
commandBO.setCoinId(coinId);  
commandBO.setCommand(EnumMatchCommand.PLACE.getCode());  
// 买卖方向
commandBO.setEntrustSide(EnumEntrustSide.SELL.getCode());  
// 委托价格
BigDecimal entrustPrice = BigDecimal.valueOf(20.3);  
// 委托数量
BigDecimal entrustQty = BigDecimal.valueOf(20);  
commandBO.setEntrustAmount(entrustAmount);  
commandBO.setEntrustQty(entrustQty);  
commandBO.setEntrustPrice(entrustPrice);  
// 精度
commandBO.setQuoteScale(4);  
// 普通限价
commandBO.setEntrustType(EnumEntrustType.NORMAL_LIMIT.getType());  
commandBO.setOrderId(Math.abs(RandomUtil.randomLong()));  
commandBO.setRequestId(Math.abs(RandomUtil.randomLong()));  
long resp = matchClient.sendSync(commandBO);
```
取消订单：
```
MatchCommandBO commandBO = new MatchCommandBO();  
commandBO.setCommand(EnumMatchCommand.CANCEL.getCode());  
Long requestId = RandomUtil.randomLong();  
Long orderId = RandomUtil.randomLong();  
Long coinId = 1L;  
commandBO.setCoinId(coinId);  
commandBO.setOrderId(orderId);  
commandBO.setRequestId(requestId);  
Long id = matchClient.sendSync(commandBO);
```
# 完整信息
## 整体设计
TODO
## API
TODO
## 部署建议
TODO

# 参考信息
[Bybit 焦点](https://www.aicoin.com/article/128773.html)

[The Raft Consensus](https://raft.github.io/)