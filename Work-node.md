## 工作笔记
1. oracle版权问题不能直接从maven库中拉取
   * 解决办法:下载相关jar包,再通过mvn生成maven所需要的依赖。
   * ./mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0.4.0 -Dpackaging=jar -Dfile=/Users/xurui/.m2/repository/oracle/ojdbc14/10.2.0.4/ojdbc14-10.2.0.4.0.jar

2. svn拉取

   * svn checkout svn://localhost/hangge --username=hangge --password=123 ~/Documents/hangge

3. 事务只加在方法上就行，锁表解决方法为SELECT * FROM `information_schema`.`innodb_trx` ORDER BY `trx_started`，kill杀掉trx_mysql_thread_id进程

4. Map和Bean互转最好用的方式--FastJSON

   * 将 Map 转换为 实体类
   		* User user = JSON.parseObject(JSON.toJSONString(user01), User.class);
   *  将 实体类 转换为 Map
   		* Map map = JSON.parseObject(JSON.toJSONString(user), Map.class);
   *  将实体类转换为JSONObject
   		* JSONObject jsonObject =  JSONObject.parseObject(JSON.toJSONString(user);
   * JSONObject转String默认值为null不显示，使用SerializerFeature.WriteMapNullValue则显示null
   * String jsonString = JSONObject.toJSONString(component,SerializerFeature.WriteMapNullValue);

5. Object转为String，String.valueOf(Object)优于.toString()
     * 使用toString如果object为null则报错，使用.valueOf不会


6. 服务器防火墙问题

   * 开放端口： firewall-cmd --zone=public --add-port=80/tcp --permanent

   * 删除端口： firewall-cmd --zone=public --remove-port=80/tcp --permanent 

   * 重新加载firewall： firewall-cmd --reload 

   * 查看开放端口： firewall-cmd --zone=public --list-ports

7. 计算相关

     * 数字转BigDecimal会出现精度不准的问题，建议使用字符串来转BigDecimal
     * 计算保留小数并四舍五入
     	* String.format("%.2f", result) 转成字符串并保留2位小数
     	* BigDecimal divide = new BigDecimal(result)
                  .divide(new BigDecimal(dividend), 4, RoundingMode.HALF_UP) 除法运算四舍五入并保留4位小数
     * NumberFormat numberFormat =  NumberFormat.getPercentInstance();
      * numberFormat.setMinimumFractionDigits(2);
         	*  百分比转小数 Number parse = numberFormat.parse(result);
             	*  小数转百分比  String format = numberFormat.format(result) 

8. curl模拟

     * curl -d 'username=admin&password=admin' -v 'http://xxxxxx:8080/logins' 模拟账号密码登录 
     * -d:Post请求, -v:打印内容, -H 'Content-type: application/json'
     * curl --cookie 'JSESSIONID=xxx' -v 'url' 携带cookie请求

9. Shell脚本获取日期

   * 获取今天的日期并转换为yyyy-MM-dd格式
       * log1=$(date -d "now" +%Y-%m-%d)

       * 获取昨天的日期
           * log2=$(date -d "yesterday" +%Y-%m-%d)
           * log2=$(date -d "1 day ago" +%Y-%m-%d)

   * 获取某天的N天前
       * log3=$(date -d "15 day ago 2022-04-17" +%Y-%m-%d) 	

10. Google的Gson
     * List<String> outtradenos = GsonUtil.*fromJson*(str, new TypeToken<List<String>>() {
       }.getType());

11. Redis相关


      1. [Centos7安装升级最新版Redis7.0.10 避坑攻略_redis最新版本CSDN博客](https://blog.csdn.net/lhmyy521125/article/details/129722928)

      2. 高版本建议使用脚本启动及关闭 /etc/init.d/redis_6379 start

      3. 使用 kill -9 pid 的强制关闭Redis方式再次启动会报pid文件存在的错误，使用 rm -rf 命令强制删除 /var/run/redis_6379.pid

      4. Redis 优化及配置

            *  THP (Transparent Huge Pages) Linux内核增加THP特性支持大内存页分配。Redis建议禁用 echo never > /sys/kernel/mm/transparent_hugepage/enabled 可追加到/etc/rc.local

            * 设置maxmemory，Redis使用的最大物理内存。

            * 设置过期策略 maxmemory-policy：allkeys-lru优化淘汰最久没有使用的ey

12. Linux相关

      1. 永久关闭swap 
         * echo "vm.swappiness = 0" >> /etc/sysctl.conf 
         * 刷新swap 
           * swapoff -a && swapon -a 
           * sysctl -p  执行生效

      2. 调整open files句柄

         * ulimit -a 查看每个用户最大允许打开文件数量

         * ulimit -n 65535 设置open files临时生效

         * vim /etc/security/limits.conf 在最后加入 。*表示所有用户
           * \* soft nofile 65535
           * \* hard nofile 65535

      3. 编写system服务单元

         1. 创建system服务文件
         2. ![示例文件](https://i.niupic.com/images/2023/05/30/b7nP.png)
         3. After：指示systemd何时应该运行脚本，在当前例子脚本将在网络连接后运行。 ExecStart：启动时要执行的实际脚本的绝对路径。 WantedBy：systemd单元应该安装到哪个引导目标中
         4. systemctl daemon-reload 系统重新读取服务文件。 systemctl enable xxx.service 启用开机自启
         5. service xxx.service start/stop/restart 启动/终止/重启服务

13. Docker相关

      1. windows下安装docker需要wsl --update下载wslLinux子系统
      2. docker search xxx 搜索docker
      3. docker pull xxx下载镜像
      4. docker ps -a 查看所有镜像日志
      5. mysql启动需要配置密码 docker run  -e MYSQL_ROOT_PASSWORD=root --name mysql -d mysql
      6. docker run -d -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 --name mysql

14. 多线程相关

      * 创建线程池
      	*  ExecutorService executor = Executors.newFixedThreadPool(400)  （阿里巴巴手册不推荐）
      	*  ThreadPoolExecutor exec = new ThreadPoolExecutor(100, 500, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());（按需求类型创建线程池，推荐）

      *  execute和submit的区别
      	1. execute是Executor接口的方法，而submit是ExecutorService的方法，并且ExecutorService接口继承了Executor接口。
      	2. execute只接受Runnable参数，没有返回值；而submit可以接受Runnable参数和Callable参数，并且返回了Future对象，可以进行任务取消、获取任务结果、判断任务是否执行完毕/取消等操作。其中，submit会对Runnable或Callable入参封装成RunnableFuture对象，调用execute方法并返回。
      	3. 通过execute方法提交的任务如果出现异常则直接抛出原异常，是在线程池中的线程中；而submit方法是捕获了异常的，只有当调用Future的get方法时，才会抛出ExecutionException异常，且是在调用get方法的线程。 