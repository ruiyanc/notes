<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [JVM](#jvm)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## JVM

1. 类加载器子系统+运行时数据区+执行引擎构成
2. 对象存储在堆中,局部变量指向堆中的对象
3. 线程共享区(堆和方法区)
   * java魔数: .class文件开头都是cafe babe
   * 程序计数器:指向当前线程所执行的字节码指令的行号(地址)
   * 虚拟机栈由栈帧组成,帧中包括局部变量,操作树栈,动态链接,方法出口
   * 堆:
4. native修饰的存储在本地方法栈中
5. 内存回收机制
   1. jvm分代回收分为新生代(Eden,from,to)和老年代
      * 新生代采用的是复制算法
        * 优点:简单,没有内存碎片
        * 缺点:空间折半
      * 老年代:标记整理算法
        * 优点:没有内存碎片
        * 缺点:效率低
   2. minor gc和担保机制(major gc减少gc次数,提高gc速度)
   3. GC ROOT
6. java并发
   1. synchronized:阻塞式同步
   2. 多线程cpu并行,线程优先使用本地工作内存,即共享变量线程不会切换
   3. volatile