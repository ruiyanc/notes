<!-- START doctoc generated TOC please keep comment here to allow auto update -->

<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [java](#java)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## java面试笔记

1. 作用域public,private,protected,default的区别
   
   * | 作用域       | 当前类 | 同一包 | 子孙类 | 其它包 |
     |:---------:|:---:|:---:|:---:|:---:|
     | public    | √   | √   | √   | √   |
     | protected | √   | √   | √   | ×   |
     | default   | √   | √   | ×   | ×   |
     | private   | √   | ×   | ×   | ×   |

2. 八大基础类型及其默认值
   
   * | 数据类型         | 位   | 字节  | 封装类       | 默认值   |
     |:------------:|:---:|:---:|:---------:|:-----:|
     | byte（位）      | 8   | 1   | Byte      | 0     |
     | short（短整数）   | 16  | 2   | Short     | 0     |
     | int（整数）      | 32  | 4   | Interger  | 0     |
     | long（长整数）    | 64  | 8   | Long      | 0L    |
     | float（单精度）   | 32  | 4   | Float     | 0.0f  |
     | double（双精度）  | 64  | 8   | Double    | 0.0d  |
     | char（字符）     | 16  | 2   | Chatacter | 空     |
     | boolean（逻辑型） | 8   |     | Boolean   | false |

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

9. finally关键字
   
   1. finally 中的代码一定会执行吗？
      * finally 之前虚拟机被终止运行的话，finally 中的代码就不会被执行。
      * 程序所在的线程死亡
      * 关闭 CPU
   2. finally 中的代码比 return 和 break 语句后执行
   3. 如果try和finally中都有return语句，则只有finally里的会执行

10. 获取Class对象的四种方式
    
    1. 知道具体类下的可以使用
       
       * `Class clazz1 = TargetObject.class;`
    
    2. 通过 `Class.forName()`传入类的全路径获取
       
       * `Class clazz2 = Class.forName("com.demo.TargetObject");`
    
    3. 通过对象实例`instance.getClass()`获取
       
            * `TargetObject o = new TargetObject();`
           
           * `Class clazz3 = o.getClass();`
    
    4. 通过类加载器`xxxClassLoader.loadClass()`传入类路径获取
       
       * `ClassLoader.getSystemClassLoader().loadClass("com.demo.TargetObject");`

11. 序列化和反序列化 ->推荐使用Kryo作为序列化
    
    1. 序列化：将数据结构或对象转换成二进制字节流的过程
    
    2. 反序列化：将在序列化过程中所生成的二进制字节流转换成数据结构或对象的过程
    
    3. **序列化的主要目的是通过网络传输对象或者说是将对象存储到文件系统、数据库、内存中**
    
    4. 实体类什么情况下要实现**Serializable**接口
       
       1. 把内存中的对象状态保存到文件或数据库
       
       2. 用套接字在网络上传输对象
       
       3. 通过RMI(远程调用对象)传输对象
    
    5. serialVersionUID 有什么作用？
       
       * 序列化号 `serialVersionUID` 属于版本控制的作用。反序列化时，会检查 `serialVersionUID` 是否和当前类的 `serialVersionUID` 一致。如果 `serialVersionUID` 不一致则会抛出 `InvalidClassException` 异常
       
       * 强烈推荐每个序列化类都手动指定其 `serialVersionUID`，如果不手动指定，那么编译器会动态生成默认的 `serialVersionUID`
    
    6. 如果有些字段不想进行序列化怎么办？
       
       1. 使用 `transient` 关键字修饰
       
       2. `transient` 关键字的作用是：阻止实例中那些用此关键字修饰的的变量序列化；当对象被反序列化时，被 `transient` 修饰的变量值不会被持久化和恢复
       
       3. `transient` 注意
          
          * `transient` 只能修饰变量，不能修饰类和方法
          
          * `transient` 修饰的变量，在反序列化后变量值将会被置成类型的默认值。例如，如果是修饰 `int` 类型，那么反序列后结果就是 `0`
          
          * `static` 变量因为不属于任何对象(Object)，所以无论有没有 `transient` 关键字修饰，均不会被序列化

12. **I/O流(*)**
    
    * 输入输出流。数据输入到计算机内存的过程即输入，反之输出到外部存储（比如数据库，文件，远程主机）的过程即输出
    
    * `Reader` 用于读取文本， `InputStream` 用于读取原始字节
    
    * 字符流默认采用的是 `Unicode` 编码，可以通过构造方法自定义编码。
    
    * **常用字符编码所占字节数？**
      
      * `utf8` :英文占 1 字节，中文占 3 字节，`unicode`：任何字符都占 2 个字节，`gbk`：英文占 1 字节，中文占 2 字节
    
    * 字节流 (`InputStream/OutputStream`) -> 用于音频文件、图片等媒体文件
      
      * `InputStream`用于从文件读取数据（字节信息）到内存中，`java.io.InputStream`抽象类是所有字节输入流的父类
        
        * `read()`：返回输入流中下一个字节的数据
        
        * `skip(long n)` ：忽略输入流中的 n 个字节 ,返回实际忽略的字节数。
        - `available()` ：返回输入流中可以读取的字节数。
      - `OutputStream`用于将数据（字节信息）写入到文件中，`java.io.OutputStream`抽象类是所有字节输出流的父类
        
        * `write()`：将特定字节写入输出流
        - `flush()` ：刷新此输出流并强制写出所有缓冲的输出字节。
      * 从 Java 9 开始，`InputStream` 新增加了多个实用的方法：
        
        - `readAllBytes()` ：读取输入流中的所有字节，返回字节数组。
        - `readNBytes(byte[] b, int off, int len)` ：阻塞直到读取 `len` 个字节。
        - `transferTo(OutputStream out)` ： 将所有字节从一个输入流传递到一个输出流。
      
      * `FileInputStream` 是一个比较常用的字节输入流对象，可直接指定文件路径，可以直接读取单字节数据，也可以读取至字节数组中
      
      * `FileOutputStream` 是最常用的字节输出流对象，可直接指定文件路径，可以直接输出单字节数据，也可以输出指定的字节数组
      
      * ```java
        // 新建一个 BufferedInputStream 对象
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream("input.txt"));
        // 读取文件的内容并复制到 String 对象中
        String result = new String(bufferedInputStream.readAllBytes());
        System.out.println(result);
        ```
      
      * `DataInputStream/DataOutputStream` 用于**读取/写入**指定类型数据，不能单独使用，必须结合 `FileInputStream/FileOutputStream`
      
      * `ObjectInputStream` 用于从输入流中读取 Java 对象（反序列化），`ObjectOutputStream` 用于将对象写入到输出流(序列化)
    
    * 字节缓冲流(`BufferedInputStream/BufferedOutputStream`)
      
      * 字节流和字节缓冲流的性能差别主要体现在我们使用两者的时候都是调用 `write(int b)` 和 `read()` 这两个一次只读取一个字节的方法的时候。由于字节缓冲流内部有缓冲区（字节数组），因此字节缓冲流会先将读取到的字节存放在缓存区，大幅减少 IO 次数，提高读取效率。
      
      * ```java
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("深入理解计算机操作系统.pdf"));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("深入理解计算机操作系统-副本.pdf"))) {
                int len;
                byte[] bytes = new byte[4 * 1024];
                while ((len = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        ```
    
    * 字符流（`Reader/Writer`） ->用于字符
      
      * `Reader`用于从文件读取数据（字符信息）到内存中，`java.io.Reader`抽象类是所有字符输入流的父类
      
      * `Writer`用于将数据（字符信息）写入到文件，`java.io.Writer`抽象类是所有字节输出流的父类
      
      * `InputStreamReader `是字节流转换为字符流的桥梁，其子类` FileReader` 是基于该基础上的封装，可以直接操作字符文件
      
      * `OutputStreamWriter` 是字符流转换为字节流的桥梁，其子类 `FileWriter` 是基于该基础上的封装，可以直接将字符写入到文件
      
      * `BufferedReader` (字符缓冲输入流)和 `BufferedWriter`(字符缓冲输出流)
    
    * 打印流(`PrintStream/PrintWriter`)
      
      * `System.out.println("Hello！");`
      
      * `System.out` 实际是用于获取一个 `PrintStream` 对象，`print`方法实际调用的是 `PrintStream` 对象的 `write` 方法
      
      * `PrintStream` 属于字节打印流，与之对应的是 `PrintWriter` （字符打印流）。`PrintStream` 是 `OutputStream` 的子类，`PrintWriter` 是 `Writer` 的子类
    
    * 随机访问流(`RandomAccessFile`) ：支持随意跳转到文件的任意位置进行读写
      
      * `RandomAccessFile` 中有一个文件指针用来表示下一个将要被写入或者读取的字节所处的位置。我们可以通过 `RandomAccessFile` 的 `seek(long pos)` 方法来设置文件指针的偏移量（距文件开头 `pos` 个字节处）。如果想要获取文件指针当前的位置的话，可以使用 `getFilePointer()` 方法
      
      * `RandomAccessFile` 的实现依赖于 `FileDescriptor` (文件描述符) 和 `FileChannel` （内存映射文件）
      
      * 常用于实现大文件的 **断点续传**
        
        * 何谓断点续传？简单来说就是上传文件中途暂停或失败（比如遇到网络问题）之后，不需要重新上传，只需要上传那些未成功上传的文件分片即可。分片（先将文件切分成多个文件分片）上传是断点续传的基础
    
    *  如何解决大文件的上传问题
    
        * 分片上传
          
          * 多线程上传
          
          * 断点续传

1. 魔法类Unsafe 
    
    1. `Unsafe` 是位于 `sun.misc` 包下的一个类，主要提供一些用于执行低级别、不安全操作的方法，如直接访问系统内存资源、自主管理内存资源等，这些方法在提升 Java 运行效率、增强 Java 语言底层资源操作能力方面起到了很大的作用
    2. `Unsafe` 提供的这些功能的实现需要依赖本地方法（Native Method）。也就是用 Java 调用C/C++等语言操作内存资源的方法。本地方法使用 **`native`** 关键字修饰，Java 代码中只是声明方法头，具体的实现则交给 **本地代码**
    3. 为什么要使用本地代码呢？
       - 需要用到 Java 中不具备的依赖于操作系统的特性，Java 在实现跨平台的同时要实现对底层的控制，需要借助其他语言发挥作用。
       - 对于其他语言已经完成的一些现成功能，可以使用 Java 直接调用。
       - 程序对时间敏感或对性能要求非常高时，有必要使用更加底层的语言，例如 C/C++甚至是汇编。
    4. `Unsafe`功能
       1. 内存操作
       2. 内存屏障
       3. 对象操作
       4. 数据操作
       5. CAS 操作
       6. 线程调度
       7. Class 操作
       8. 系统信息

2. 语法糖
    
    1. Java 中最常用的语法糖主要有泛型、变长参数、条件编译、自动拆装箱、内部类等
    
    2. 泛型
       
       * Java虚拟机**需要在编译阶段通过类型擦除的方式进行解语法糖**
         
         * 将所有的泛型参数用其最左边界（最顶级的父类型）类型替换
         
         * 移除所有的类型参数
    
    3. 装箱与拆箱
    
        * **装箱过程是通过调用包装器的 valueOf 方法实现的，而拆箱过程是通过调用包装器的 xxxValue 方法实现的**
    
    4. 可变长参数
    
        * 变参数在被使用的时候，他首先会创建一个数组，数组的长度就是调用该方法是传递的实参的个数，然后再把参数值全部放到这个数组当中，然后再把这个数组作为参数传递到被调用的方法中
    
    5. 枚举
    
        * **使用`enum`来定义一个枚举类型的时候，编译器会自动帮我们创建一个`final`类型的类继承`Enum`类，所以枚举类型不能被继承**
    
    6. 内部类
    
        * 内部类是一个编译时的概念，`outer.java`里面定义了一个内部类`inner`，一旦编译成功，就会生成两个完全不同的`.class`文件了，分别是`outer.class`和`outer$inner.class`。所以内部类的名字完全可以和它的外部类名字相同
    
    7. 条件编译
    
        * **条件编译是通过判断条件为常量的 if 语句实现的。根据 if 判断条件的真假，编译器直接把分支为 false 的代码块消除。**
    
    8. 断言
    
        * Java执行时默认不开启，如果要开启断言检查，则需要用开关`-enableassertions`或`-ea`来开启
    
        * **断言的底层实现就是 if 语言，如果断言结果为 true，则什么都不做，程序继续执行，如果断言结果为 false，则程序抛出 AssertError 来打断程序的执行**
    
    9. 数值字面值
    
        * 在 java 7 中，数值字面量，不管是整数还是浮点数，都允许在数字之间插入任意多个下划线。这些下划线不会对字面量的数值产生影响，目的就是方便阅读。
    
        * 反编译后就是把`_`删除了。也就是说 **编译器并不认识在数字字面量中的`_`，需要在编译阶段把他去掉。**
    
    10. try-with-resource
        *  编译器帮我们实现了close那些关闭资源的操作
    
    11. lambda表达式
    
        * **lambda 表达式的实现其实是依赖了一些底层的 api，在编译阶段，编译器会把 lambda 表达式进行解糖，转换成调用内部 api 的方式**

15. 在浏览器地址栏输入URL回车后经历的流程
    
    * DNS解析->TCP连接->发送HTTP请求->服务器处理请求并返回HTTP报文->浏览器解析渲染页面->连接结束

16. 谈谈你对SpringBoot的理解
    
    * 

17. mybatis中的#和$的区别
    
    * #{}表示占位符,${}替换成字符串
    * #{}主要用于预编译,而${}用于字符串替换

18. rabbitMQ有几种工作模式
    
    * 简单模式-> 生产者将消息交给默认的交换机（AMQP default），交换机将获取到的信息绑定这个生产者对应的队列上，监听当前队列的消费者获取消息，执行消息消费。应用场景：短信、聊天
    * 工作模式-> 生产者将消息交给默认的交换机（AMQP default），交换机将获取到的信息绑定这个生产者对应的队列. 由于监听这个队列的消费者较多，并且消息只能有一个被消费，就会造成消息竞争。应用场景：抢红包、资源调度任务
    * 发布订阅模式->生产者将消息给交换机，交换机根据自身的类型（fanout）将会把所有消息复制同步到所有与其绑定的队列，每个队列可以有一个消费者接收消息进行消费逻辑。应用场景：邮件群发、广告
    * 路由模式->生产者将消息发送到交换机信息携带具体的路由key,交换机的类型是direct，将接收到的信息中的routingKey,比对与之绑定的队列routingkey。消费者监听一个队列，获取消息，执行消费逻辑。应用场景：根据生产者要求发送给特定的队列
    * topic主题模式->生产者发送消息，消息中带有具体的路由key，交换机的类型是topic，队列绑定交换机不在使用具体的路由key而是一个范围值

19. Vue的生命周期
    
    * 从开始创建、初始化数据、编译模板、挂载Dom→渲染、更新→渲染、卸载等一系列过程
    
    * 初始化、运行中、销毁。new Vue() -> created -> mounted -> destroyed

20. Vue父子组件交互
    
    * 父组件传给子组件：子组件通过props方法接受数据
    * 子组件传给父组件：$emit方法传递参数

1. Vue的路由实现方式
    
    * hash模式：在浏览器中符号“#”，history模式：
    
##### 数据库
1. 为什么不推荐使用外键
    1. 增加了复杂性：进行增删操作时都必须考虑外键约束；外键关系是确定的，若需求变更导致麻烦
    2. 分库分表下无法使用
    3. 当然保证数据的一致性和完整性；级联操作方便减少代码量
2. truncate清空数据，自增id重新从1开始
3. 
***
1. 批量插入时，其中有一条出错了怎么解决
    * 事物处理
    * insert ignore/replace
