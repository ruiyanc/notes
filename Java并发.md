## JAVA并发

1. 并发与高并发
   1. 并发
      * 资源利用率、公平性、便利性
      * 多个线程操作相同资源，保证线程安全，合理使用资源 
      * 三要素：原子性、可见性、有序性
   2. 并发与并行
      * 并行：
      * 并发：
   3. 高并发：互联网分布式系统架构设计中必须考虑的因素之一，通过设计保证系统能够**同时并行处理**很多请求
      * 服务能同时处理很多请求，提高程序性能 
2. CPU多级缓存
   1. 为什么需求CPU cache
      * CPU频率飞快，快到主存跟不上，所以在处理器时钟周期内，CPU需要等待主存，浪费资源。cache的出现，是为了缓解CPU和内存之间速度的不匹配问题 （cpu -> cache -> memory）
   2. CPU cache有什么意义
      * 时间局部性：如果某个数据被访问，那么在不久的将来它很可能被再次访问  
      * 空间局部性：如果某个数据被访问，那么与它相邻的数据很快也可能被访问
   3. CPU多级缓存-缓存一致性（MESI）
      * 用于保证多个CPU cache之间缓存共享数据的一致
   4. CPU多级缓存-乱序执行优化
      * 处理器为了提高运算速度而做出违背代码原来顺序的优化
3. **Java内存模型(*)**（Java Memory Model，JMM） 
   * Java内存模型-同步八种操作 
     
     * work=作用于工作内存的变量；main=作用于主内存的变量
     
     * lock（锁定）：$main把一个变量标识为一条线程独占状态
     
     * unlock（解锁）：$main把一个处于锁定状态的变量释放出来，释放出来的变量才可以被其他线程锁定
     
     * read（读取）：$main把一个变量值从主内存传输到线程的工作内存中，以便随后的load动作使用
     
     * load（载入）：$work它把read操作从主内存中得到的变量值放入工作内存的变量副本中
     
     * use（使用）：$work把工作内存中的一个变量值传递给执行引擎
     
     * assign（赋值）：$work它把一个从执行引擎接收到的值赋值给工作内存的变量
     
     * store（存储）：$work把工作内存中的一个变量的值传送到主内存中，以便随后的write操作
     * write（写入）：$main它把store操作从工作内存中一个变量的值传送到主内存的变量中
   * Java内存模型-同步规则
        * 如果要把一个变量从主内存中复制到工作内存，就需要按顺序地执行read和load操作；如果把变量从工作内存中同步回主内存中，就要按顺序地执行store和write操作。上述操作必须按顺序执行即可，而没有保证必须是连续执行
        * 不允许read和load、store和write操作其中一个单独出现
        * 不允许一个线程丢弃它的最近assign操作，即变量在工作内存中改变了之后必须同步到主内存中
        * 不允许一个线程无原因地（没有发生过任何assign操作）把数据从工作内存同步回主内存中
        * 一个新的变量只能在主内存中诞生，不允许在工作内存中直接使用一个未被初始化（load或assign）的变量。即就是对一个变量实施use和store操作之前，必须先执行assign和load操作
        * 一个变量在同一时刻只允许一条线程对其进行lock操作，但lock操作可以被同一线程重复执行多次，多次执行lock后，只有执行相同次数的unlock操作，变量才会被解锁。lock和unlock必须成对出现
        * 如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前需要重新执行load或assign操作初始化变量的值
        * 如果一个变量事先没有被lock操作锁定，则不允许对它执行unlock操作；也不允许去unlock一个被其它线程锁定的变量
        * 对一个变量执行unlock操作之前，必须先把此变量同步到主内存中（执行store和write操作）
        * <img title="JMM内存操作" src="https://github.com/ruiyanc/images/blob/main/JMMSynchronousOperation.jpg" alt="">
