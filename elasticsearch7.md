### elasticsearch7
1.es运行
  * ./elasticsearch -Ecluster.name=my_cluster_name -Enode.name=my_node_name
2.浏览集群
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
    * took: Elasticsearch执行搜索所用的时间(以毫秒为单位) timed_out:搜索是否超时
    * _shards: 搜索多少碎片，以及成功/失败搜索碎片的计数
    * 
