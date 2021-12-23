### Apache NiFi
1. 基础术语
   * FlowFile:每条用户数据(即用户带进NiFi的需要进行处理和分发的数据)
     * Content是用户数据本身。
     * Attributes是与用户数据关联的键值对。
   * Processor:处理器,是NiFi组件,负责创建,发送,接收,转换,路由,拆分,合并和处理FlowFiles。它是NiFi用户可用于构建其数据流的最重要的构建块
2. 启动NiFi
   * bin/nifi.sh run 前台运行，bin/nifi.sh start后台
   * bin/nifi.sh install作为服务安装