4. 线程安全性
   * 当多个线程访问某个类时，不管运行时环境采用`何种调度方式`或者这些进程将如何交替执行，并且在主调代码中`不需要任何额外的同步或协同`，这个类都能表现出`正确的行为`，那么就称这个类是线程安全的
   
   * 原子性：提供了互斥访问，同一时刻只能有一个线程来对它进行操作
     
     1. `CAS（compare and swap）(*)`：比较并交换，其作用是让CPU比较内存中某个值是否和预期的值相同，如果相同则将这个值更新为新值，不相同则不做更新
        * Atomic中的子方法 **Unsafe.compareAndSwapInt(Object var1, long var2, int var4, int var5)**：比较内存中的一个值和我们的期望值(var4)是否一样，如果一样则将内存中的这个值更新为var5，参数中的var1是值所在的对象，var2是值在对象(var1)中的内存偏移量，`参数var1和参数var2是为了定位出值所在内存的地址`
        * CAS的ABA问题 ：线程1抢先获得CPU时间片，而线程2因为其他原因阻塞了，线程1取值与期望的A值比较，发现相等然后将值更新为B，然后这个时候出现了线程3，期望值为B，欲更新的值为A，线程3取值与期望的值B比较，发现相等则将值更新为A，此时线程2从阻塞中恢复，并且获得了CPU时间片，这时候线程2取值与期望的值A比较，发现相等则将值更新为B，虽然线程2也完成了操作，但是线程2并不知道值已经经过了A->B->A的变化过程
          * 版本号机制：取数据时获取当前version,更新时把version+1,如果version不对则更新失败 
     2. LongAdder：使用分散提高效率
     3. AtomicReference
     4. AtomicStampReference
     5. 原子性-锁
        * synchronized：依赖JVM
          * 修饰代码块：大括号括起来的代码，作用于调用的对象
          * 修饰方法：整个方法，作用于调用的对象
          * 修饰静态方法：整个静态方法，作用于所有对象
          * 修饰类：括号括起来的部分，作用于所有对象
        * Lock：ReentrantLock
     6. 原子性-对比
        * synchronized：不可中断锁，适合竞争不激烈，可读性好
        * Lock：可中断锁，多样化同步，竞争激烈时能维持常态
        * Atomic：竞争激烈时能维持常态，比Lock性能好；只能同步一个值

   * 可见性：一个线程对主内存的修改可以及时的被其它线程观察到
   
     * 导致共享变量在线程间不可见的原因
       1. 线程交叉执行
       2. 重排序结合线程交叉执行
       3. 共享变量更新后的值没有在工作内存与主内存间及时更新
     * 可见性-synchronized
       * JMM关于synchronized的两条规定
           1. 线程解锁前，必须把共享变量的最新值刷新到主内存
           2. 线程加锁时，将清空工作内存中共享变量的值，从而使用共享变量时需要从主内存中重新读取最新的值（加锁与解锁是同一把锁）
     * 可见性-volatile
          1. 通过加入**内存屏障**和**禁止重排序**优化来实现
          2. 对volatile变量写操作时，会在写操作后加入一条store屏障指令，将本地内存中的共享变量值刷新到主内存 
          3. 对volatile变量读操作时，会在读操作前加入一条load屏障指令，从主内存中读取共享变量
   * 有序性：一个线程观察其它线程中的指令执行顺序，由于指令重排序的存在，该观察结果一般杂乱无序
     * JMM允许编译器和处理器对指令进行重排序，但重排序过程不会影响到单线程程度的执行，却会影响到多线程并发执行的正确性
     * 有序性-happens-before原则
       * 程序次序规则：一个线程内，按照代码顺序，书写在前面的操作先行发生于书写在后面的操作
       * 锁定规则：一个unlock操作先行发生于后面对同一个锁的lock操作
       * volatile变量规则：对一个变量的写操作先行发生于后面对这个变量的读操作
       * 传递规则：如果操作A先行方式于操作B，而操作B又先行发生于操作C，则可以得出操作A先行发生于操作C
       * 线程启动规则：Thread对象的start（）方法先行发生于此线程的每一个动作
       * 线程终端规则：对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生
       * 线程终结规则：线程中所有操作都先行发生于线程的终止检测，可以通过Thread.join()方法结束、Thread.isAlive()的返回值检测到线程已经终止执行
       * 对象终结规则：一个对象的初始化完成先行发生于他的finalize()方法的开始
