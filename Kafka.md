<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Kafka](#kafka)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Kafka

* 分布式的发布-订阅消息系统。将消息持久化到磁盘中，并对消息创建了备份保证了数据的安全。
* 特性：
  1. `高吞吐量`、低延迟
  2. `可扩展性`
  3. 持久性，`可靠性`
  4. `容错性`
  5. 高并发
* 使用场景：
  1. 日志收集
  2. 消息系统
  3. 用户活动跟踪
  4. 运营指标
  5. 流式处理
* 先安装Zookeeper，再安装Kafka
* kafka测试消息生产与消费
  * bin/kafka-topics.sh --zookeeper localhost:2181 -create --topic yanrui --partitions 2 --replication-factor 1 -->创建一个主题
    * --zookeeper：制定kafka连接的zookeeper服务地址
    * --topic：所要创建主题的名称
    * --partitions：指定分区个数
    * --replication-factor：指定副本因子
    * --create：才能主题的动作指令
  * bin/kafka-topics.sh --zookeeper localhost:2181 --list -->展示所有主题
  * bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic yanrui -->查看主题详情
  * bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic yanrui --> 启动消费端接收消息
  * bin/kafka-console-producer.sh --broker-list localhost:9092 --topic yanrui -->生产端发送消息
* 位移提交
  * 重复消费
  * 消息丢失
  * 自动提交
  * 同步提交
  * 异步提交
* zookeeper集群搭建
    * 三个echo "1" > myid
    * 服务检查 -> netstat -nlpt | grep -E "2181|2888|3888" 
* kafka集群搭建
	*  修改server.properties
		*  broker.id=1
		* listeners=PLAINTEXT://ip:9092
		* advertised.listeners=PLAINTEXT://ip:9092
		* zookeeper.connect