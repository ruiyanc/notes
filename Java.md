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

   * |  类型  | char | byte | short | int  | long | float | double | boolean |
     | :----: | :--: | :--: | :---: | :--: | :--: | :---: | :----: | :-----: |
     | 默认值 |  空  |  0   |   0   |  0   |  0   |  0.0  |  0.0   |  false  |

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

6. ==与equals

   * ==:判断两个对象的地址是否相等(基础类型比较值,引用类型比较内存地址)
   * equals:判断两个对象的值是否相等

7. BIO,NIO和AIO

   * BIO:同步阻塞模式Socket和ServerSocket
   * NIO:同步非阻塞模式SocketChannel和ServerSocketChannel
   * AIO:异步非阻塞