5. 发布对象
   1. 发布对象：使一个对象能够被当前范围之外的代码所使用
   2. 对象逸出：一个错误的发布。当一个对象还没有构造完成时，就使它被其他线程所见
   3. 安全发布对象
      1. 在静态初始化函数中初始化一个对象引用
      2. 将对象的引用保存到volatile类型域或AtomicReference对象中
      3. 将对象的引用保存至某个正确构造对象的final类型域中
      4. 将对象的引用保存到一个由锁保护的域中
   4. 不可变对象
      * 不可变对象需要满足的条件
         1. 对象创建以后其状态就不能修改
         2. 对象所有域都是final类型
         3. 对象是正确创建的（在对象创建期间，this引用没有逸出）
      * final关键字：类、方法、变量
         4. 修饰类：不能被继承
         5. 修饰方法：锁定方法不被继承类修改；效率
         6. 修饰变量：基本数据类型变量、引用类型变量
            * final修饰引用变量时，只是不允许指向其它变量，但它的值是可以修改的
      *  Collections.unmodifiableXXX：Collection、List、Set
      *  Guava：ImmutableXXX：Collection、List、Set
      
6. 线程安全策略
    1. 线程封闭
        1. Ad-hoc线程封闭：程序控制实现，最糟糕
      
        2. 堆栈封闭：局部变量，无并发问题
      
        3. ThreadLocal线程封闭：特别好的封闭方法
   
   2. 线程不安全类 -> 线程安全-同步容器
      
      1. StringBuilder -> StringBuffer
      
      2. SimpleDateFormat -> JodaTime
      
      3. ArrayList,HashSet,HashMap等Collections
         
         * 线程安全-同步容器
             1. ArrayList -> Vector,Stack
             
             2. HashMap -> HashTable（key,value不能为null）
             
             3. Collections.synchronizedXXX(List,Set,)
      
      4. 先检查再执行：if(condition(a)){handle(a);}

7. 线程安全-并发容器 J.U.C （java.util.concurrent.\*）
   
   * ArrayList -> CopyOnWriteArrayList ->适合读多写少
     
     * 数组多的时候GC异常
     
     * 不满足实时读
   
   * HashSet、TreeSet -> CopyOnWriteArraySet、ConcurrentSkipListSet
   
   * HashMap、TreeMap -> **ConcurrentHashMap(\*)**、ConcurrentSkipListMap(高并发、有序)
   * HashMap与ConcurrentHashMap（*）
       * 
   * 安全共享对象策略
     * 线程限制：一个被线程限制的对象，由线程独占，并且只能被占有它的线程修改
     * 共享只读：一个共享只读的对象，在没有额外同步的情况下，可以被多个线程并发访问，但是任何线程都不能修改它
     * 线程安全对象：一个线程安全的对象或容器，在内部通过同步机制来保证线程安全，所以其他线程无需额外的同步就可以通过公共接口随意访问它
     * 被守护对象：被守护对象只能通过获取特定的锁来访问
