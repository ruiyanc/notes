## XML
   * 可扩展标记语言 。可扩展：标签都是自定义的
   * 功能：存储数据。
        1. 配置文件
        2. 在网络中传输
   * XML与HTML的区别：
        * 标签：自定义，预定义
        * 语法：严格，松散
        * 作用：存储数据，展示数据
   * 语法：
        1. XML文档后缀名.xml
        2. 第一行必须定义为文档声明
        3. 文档中有且仅有一个根标签
        4. 属性值必须使用引号引起来
        5. 标签必须正确关闭
        6. 标签名称区分大小写
   * 组成部分：
        * 文档声明：
            1. 格式：<?xml 属性列表 ?>
            2. 属性列表：
                * version:版本号，必须的属性。主流1.0 
                * encoding:编码方式。默认：ISO-8859-1
                * standalone:是否独立
        * 指令：结合css 
            *  \<? xml-stylesheet type="text/css" href="xx.css" ?>
        * 属性：id值唯一
        * 文本：CDATA区：在该区域中的数据被原样展示
            * 格式：<![CDATA[数据]]>
        * 约束:schemaLocation
   * 解析：操作xml文档，将文档中的数据读取到内存中
        * 解析方式： DOM，SAX：逐行读取
        * html或xml解析器Jsoup
            1. Jsoup:工具类，解析html或xml文档，返回document
                * parse:
                    * parse(File in, String charsetName):解析。。。文件
                    * parse(String html):解析。。。字符串
                    * parse(URL url, int timeoutMillis):解析。。。网络路径获取指定的。。。文档对象
            2. Document:文档对象。代表内存中的dom树
                * 获取Element对象
                    * getElementByXxx同js的获取对象。Id，Tag，Attribute属性名，
                    AttributeValue属性名与值
            3. Elements:元素Element对象的集合。ArrayList<Element>
            4. Element:元素对象
                1. 获取子元素对象。同上
                2. 获取属性值：
                    * attr():通过属性名获取属性值
                3. 获取文本内容
                    * text():获取所有子标签的纯文本内容
                    * html():标签体的所有内容(包括子标签的标签和文本内容)
            5. Node：节点对象。Element和Document的父类
        * 快捷查询：
            1. selector选择器 
            2. xPath XML路径语言        