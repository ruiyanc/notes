### Apache NiFi
1. 基础术语
   * FlowFile:每条用户数据(即用户带进NiFi的需要进行处理和分发的数据)
     * Content是用户数据本身。
     * Attributes是与用户数据关联的键值对。
   * Processor:处理器,是NiFi组件,负责创建,发送,接收,转换,路由,拆分,合并和处理FlowFiles。它是NiFi用户可用于构建其数据流的最重要的构建块
2. 启动NiFi
	* bin/nifi.sh run 前台运行，bin/nifi.sh start后台
	* bin/nifi.sh install作为服务安装
3. nifi使用说明文档
	* https://nifi.apache.org/docs.html
4. nifi官方api文档
	* https://nifi.apache.org/docs/nifi-docs/rest-api/index.html 
5. 各配置文件说明
	* authorizers.xml ->记录用户信息及用户认证授权方式等
	* bootstrap.conf -> nifi启动配置信息（如nifi启动内存）
	* state-management.xml -> 记录zookeeper节点信息
5. api具体项目使用详见TemplateController
6. Nifi优化
	* linux服务器设置	![linux](https://github.com/ruiyanc/images/blob/main/Nifi-soft.png)
	* nifi.propertion设置 ![cluster](https://github.com/ruiyanc/images/blob/main/Nifi-cluster.png)