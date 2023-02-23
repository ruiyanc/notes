## JAVA并发

1. 并发与高并发
   1. 并发
      * 资源利用率、公平性、便利性
      * 多个线程操作相同资源，保证线程安全，合理使用资源 
      * 三要素：原子性、可见性、有序性
   2. 高并发：互联网分布式系统架构设计中必须考虑的因素之一，通过设计保证系统能够**同时并行处理**很多请求
      * 服务能同时处理很多请求，提高程序性能 
2. CPU多级缓存
   * 为什么需求CPU cache
     * CPU频率飞快，快到主存跟不上，所以在处理器时钟周期内，CPU需要等待主存，浪费资源。cache的出现，是为了缓解CPU和内存之间速度的不匹配问题 （cpu -> cache -> memory）
   * CPU cache有什么意义
     * 时间局部性：如果某个数据被访问，那么在不久的将来它很可能被再次访问  
     * 空间局部性：如果某个数据被访问，那么与它相邻的数据很快也可能被访问
   * CPU多级缓存-缓存一致性（MESI）
     * 用于保证多个CPU cache之间缓存共享数据的一致
   * CPU多级缓存-乱序执行优化
     * 处理器为了提高运算速度而做出违背代码原来顺序的优化

3. `Java内存模型(*)`（Java Memory Model，JMM） 
   
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
      
      1. 不可变对象需要满足的条件
         
         1. 对象创建以后其状态就不能修改
         
         2. 对象所有域都是final类型
         
         3. 对象是正确创建的（在对象创建期间，this引用没有逸出）
      
      2. final关键字：类、方法、变量
         
         1. 修饰类：不能被继承
         
         2. 修饰方法：锁定方法不被继承类修改；效率
         
         3. 修饰变量：基本数据类型变量、引用类型变量
            
            * final修饰引用变量时，只是不允许指向其它变量，但它的值是可以修改的
      
      1. Collections.unmodifiableXXX：Collection、List、Set
      
      2. Guava：ImmutableXXX：Collection、List、Set
   
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

6. 线程安全-并发容器 J.U.C （java.util.concurrent.\*）
   
   * ArrayList -> CopyOnWriteArrayList ->适合读多写少
     
     * 数组多的时候GC异常
     
     * 不满足实时读
   
   * HashSet、TreeSet -> CopyOnWriteArraySet、ConcurrentSkipListSet
   
   * HashMap、TreeMap -> **ConcurrentHashMap(\*)**、ConcurrentSkipListMap(高并发、有序)
   
   * 安全共享对象策略
     
     * 线程限制：一个被线程限制的对象，由线程独占，并且只能被占有它的线程修改
     
     * 共享只读：一个共享只读的对象，在没有额外同步的情况下，可以被多个线程并发访问，但是任何线程都不能修改它
     
     * 线程安全对象：一个线程安全的对象或容器，在内部通过同步机制来保证线程安全，所以其他线程无需额外的同步就可以通过公共接口随意访问它
     
     * 被守护对象：被守护对象只能通过获取特定的锁来访问

7. AbstractQueuedSynchronizer - AQS （JUC的核心）
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