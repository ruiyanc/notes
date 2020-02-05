##Hibernate
* 对象关系映射
* configure():用于加载配置文件
    * 默认加载src/目录下的hibernate.cfg.xml
* 映射配置文件和JavaBean对象放在同一个包下
* `new Configuration().configure()`:加载配置管理类
* `buildSessionFactory()`:用于创建Session工厂对象
    * SessionFactory:对应配置文件的<session-factory>节点
* `Transaction transaction = session.beginTransaction();`:获取事务对象并开启
    * begin():开启事务
    * commit():提交事务
* openSession:创建一个session对象
    * save():添加对象
    * update():更新对象,(更新需要设置主键)
    * get(javaBean.class,id):根据主键查询对象
* HQL查询:
    * Query query = session.createQuery("FROM User"):获取查询query对象
    * List list = query.list():获取查询对象集合