8. AbstractQueuedSynchronizer - AQS （JUC的核心）
   1. 实现方式
      * 使用Node实现FIFO队列，可以用于构建锁或者其他同步装置的基础框架
      * 利用了一个int类型表示状态
      * 使用方法是继承
      * 子类通过继承并通过实现它的方法管理其状态（acquire和release）的方法操纵状态
      * 可以同时实现排它锁和共享锁模式（独占、共享）
   2. AQS同步组件
      * CountDownLatch （共享） -> **倒数计时器**(闭锁)
          * 初始值为总线程的数量。每当一个线程完成了自己的任务,计数器的值就相应减1。当计数器到达0时,表示所有的线程都已执行完毕,然后在等待的线程就可以恢复执行任务。 -->5个玩家准备好，主线程宣布游戏开始
          * countDown():用于使计数器减1，其一般是执行任务的线程调用
          * await():则使调用该方法的线程处于等待状态，其一般是主线程调用
          * 应用场景
              * 大任务切分成多个小任务，再实现多个线程开始执行任务的最大并行性
              * 某一线程在开始运行前需要等待多个线程执行完毕再开始
      * Semaphore （共享） -> **信号量**
          * 用来实现流量控制,它可以控制同一时间内对资源的访问次数
          * acquire(获取):当一个线程调用acquire操作时,它要么成功获取到信号量(信号量减1),要么一直等下去,直到有线程释放信号量,或超时,Semaphore内部会维护一个等待队列用于存储这些被暂停的线程.
          * release(释放):实际上会将信号量的值+1,然后唤醒相应Sepmaphore实例的等待队列中的一个任意等待线程.
          * 应用场景
              * 用于多个共享资源的互斥使用
              * 用于并发线程数的控制
      * CyclicBarrier（独占） -> **栅栏**
          * 让所有线程都等待完成后才会继续下一步行动。-->所有人五点在麦当劳集合
          * await():线程调用await()表示自己已经到达栅栏
          * BrokenBarrierException:表示栅栏已经被破坏，破坏的原因可能是其中一个线程 await()时被中断或者超时
          * 应用场景
              * 可以用于多线程计算数据，最后合并计算结果
      * CountDownLatch与CyclicBarrier区别
          * CountDownLatch减计数，CyclicBarrier加计数
          * CountDownLatch是一次性的，CyclicBarrier可以重用
          * CountDownLatch和CyclicBarrier都有让多个线程等待同步然后再开始下一步动作的意思，但是CountDownLatch的下一步的动作实施者是主线程，具有不可重复性；而CyclicBarrier的下一步动作实施者还是其他线程本身，具有往复多次实施动作的特点
      * ReentrantLock （独占）
          * ReentrantLock（可重入锁）和synchronized区别
              * 可重入性
              * 锁的视线
              * 性能的区别
              * 功能区别
          * ReentrankLock独有的功能
              * 可指定是公平锁还是非公平锁
              * 提供一个Condition类，可以分组唤醒需要唤醒的线程
              * 提供能够中断等待锁的线程的机制，lock.lockInterruptibly()
      * Condition
          * Condition用来替代Object的wait()、notify()来实现线程间的协作，使用Condition的await()、signal()这种方式实现线程间协作更加安全和高效
          * Condition是个接口，基本方法是await()和signal()
          * Condition依赖于Lock接口，一个Condition的实例必须与一个Lock绑定，生成一个Condition的基本代码是`lock.newCondition()`
          * 阻塞队列实际上是使用了Condition来模拟线程间协作
      * FutureTask
          * Runnable与Callable接口对比
              * Runnable和Callable都是一个接口，Runnable声明了void类型的run()方法,**没有返回值**；Callable声明了一个参数传泛型且有返回值的call()方法,**能返回执行结果**
              * run()方法异常必须在内部处理不能抛出；call()方法允许异常抛出，也可在内部处理
          * Future接口
              * Future表示异步计算的结果。提供了检查计算是否完成`cancel()`、等待其完成`isDone()`以及检索计算结果`get()`的方法
          * FutureTask类
              * FutureTask实现了RunnableFuture接口，RunnableFuture接口又实现了Runnable接口和Future接口。所以FutureTask既可以被当做Runnable来执行，也可以被当做Future来获取Callable的返回结果
      * Fork/Join框架（工作窃取算法）
          * 局限性
              * 任务线程进入睡眠，则不会再工作
              * 不应该进行IO操作
              * 任务不能抛出检查异常，只能通过代码处理
      * BlockingQueue
          * ArrayBlockingQueue
          * DelayQueue
          * LinkedBlockingQueue
          * PriorityBlockingQueue
          * SynchronousQueue         
