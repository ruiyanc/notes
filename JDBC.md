## JDBC
* 连接mysql8.0时出现的问题
    * 驱动包路径改为`com.mysql.cj.jdbc.Driver`  默认自动已注册驱动
    * url:`jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC` 禁用SSL证书
* 各个对象：
    1. DriverManager：驱动管理对象
     * 注册驱动：   registerDriver
        mysql5之后可以省略注册驱动jar包
     * 获取数据库连接：   getConnection（url，user，password）
        * url：指定连接的路径 `jdbc:mysql://ip地址:端口号/数据库名称`
        默认为本机mysql和端口3306，可省略。jdbc:mysql///库名
    2. Connection：数据库连接对象
     * 获取执行sql的对象
        * createStatement() 
        * prepareStatement() 
     * 管理事务
         * 开启事务：setAutoCommit()： false则开启
         * 提交事务：commit()
         * 回滚事务：rollback()
    3. Statement：执行sql的对象
     * 执行sql：
         1. execute：执行任意
         2.  int executeUpdate：执行DML语句，DDL语句
             * 返回值：影响的行数用于判断DML语句是否执行成功，大于0则成功
         3. ResultSet executeQuery:执行DQL语句（select）
     4. ResultSet:结果集对象，封装查询结果
     * next():游标向下移动一行
     * getXxx:获取与Xxx相应数据类型的数据 <br>
       参数： int：代表列的编号，String：代表列名称 
     * 一般步骤： (循环移动游标，判断是否有数据，有则获取，无则到达末尾)
	 
##Junit单元测试
* 黑盒测试：不写代码，给输入值看程序是否能够输出期望的值
* 白盒测试：要写代码，关注程序具体的执行流程。
    * 白盒测试
         * 显示绿则成功，红则失败
         * 测试类名：被测试的类名Test，包名：xxx.test
         * 测试方法：test测试的方法名，返回值void，空参
         * 添加注解@Test
         * 断言判断Assert.assertEquals(期望的值，实际结果)
    *     @Before注解：初始化方法，用于资源申请，所有测试方法在执行之前都会先执行该方法
    *     @After注解：释放资源方法，所有测试执行完成后，都会自动执行该方法
         
## Annotation 注解（也叫元数据）说明程序。相当于标签
* 作用：
*       编写文档：通过注解生成doc文档
*       代码分析：通过注解对代码进行分析【使用反射】
*       编译检查：通过注解让编译器能够实现基本的编译检查【Override】
* JDK中预定义的注解：
*       @Override：检测被该注解标注的方法是否是继承父类（接口）的
*       @Deperecated：该注解标注的内容，表示已过时
*       @SuppressWarnings：压制警告。一般用"all"压制所有警告

自定义注解
* 格式：_public @interface_ 注解名称{属性列表;}
* 本质：注解本质上是一个接口，该接口默认继承Annotation接口
    * `public interface 注解名称 extends java.lang.annotation.Annotation{}`
* 属性：接口中的抽象方法
    * 属性的返回值类型
        * 基本数据类型
        * String
        * 枚举
        * 注解
        * 数组      

元注解：用于描述注解的注解
* @Target:描述注解能够作用的位置
*      ElementType取值：TYPE作用于类上，METHOD作用于方法上，FIELD作用于成员变量上
* @Retention:描述注解被保留的阶段
*      RetentionPolicy：RUNTIME
* @Documented：描述注解是否被抽取到api文档中
* @Inherited：描述注解是否被子类继承

解析注解：获取注解中定义的属性值
* 获取注解定义的位置的对象（Class，Method，Field）
* 获取指定的注解
    * getAnnotation（Class）
* 调用注解中的抽象方法获取配置的属性值
