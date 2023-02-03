## 工作笔记
1. oracle版权问题不能直接从maven库中拉取
   * 解决办法:下载相关jar包,再通过mvn生成maven所需要的依赖。
   * ./mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0.4.0 -Dpackaging=jar -Dfile=/Users/xurui/.m2/repository/oracle/ojdbc14/10.2.0.4/ojdbc14-10.2.0.4.0.jar

2. svn拉取

   * svn checkout svn://localhost/hangge --username=hangge --password=123 ~/Documents/hangge

3. 事务只加在方法上就行，锁表解决方法为SELECT * FROM `information_schema`.`innodb_trx` ORDER BY `trx_started`，kill杀掉trx_mysql_thread_id进程

4. Map和Bean互转最好用的方式

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
 
