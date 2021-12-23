### mongodb
1. 数据库 database
    * 创建数据库 -> use dbName
        * 存在则切换，否则创建
    * 查看所有数据库 -> show dbs
    * 删除当前数据库 -> db.dropDatabase()
2. 集合 collection
    * 创建集合 -> db.createCollection(name, [options])
        * 插入文档时会自动创建集合
    * 查看已有集合 -> show collections 或 show tables
    * 删除集合 -> db.collectionName.drop()
3. 文档 document
    * 插入文档 -> db.collectionName.insert()
        * 插入一条文档数据 -> insertOne() <--> JSONObject格式
        * 插入多条文档数据 -> insertMany() <--> JSONArray格式
    * 更新文档 -> update(<query>, <update>)
        * update({'title':'MongoDB 教程'},{$set:{'title':'MongoDB'}})
    * save() -> 通过传入的文档来替换已有文档，_id 主键存在就更新，不存在就插入
