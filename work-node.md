## 工作笔记
1.oracle版权问题不能直接从maven库中拉取
  * 解决办法:下载相关jar包,再通过mvn生成maven所需要的依赖。
  * ./mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0.4.0 -Dpackaging=jar -Dfile=/Users/xurui/.m2/repository/oracle/ojdbc14/10.2.0.4/ojdbc14-10.2.0.4.0.jar
2.svn拉取
  * svn checkout svn://localhost/hangge --username=hangge --password=123 ~/Documents/hangge
3.事务只加在方法上就行，锁表解决方法为SELECT * FROM `information_schema`.`innodb_trx` ORDER BY `trx_started`，kill杀掉trx_mysql_thread_id进程
