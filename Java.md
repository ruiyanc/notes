<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [java](#java)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

### java

1. 作用域public,private,protected,default的区别
   
   * |  作用域   | 当前类 | 同一包 | 子孙类 | 其它包 |
     | :-------: | :----: | :----: | :----: | :----: |
     |  public   |   √    |   √    |   √    |   √    |
     | protected |   √    |   √    |   √    |   ×    |
     |  default  |   √    |   √    |   ×    |   ×    |
     |  private  |   √    |   ×    |   ×    |   ×    |
   
2. 八大基础类型及其默认值

   * |     数据类型      |  位  | 字节 |  封装类   | 默认值 |
     | :---------------: | :--: | :--: | :-------: | :----: |
     |    byte（位）     |  8   |  1   |   Byte    |   0    |
     |  short（短整数）  |  16  |  2   |   Short   |   0    |
     |    int（整数）    |  32  |  4   | Interger  |   0    |
     |  long（长整数）   |  64  |  8   |   Long    |   0L   |
     |  float（单精度）  |  32  |  4   |   Float   |  0.0f  |
     | double（双精度）  |  64  |  8   |  Double   |  0.0d  |
     |   char（字符）    |  16  |  2   | Chatacter |   空   |
     | boolean（逻辑型） |  8   |      |  Boolean  | false  |

3. 字符型常量(char)和字符串常量(String)的区别?

   * 形式上:char是' '单引号的一个字符,String是" "双引号的若干个字符
   * 含义上:char相当于一个整型值(ASCII值),String代表一个地址值(在内存中存放位置)
   * 占内存大小:char只占２个字节,String占若干个字节(至少一个字符结束标志)

4. String,StringBuffer和StringBuilder的区别是什么？String为什么不可变?

   * 可变性:String使用final关键字修饰字符数组来保存字符串(private final char value[]),所以不可变;StringBuffer和StringBuilder继承子AbstractStringBuilder类,没用final修饰所以可变.
   * 线程安全性:String不可变即为常量,线程安全;StringBuffer对方法加了同步锁,所以线程安全；StringBuilder非线程安全.
   * 操作少量数据:适用String;单线程操作字符串缓存区下大量数据:StringBuiler.多线程:StringBuffer.

5. 接口和抽象类的区别？

   * 接口默认是public,只能有抽象方法和常量;抽象类中可以有非抽象方法.
   * 一个类可以实现多个接口,但只能继承一个抽象类;接口可通过extends扩展多个接口.
   * 抽象类是类的抽象,是一种模板设计;接口是行为的抽象,一种行为的规范.

6. 重载和重写的区别

   * 重载是同样的一个方法能够根据输入数据的不同，做出不同的处理。
   * 重写是当子类继承自父类的相同方法，输入数据一样，但要做出有别于父类的响应时，你就要覆盖父类方法。

7. ==与equals

   * ==:判断两个对象的地址是否相等(基础类型比较值,引用类型比较内存地址)
   * equals:判断两个对象的值是否相等

8. BIO,NIO和AIO

   * BIO:同步阻塞模式Socket和ServerSocket
   * NIO:同步非阻塞模式SocketChannel和ServerSocketChannel
   * AIO:异步非阻塞
   
9. finally 中的代码比 return 和 break 语句后执行。

10. 在浏览器地址栏输入URL回车后经历的流程

    * DNS解析->TCP连接->发送HTTP请求->服务器处理请求并返回HTTP报文->浏览器解析渲染页面->连接结束

11. 谈谈你对SpringBoot的理解

    * 
    
12. mybatis中的#和$的区别

    * #{}表示占位符,${}替换成字符串
    * #{}主要用于预编译,而${}用于字符串替换
    
13. rabbitMQ有几种工作模式
    *  简单模式-> 生产者将消息交给默认的交换机（AMQP default），交换机将获取到的信息绑定这个生产者对应的队列上，监听当前队列的消费者获取消息，执行消息消费。应用场景：短信、聊天
    *  工作模式-> 生产者将消息交给默认的交换机（AMQP default），交换机将获取到的信息绑定这个生产者对应的队列. 由于监听这个队列的消费者较多，并且消息只能有一个被消费，就会造成消息竞争。应用场景：抢红包、资源调度任务
    * 发布订阅模式->生产者将消息给交换机，交换机根据自身的类型（fanout）将会把所有消息复制同步到所有与其绑定的队列，每个队列可以有一个消费者接收消息进行消费逻辑。应用场景：邮件群发、广告
    * 路由模式->生产者将消息发送到交换机信息携带具体的路由key,交换机的类型是direct，将接收到的信息中的routingKey,比对与之绑定的队列routingkey。消费者监听一个队列，获取消息，执行消费逻辑。应用场景：根据生产者要求发送给特定的队列
    * topic主题模式->生产者发送消息，消息中带有具体的路由key，交换机的类型是topic，队列绑定交换机不在使用具体的路由key而是一个范围值
    
14. Vue的生命周期

    * 从开始创建、初始化数据、编译模板、挂载Dom→渲染、更新→渲染、卸载等一系列过程

    * 初始化、运行中、销毁。new Vue() -> created -> mounted -> destroyed

15. Vue父子组件交互

    * 父组件传给子组件：子组件通过props方法接受数据
    * 子组件传给父组件：$emit方法传递参数

16. Vue的路由实现方式

    * hash模式：在浏览器中符号“#”，history模式：