9. 线程池
    1. new Thread弊端
        * 每次new Thread新建对象，性能差
        * 线程缺乏统一管理，可能无限制的新建线程，相互竞争，有可能占用过多系统资源导致死机或OOM
        * 缺少更多功能，比如更多执行、定期执行和线程中断等
    2. 线程池的好处
        * 重用存在的线程，减少对象创建，消亡的开销，性能佳
        * 可有效控制最大并发线程数，提高系统资源利用率，同时可以避免过多资源竞争，避免阻塞
        * 提供定时执行、定期执行、单线程和并发数控制等功能
    3. 线程池 - ThreadPoolExecutor
        1. ThreadPoolExecutor的参数
            * corePoolSize：核心线程数量
            * maximumPoolSize：线程最大线程数
            * workQueue：阻塞队列，存储等待执行的任务
            * keepApliveTime：线程没有任务执行时最多保持多久终止
            * unit：keepApliveTime的时间单位
            * ThreadFactory：线程工厂，用来创建线程
            * rejectHandler：当拒绝处理任务时的策略
        2. ThreadPoolExecutor的方法
            * execute()：提交任务，交给线程池执行
            * submit()：提供任务，能够返回执行结果
            * shutdown()：关闭线程池，等待任务都执行完
            * shutdownNow()：关闭线程池，不等待任务执行完
            * getTaskCount()：线程池已执行和未执行的任务总数
            * getCompletedTaskCount()：已完成的任务数量
            * getPoolSize()：线程池当前的线程数量
            * getActiveCount()：当前线程池中正在执行任务的线程数量
    4. 线程池 - Executor框架接口
        * Executors.newCachedThreadPool：创建一个可缓存的线程池，如果线程池的当前规模超过了处理需求时，那么将回收空闲的线程，而当需求增加时，则可以添加新的线程，线程池的规模不存在任何限制
        * Executors.newFixedThreadPool：创建一个固定长度的线程池，每提交一个任务时就创建一个线程，直到达到线程池的最大数量，这时线程池的规模将不再变化
        * Executors.newScheduledThreadPool：创建一个固定长度的线程池，而且以定时的方式来执行任务
        * Executors.newSingleThreadExecutor：是一个单线程的Executor，它创建单个线程来执行任务，如果这个线程异常结束，则会创建另一个线程来替代。能确保依照任务在队列中的顺序来串行执行（FIFO、LIFO、优先级）
    5. 线程池 - 合理配置
        * CPU密集型任务，就需要尽量压榨CPU，参考值可以设为NCPU+1
        * IO密集型任务，参考值可以设为2*NCPU
10. 并发问题
    * 死锁 
        * 多个进程在执行的过程中因争夺资源而造成的一种互相阻塞等待的现象，若无外力作用它们将无法推进下去，则称为死锁状态
    * 死锁-必要条件
        * 互斥条件：进程对所分配到的资源进行排它性使用，即在一段时间内某资源只由一个进程占用。如果此时还有其它进程请求资源，则请求者只能等待，直至占有资源的进程用完释放
        * 请求和保持条件：进程已经持有至少一个资源，但又提出了新的资源请求，而该资源已被其它进程占有，此时请求进程阻塞，但又对自己已获得的其它资源保持不放
        * 不剥夺条件：指进程已获得的资源，在未使用完之前，不能被剥夺，只能在使用完时由自己释放
        * 环路等待条件：指在发生死锁时，必然存在一个进程——资源的环形链
    * 死锁-解决办法
        * 死锁预防
        * 死锁避免
        * 死锁检测和解除
    * 多线程并发最佳实践
        * 使用本地变量
        * 使用不可变类
        * 最小化锁的作用域范围：S=1/(1-a+a/n)
        * 使用线程池的Executor，而不是直接new Thread执行
        * 宁可使用同步也不要使用线程的wait和notify
        * 使用BlockingQueue实现生产-消费模式
        * 使用并发集合而不是加锁的同步集合
        * 使用Semaphore创建有界的访问
        * 宁可使用同步代码块，也不使用同步方法
        * 避免使用静态变量
        
