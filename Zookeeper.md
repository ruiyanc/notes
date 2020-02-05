<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [zookeeper](#zookeeper)
- [dubbo](#dubbo)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

### zookeeper

* 中间件，提供协调服务；作用于分布式系统
* 分布式系统的概念
  * 多台计算机组成一个整体，一致对外处理同一个请求
  * 内部的每台计算机都可以相互通信
  * 客户端到服务器的一次请求到响应结束会历经多台计算机
* zookeeper的特性
  * 一致性：数据一致性，数据按照顺序分批入库
  * 原子性：事务要么成功要么失败，不会局部化
  * 单一视图：客户端连接集群中的任一zk节点，数据都是一致的
  * 可靠性：每次对zk的操作状态都会保存在服务端
  * 实时性：客户端可以读取到zk服务端的最新数据
* zoo.cfg配置
  * tickTime：计算时间的单元（时间都以tickTime为单位倍数）
  * dataDir：存放路径，必须配置
* zk基本数据模型
  * 理解为Linux的文件目录，cd进入
  * 每一个节点被称为znode，可以有子节点，也可以有数据
  * 每个节点分为临时节点和永久节点，临时节点在客户端断开后消失
  * 每个zk节点都各自的版本号，可以通过命令行来显示节点信息
  * 每当节点数据发生变化，那么该节点的版本号会叠加（乐观锁）
  * 删除/修改过时节点，版本号不匹配则会报错
  * 每个zk节点存储的数据不宜过大，几k就行
  * 节点可以设置权限acl，可以通过权限来限制用户的访问
* zookeeper数据模型基本操作
  * 客户端连接
  * 查看znode结构
  * 关闭客户端连接
* zk作用体现
  * master节点选举，主节点挂了之后，从节点就会接手工作，并且保证这个节点是唯一的，也就是首脑模式，从而保证集群是高可用的
  * 统一配置文件管理，即只需要部署一台服务器，则可以把相同的配置文件同步更新到其他所有服务器
  * 发布与订阅，发布者把数据存在znode上，订阅者会读取这个数据
  * 提供分布式锁，分布式环境中不同进程直接争夺资源
  * 集群管理，集群中保证数据的强一致性
* zk常用命令行

  * zkCli.sh打开zk客户端
  * ls与ls2，get与stat
  * session的基本原理
    * 客户端与服务端之间的连接存在会话
    * 每个会话都可以设置一个超时时间
    * 心跳结束，session过期
    * session过期，则临时节点znode会被抛弃
    * 心跳机制：客户端向服务端的ping包请求
  * create创建节点，-e临时，-s顺序节点
  * set修改，delete删除 ，（带上version乐观锁）
  * watcher机制
    * 针对每个借点的操作，都会有一个监督者  -> wathcer
    * 当监控的某个对象(znode)发生了变化，则触发watcher事件
    * zk中的watcher是一次性的，触发后立即销毁
    * 父节点、子节点增删改都能触发其watcher
    * watcher命令行
      1. 通过get/set/ls path [watcher]设置watcher
    * watcher事件类型
      1. 创建父节点触发：NodeCreated，修改：NodeDataChanged，删除：NodeDeleted
      2. ls为父节点设置watcher，创建/删除：NodeChildrenChanged
    * watcher使用场景 -> 统一资源配置
* ACL权限控制

  * 针对节点可以设置相关读写等权限，目的为了保障数据安全性

  * 权限permissions可以指定不同的权限范围以及角色

  * ACL命令行

    * getAcl/setAcl：获取/设置某个节点的acl权限信息
    * addauth：输入认证授权信息注册时使用明文登录（系统中自动加密）如addauth digest yy:yy注册用户
    * world:anyone:cdrwa：默认权限
    * digest:user:pwd:cdrwa：加密登录
    * ip:192.xx.x.x:cdrwa：限制ip权限

  * ACL的构成

    * zk的acl通过[scheme:\id:permissions]构成权限列表

      1. scheme：代表采用的某种的权限机制
         * world：world下只有一个id(用户)，也就是anyone，写法为world:anyone:[permissions]
         * auth(`明文`)：代表认证登录，需要注册用户有权限才行，形式为auth:user:password:[permissions]
         * digest(`密文`)：需要对密码加密才能访问，形式为digest:username:BASE64(SHA1(password)):[permissions]
         * ip：设置为指定的ip地址，限制ip进行访问，如192.xxx:[permissions]
         * super：超级管理员，拥有所有的权限。修改zkServer.sh增加super管理员

      2. id代表：允许访问的用户

      3. permissions：权限组合字符串 ->权限字符串缩写crdwa
         * create：创建子节点，read：获取(子)节点，write：设置节点数据
         * delete：删除子节点，admin：设置权限
* zk四字命令 four letter words

  * zk通过nc命令和服务器交互
  * echo [commond] | nc [ip] [port]
  * stat：查看zk状态信息以及是否mode
  * ruok：当前zkServer是否启动返回imok
  * dump：列出未经处理的会话和临时节点
  * conf：查看服务器配置，envi环境变量
  * cons：展示连接到服务器的客户端信息
  * mntr：监控zk健康信息，wchs：展示watch信息
  * wchc和wchp session与watch及path与watch信息
* zookeeper集群

  * zk集群，主从节点，心跳机制
  * 配置数据文件myid1/2/3对应server1/2/3
  * 通过zkCli.sh -server [ip]:[port] 检测集群是否配置成功
* Apache cuator
  * 详见[CuratorOperator.java](https://github.com/ruiyanc/notes/CuratorOperator.java)
###  dubbo

* dubbo简介	
  * 最大程度进行解耦，降低系统耦合性
  * 生产者/消费者模式
  * zk注册中心，admin监控中心，协议支持
