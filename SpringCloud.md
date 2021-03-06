<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Spring Cloud](#spring-cloud)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

### Spring Cloud

1. 微服务的协调者
2. 什么是SpringCloud
   
   * 配置管理，服务注册，服务发现，智能路由，微代理，负载均衡，断路器，一次性令牌，服务间调用，控制总线，全局锁，领导选举，分布式会话，集群状态，思维导图模板，分布式消息
3. SpringCloud与SpringBoot的关系
   
   * SpringBoot是构建SpringCloud架构的基石
4. 微服务的注册与发现
   1. 访问服务
      1. 通过URI来访问服务
   2. 使用Eureka
      1. 服务注册和发现机制，高可用性，开源
      2. 和SpringCloud无缝集成
   3. 如何集成Eureka Server/Clinet
5. 微服务的消费模式

   1. 服务直连模式
   2. 客户端发现模式
      1. 服务实例启动后，将位置信息提交到服务注册表
      2. 客户端从服务注册表进行查询，来获取可用的服务实例
      3. 客户端自行使用负载均衡从多个服务器实例中选择出一个
   3. 消费者：httpclient -- ribbon -- feign
6. API网关
   1. 好处：避免将内部信息泄露给外部，为微服务添加额外的安全层，支持混合通信协议，降低构建微服务的复杂性，微服务模拟与虚拟化
   2. Zuul：提供了认证、鉴权、限流、动态路由、监控、弹性、安全、负载均衡、协助单点压测、静态相应等边缘服务的框架
      1. @EnableZuulProxy
      2. zuul.routes.xxx.path: /xxx/**
      3. zuul.routes.xx.sericeId: 名称
7. 集中化配置
   1. 配置中心：面向可配置的编码，隔离性，一致性，集中化配置
   2. SpringCloudConfig
      1. 分布式外部化配置
      2. 集成configServer
8. 熔断机制
   1. 什么是服务的熔断机制？
      1. 对该服务的调用执行熔断，对于后续请求，不在继续调用该目标服务，而是直接返回，从而可以快速释放资源。保护系统
   2. 服务熔断：断路器，断路器模式
   3. 熔断的意义：系统稳定，减少性能损耗，及时响应，阀值可定制
   4. 熔断器的功能：异常处理，日志记录，测试失败的操作，手动复位，并发，加速断路，重试失败请求
   5. 熔断与降级的区别
      1. 触发条件不同，管理目标的层次不同
   6. 集成Hystrix
      1. @EnableCircuitBreaker -> @HystrixCommand
9. 自动扩展
   1. 意义：提高了高可用性和容错能力；增加了可伸缩性；具有最佳使用率，并节约成本；优先考虑某些服务或服务组
   2. 常见模式：消息长度，业务参数，根据预测
   3. 容器编排职责：集群管理，自动部署，可伸缩性，运行状况监控，基础架构抽象，资源优化，资源分配，服务可用性，敏捷性，隔离
   4. 容器编排技术：DockerSwarm， Kubernetes，ApacheMesos