******
#### 高并发处理
1. 高并发-扩容思路
    * 垂直扩容：提高系统部件能力
    * 水平扩容：增加更多系统成员来实现
    * 扩容-数据库
        * 读操作扩展：redis、rabbitMQ、CDN等缓存
        * 写操作扩展：Cassandra、Hbase等
2. 高并发-缓存
    * 缓存特征
        * 命中率：命中数/(命中数+没有命中数)
        * 最大元素（空间）
        * 清空策略：FIFO、LFU、LRU、过期时间和随机等
    * 缓存命中率影响因素
        * 业务场景和业务需求-适合读多写少
        * 缓存的设计（粒度和策略）
        * 缓存容量和基础设施
    * 缓存分类和应用场景
        * 本地缓存：编程实现、Guava Cache
        * 分布式缓存：Memcache、Redis
    * 缓存常见问题
        * 缓存一致性
        * 缓存并发问题
        * 缓存穿透问题
        * 缓存雪崩问题
3. 高并发-消息队列
    * 消息队列特性
        * 业务无关：只做消息分发
        * FIFO：先投递先到达
        * 容灾：节点的动态增删和消息的持久化
        * 性能：吞吐量提升，系统内部通信效率提高
    * 为什么需要消息队列
        * 生产和消费的速度或稳定性等因素不一致
    * 消息队列好处
        * 业务解耦
        * 最终一致性
        * 广播
        * 错峰与流控
4. 高并发-应用拆分
    * 应用拆分-原则
        * 业务优先
        * 循序渐进
        * 兼顾技术：重构、分层
        * 可靠测试
    * 应用拆分-思考
        * 应用间通信：RPC(dubbo)、消息队列
        * 应用之间数据库设计：每个应用都有独立的数据库
        * 避免事物操作跨应用
        * Dubbo、SpringCloud
5. 高并发-应用限流
    * 应用限流-算法
        * 计数器法
        * 滑动窗口
        * 漏桶算法
        * 令牌桶算法
6. 高并发-服务降级与服务熔断
    * 服务降级
        * 服务降级分类
            * 自动降级：超时、失败次数、故障和限流
            * 人工降级：秒杀、双11大促等
            * 共性：目的、最终表现、粒度、自治
            * 区别：触发原因、管理目标层次、实现方式
        * 服务降级要考虑的问题
            * 核心服务、非核心服务
            * 是否支持降级、降级策略
            * 业务放通场景、策略
        * Hystrix
            * 在通过第三方客户端访问（网络）依赖服务出现高延迟或失败时，为系统提供保护和控制
            * 在分布式系统中防止级联失败
            * 快速失败同时能快速恢复
            * 提供失败回退（fallback）和优雅的服务降级机制
7. 高并发-分库分表
    * 数据库瓶颈
        * 单个库数据量太大（1T~2T）：多个库
        * 单个数据库服务器压力过大、读写瓶颈：多个库
        * 单个表数据量过大：分表
    * 数据库切库
        * 切库的基础及实际运用：读写分离
        * 自定义注解完成数据库切换-代码实现
    * 数据库分表
        * 水平切分与垂直切分
        * Mybatis分表插件shardbatis
8. 高并发-高可用手段
    * 任务调度系统分布式：elastic-job+zookeeper
    * 主备切换：apache curator+zookeeper分布式锁实现
    * 监控报警机制