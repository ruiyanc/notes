### elasticsearch7

1. es运行

  * ./elasticsearch -Ecluster.name=my_cluster_name -Enode.name=my_node_name

2. 浏览集群

  * 检查集群的运行状况,使用_cat -> curl -X GET "localhost:9200/_cat/health?v"
  * 获取集群中的节点列表 -> curl -X GET "localhost:9200/_cat/nodes?v"
  * 查看所有索引 -> curl -X GET "localhost:9200/_cat/indices?v"
  * 创建索引,索引名customer -> curl -X PUT "localhost:9200/customer?pretty"
  * 删除索引 -> curl -X DELETE "localhost:9200/customer?pretty"
3. 修改数据
  * 索引中创建/替换文档, id可不选则生成随机ID -> curl -X PUT "localhost:9200/customer/_doc/1?pretty" -H 'Content-Type: application/json' -d'{  "name": "John Doe"}'
  * 检索索引的文档 -> curl -X GET "localhost:9200/customer/_doc/1?pretty"
  * 检索没有显性ID的文档 -> curl -X POST "localhost:9200/customer/_doc?pretty" -H 'Content-Type: application/json' -d'{  "name": "Jane Doe"}'
  * 更新文档 -> curl -X POST "localhost:9200/customer/_update/1?pretty" -H 'Content-Type: application/json' -d'{  "doc": { "name": "Jane Doe", "age": 20 }}'
  * 删除文档 -> curl -X DELETE "localhost:9200/customer/_doc/2?pretty"
4. 加载json数据文件
  * curl -H "Content-Type: application/json" -XPOST "localhost:9200/bank/_bulk?pretty&refresh" --data-binary "@xxx.json"
5. 浏览数据

  * 通过REST请求URI发送搜索参数。-> curl -X GET "localhost:9200/bank/_search?q=*&sort=account:asc&pretty"
  * 通过REST请求体发送搜索参数。-> curl -X GET "localhost:9200/bank/_search" -H 'Content-Type: application/json' -d'{"query": {"match_all":{}}, "sort":[{"account":"asc"}]}'
    * 用于搜索的REST API可以从_search端点访问,q=*参数指示Elasticsearch匹配索引中的所有文档,sort=account:asc参数指示使用每个文档的account字段按升序对结果排序
  * 关于返回到回应结果
    * `took`: Elasticsearch执行搜索所用的时间(以毫秒为单位)   `timed_out`:搜索是否超时
    * `_shards`: 搜索多少碎片，以及成功/失败搜索碎片的计数  `hit`:搜索结果
    * `hits.total`:包含与搜索条件匹配的文档总数相关的信息的对象
    * `hits.hits` :实际的搜索结果数组(默认为前10个文档)

6. 查询语言Query DSL
   * `query`:查询操作,`match_all`:搜索指定索引中的所有文档
   * `from`:(基于0)指定从哪个文档索引开始，`size`参数指定从from参数开始返回多少文档
   * `_source`:表示只返回需要的字段内容
   * `match`:字段搜索查询  --> 也就是mysql的条件查询
     * match 中如果加空格，那么会被认为两个单词，包含任意一个单词将被查询到
     * match_parase 将忽略空格，将该字符认为一个整体，会在索引中匹配包含这个整体的文档。
   * `bool`:逻辑查询 --> MySQL的and or not
     * bool must:子句指定了所有条件必须为true的查询，则将文档视为匹配
     * bool should:子句指定了一个查询列表，其中任何一个查询必须为真，才能将文档视为匹配。
     * bool must_not:子句指定了一个查询列表，其中没有一个查询必须为真，才能将文档视为匹配
     * filter:range筛选部分查询
7. 执行聚合查询 -> 聚合提供对数据进行分组和提取统计信息
   * aggs:聚合条件
     * group_by_xxx:根据xxx进行分组

