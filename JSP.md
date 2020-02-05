## HTTP
* 超文本传输协议
    * 基于TCP/IP高级协议
    * 默认80端口
    * 请求/响应模型：一次请求对应一次响应
    * 无状态：每次请求之间相互独立，不能交互数据
    * 1.0短连接，1.1长连接
* 请求消息数据格式：
    1. 请求行:请求方式/请求URL 请求协议/版本
        GET/login.html HTTP/1.1
    * 请求方式：HTTP协议有7种请求方式
        * GET:
            1. 请求参数在请求行中，在url后，地址栏上
            2. 请求的url长度最多为255
            3. 不安全
        * POST：
            1. 请求参数在请求体重
            2. 请求的url长度没有限制
            2. 相对安全
    2. 请求头，名称：请求头值
        * 常见的请求头、
            1. User_Agent:浏览器告诉服务器，访问时使用的浏览器版本信息
            2. Referer:http://localhost/login.html
                * 告诉浏览器当前请求的来源
                * 作用：放盗链，统计工作
    3. 请求空行
        * 空行,用于分割post请求的请求头和请求体
    4. 请求体
        * 封装post请求消息的请求参数
        

##Servlet
* 概念：运行在服务器上的小程序
* 配置xml文件：
    * <!--    配置servlet-->
          <servlet>
              <servlet-name>demo1</servlet-name>
              <servlet-class>cn.web.servlet.SercletDemo1</servlet-class>
          </servlet>
          <servlet-mapping>
              <servlet-name>demo1</servlet-name>
              <url-pattern>/demo1</url-pattern>
          </servlet-mapping>
* 执行原理：
    1. 当服务器接受到客户端浏览器的请求后，会解析请求URL路径，获取访问的Servlet的资源路径
    2. 查找web.xml文件，是否有对应的<url-pattern>标签体内容
    3. 如果有，则在找到对应的<servlet-class>全类名
    4. tomcat会将字节码文件加载进内存，并且创建其对象
    5. 调用其方法
    
* Servlet中的生命周期方法：
    1. 被创建：执行init方法，只执行一次
        * 默认情况下，第一次被访问时，Servlet被创建
             * 可以在\<servlet>标签下配置创建时机
                1.第一次被访问时创建。  \<load-on-startup>的值为负数
                2.在服务器启动时创建。  \<load-on-startup>的值为0或正整数
    * Servlet的init方法，说明一个Servlet在内存中只存在一个对象，单例的
    2. 提供服务：执行service方法，执行多次
        * 每次访问Servlet时，service方法都会被调用一次
    3. 被销毁：执行destroy方法，执行一次
        * Servlet被销毁时执行。服务器关闭时，Servlet被销毁。用于释放资源

* Servlet3.0：
    * 支持注解配置，不需要web.xml
    * 在类上使用@WebServlet注解进行配置
        * @WebServlet("资源路径") 
        * urlPatterns：Servlet访问路径。注解中的重点用value代替，则也可省略 
        

##Request
* request和response对象是由服务器创建。
* request获取请求消息，response设置响应消息
1. 获取请求消息数据
    1. 获取请求行数据
    * GET /jspTest_war/demo?n=5 HTTP/1.1
        1. 请求方式 ：getMethod() GET
        2. (*)虚拟目录： `getContextPath()` /jspTest_war
        3. Servlet路径： getServletPath() /demo
        4. get方式请求参数：getQueryString() n=5
        5. (*)请求URI：`getRequestURI()` /jspTest_war/demo 
        getRequestURL() http://localhost/jspTest_war/demo
        6. 协议及版本： getProtocol() HTTP/1.1
        7. 客户机的ip地址： getRemoteAddr()
    2. 获取请求头数据
        * `getHeader()`:通过头名称获取值
        * getHeaderNames():获取所有请求头名称
    3. 获取请求体数据
        * 请求体：只有post请求才有
    
2. 重点方法：
    1. 获取请求参数通用方式
        1. `getParameter()`:根据参数名称获取参数值
        2. getParameterValues():根据参数名称获取参数值的数据，多值
        3. getParameterNames():获取所有请求的参数名称
        4. `getParameterMap()`:获取所有参数的map集合
        * 中文乱码问题：
            * get方式：Tomcat8以上get方式乱码问题已被解决
            * post方式：在解决参数前，设置request编码`request.setCharacterEncoding("utf-8");`
    2. 请求转发(forward):服务器内部资源跳转方式
        1. 使用步骤：
            1. getRequestDispatcher(path):通过request对象获取请求转发器对象
            2. forward(request, response):使用RequestDispatcher对象进行转发
            * 一般使用匿名。`request.getRequestDispatcher(path).forward(request,response);`
        2. 特点：
            1. 浏览器地址栏路径不发生改变
            2. 只能转发到当前服务器内部资源中
            3. 一次请求
    3. 共享数据
        * 域对象：一个有作用范围的对象，可以在范围内共享数据
        * request域：代表一次请求的范围，用于请求转发的多个资源中共享数据
        * 方法：
            * setAttribute(name,obj):存储数据
            * getAttribute(name):通过键获取值
            * removeAttribute():通过键删除键值对
    4. 获取ServletContext:
        * 
        

##Response
* 重定向(redirect)
    * response.sendRedirect(url)
    * 特点：
        1. 地址栏发送改变
        2. 重定向可以访问其它站点的资源
        3. 两次请求。不能使用request对象共享数据
    * 路径写法：
        * 给客户端浏览器使用：需要加虚拟目录(项目访问路径)
            * 动态获取，request.getContentPath()
        * 给服务器使用：不需要加虚拟目录。`转发路径`
    * 乱码问题：`response.setContentType("text/html;charset=utf-8");`
* 服务器输出字符数据到浏览器
    * response.getWriter()然后write()
* 服务输出字节码数据到浏览器
    * getOutputStream()
* 验证码
    1. 本质：图片
    2. 目的：防止恶意破坏
    

##ServletContext对象
1. 概念:代表整个web应用，可以和程序服务器来通信
2. 获取：
    * request.getServletContext()获取对象
    * this.getServletContext()
2. 功能：
    1. 获取MIME类型
        * MIME类型：在互联网通信过程中定义的一种文件数据类型
            * 格式：大类型/小类型    text/html
        * 获取：getMimeType(file)
    2. 域对象：共享数据
        * setAttribute,getAttribute
        * ServletContext:所有用户所有请求的数据
    3. 获取文件的真实(服务器)路径
        1. getRealPath(file)
            * 能获取WEB-INF下的文件("/WEB-INF/...")
            * src目录下的文件("/WEB-INF/classes/...")
    

##会话
* 会话：一次会话中包含多次请求和响应
    * 一次会话：浏览器第一次给服务器资源发送请求，会话建立，直到一方断开为止
* 功能：在一次会话范围内的多次请求间共享数据
* 方式：
    1. 客户端会话技术：Cookie
    2. 服务器端会话技术：Session
    

##Cookie
* 概念：将数据保存到客户端
* 实现原理：基于响应头set-cookie和请求头cookie实现
* 使用：
    1. new Cookie(name,value):创建cookie对象,绑定数据
    2. response.addCookie(cookie):发送cookie对象
    3. request.getCookies():获取cookie拿到数据
    * 可以创建多个Cookie对象，使用response调用多次addCookie
    * 默认情况下，当浏览器关闭后，Cookie数据被销毁
        * 可持久化存储：setMaxAge(seconds)
            * 正数:将cookie数据写入硬盘文件，cookie存活时间
            * 零：删除cookie信息
* tomcat8之后cookie支持中文数据，特殊字符空格等不支持。
8之前需要转码，采用URL编码。
    * 编码：str_date = URLEncoder.encode(str_date, StandardCharsets.UTF_8);     
    * 解码：value = URLDecoder.decode(value, StandardCharsets.UTF_8);
* cookie共享问题：
    * 一个tomcat中多个web项目之间，默认情况下cookie不能共享
        * setPath(path):设置cookie的获取范围
            * 若要多个项目间共享，将path设置为"/"
    * 不同服务器间共享：
        * setDomain(path):设置一级域名相同，多个服务器间cookie可以共享
* 特点：
    1. cookie存储数据在客户端浏览器上
    2. 浏览器对于单个cookie大小有限制(4kb)，同一个域名的总cookie数据最多为20个
* 作用：
    1. cookie一般用于存储少量的不敏感数据
    2. 在不登录的情况下，完成服务器对客户端的身份识别
    

##Session
* 概念：服务器端会话技术，在一次会话的多次请求间共享数据，将数保存在服务器端的对象中。
* 原理：session的实现依赖于cookie
* 使用：
    * request.getSession():获取HTTPSession对象
    * 使用HttpSession对象
        * setAttribute(),getAttribute(),removeAttribute()
* 客户端关闭，服务器不关闭，两次获取的session默认不是同一个，
    * 若需要相同，创建cookie，键为**JSESSIONID**，设置setMaxAge持久化保存cookie
         * new Cookie(`"JSESSIONID", session.getId()`)
* 客户端不关，服务器关闭，确保两次获取的session为同一个：
    * session的钝化：
        * 在服务器正常关闭之前，将session对象序列化到硬盘上
    * session的活化：
        * 在服务器启动后，将session文件转化为内存中的session对象
    * tomcat服务器自动完成钝化活化
* session销毁时间：
    1. 默认失效时间为30分钟。可选择性配置修改
        <session-config>
            <session-timeout>30</session-timeout>
        </session-config>
    2. session对象调用invalidate()
    3. 服务器关闭
* 特点： 
    1. session用于存储一次会话的多次请求的数据，存在服务器端
    2. session可以存储任意类型，任意大小的数据
    * session与cookie的区别：
        * session存储数据在服务器端，cookie在客户端
        * session没有数据大小限制，cookie有
        * session数据安全，cookie相对不安全

##JSP
* java服务器端界面：特殊的界面，可指定定义html标签，也可以定义java代码
    * 本质上就是一个Servlet
* JSP的脚本：JSP定义java代码的方式
    1. <% 代码 %>：在service方法中
    2. <%! 代码 %>：在jsp转换后的java类的成员位置
    2. <%= 代码 %>：输出到页面上
* 指令
    * 作用：用于配置jsp页面，导入资源文件
    * 格式：
        * <%@ 指令名称 属性名=属性值 ...%>
    * 分类：
        1. page：配置jsp页面
            * contentType：等同于response.setContentType()
               1. 设置响应体的mime类型以及字符集
               2. 设置当前jsp页面的编码
            * import：导包
            * errorPage：当前页面发生异常后，会自动跳转到指定的错误页面
            * isErrorPage：标识当前是否也是错误页面
                * true：可以使用内置对象exception
                * false：默认值。 
        2. include：页面包含的。导入页面的资源文件
            * <%@include file="...jsp" %>
        3. taglib：导入资源
            * <%@ taglib prefix="c" uri="...core"%>
                * prefix:自定义的前缀
* 注释：
    1. <!-- -->：html注释，只能注释html代码片段                
    2. <%-- --%>：jsp注释，可以注释所有
* JSP的内置对象(9个)：
    * 在jsp页面中不需要获取和创建，可以直接使用的对象
    * out：字符输出对象，将数据输出到页面上。response.getWriter()类似
        * response.getWriter()和out.write()的区别：
            * tomcat服务器响应前，先找response缓冲区数据，再找out
            * response.getWriter()数据输出永远在out.write()之前
    * 变量名                 真实类型                作用
        * pageContext      pageContext              当前页面共享数据，还可获取其它内置对象
        * request           HTTPServletRequest      一次请求访问的多个资源(转发)
        * session           HTTPSession             一次会话多个请求间
        * application       ServletContext          所有用户间共享数据
        * response          HTTPServletResponse     响应对象
        * page              Object                  当前页面对象(Servlet)，this
        * out               JspWriter               输出对象
        * config            ServletConfig           Servlet配置对象
        * exception         Throwable               异常对象(isErrorPage上才有)
        
## MVC：开发模式
* M：Model，模型。JavaBean
    * 完成具体的业务操作，如：查询数据库，封装对象
* V：View，视图。JSP
    * 展示数据
* C：Controller，控制器。Servlet
    * 获取用户的输入
    * 调用模型
    * 将数据交给视图进行展示
* 优点：耦合性低，方便维护，利于分工协作，重用型高

##EL表达式
* 概念：表达式语言
* 作用：替换和简化jsp页面中java代码的编写
* 语法：{表达式}
    * jsp默认支持EL表达式。
        * 若忽略，isELIgnored="true"忽略当前所有jsp表达式，转义符\忽略单个
* 使用：
    * 运算：
        *  \+ - * /(div) %(mod)
        * \> < == != &&(and) ||(or) !(not)
        * empty：判断字符串、集合、数组对象是否为null或者长度是否为0
            * {empty list}:是否为null或者长度是否为0
            * {not empty list}:是否不为null且长度大于0
    * 获取值：
        1. el表达式只能从域对象中获取值
        2. ${域名称.键名}：从指定域中获取指定键的值
            * pageScope  --> pageContext
            * requestScope  --> request
            * sessionScope  --> session
            * applicationScope  --> application(ServletContext)
        3. ${键名}：表示依次从最小的域中查找是否有该键对应的值，直到找到为止
        4. 获取对象、List集合，Map集合的值
            * 对象：${域名称.键名.属性名}
            * List集合：${域名称.键名[索引]}
            * Map集合：${域名称.键名.key名称}或${域名称.键名["key名称"]}
    * 隐式对象(11个)：
        * pageContext：获取jsp其它八个内置对象
            * `${pageContext.request.contextPath}`:动态获取虚拟目录
            
## JSTL
* 概念：JSP标准标签库
* 作用：用于简化和替换jsp页面上的java代码
* 常见jstl标签
    1. if：if语句
        * test必须属性，接受Boolean表达式，true则显示if标签体内容
    2. choose：while语句
        * choose声明，->switch
        * when做判断，->case
        * otherwise其他情况，->default
    3. foreach：for语句
        1. 循环操作
            * begin：开始值
            * end：结束值
            * var：临时变量
            * step：步长
            * varStatus：循环状态对象
                * index：容器中元素的索引，从0开始
                * count：循环次数：从1开始
        2. 遍历容器
            * items:容器对象
            * var：容器中元素的临时变量
            * varStatus同上
            
## 三层架构
* 界面层：用户看到的界面。用户可以通过界面上的组件和服务器进行交互
* 业务逻辑层：处理业务逻辑的。
* 数据访问层：操作数据存储文件。

##Filter：过滤器
* 概念：当访问服务器资源时，过滤器可以将请求拦截下来，完成一些特殊的功能
* 作用：完成通用的操作。如：登录验证，统一编码处理，敏感字符过滤...
* 使用：
    * @WebFilter("/*")（urlPatterns）：访问所有资源之前，都会执行该过滤器
    * chain.doFilter(req, resp)：过滤器放行
* 问题：
    1. web.xml配置
    * <!--xml配置filter-->
            <filter>
                <filter-name>demo1</filter-name>
                <filter-class>cn.web.filter.FilterDemo1</filter-class>
            </filter>
            <filter-mapping>
                <filter-name>demo1</filter-name>
                <url-pattern>/*</url-pattern>拦截路径
            </filter-mapping>
    2. 执行流程
        * 放行前对request请求消息增强
        * 放行后对response响应消息增强
    3. 生命周期方法
        * init：服务器启动后创建filter对象时调用，只执行一次。加载资源
        * doFilter：每一次请求被拦截资源时会执行，执行多次
        * destroy：服务器关闭后，filter对象被销毁，只执行一次。释放资源
    4. 配置详解
        * 拦截路径配置：
            1. 具体资源路径：/index。jsp 访问此资源时
            2. 拦截目录：/user/* 该目录下的所有资源
            3. 后缀名拦截：*.jsp  后缀为jsp的资源
            4. 拦截所有资源：/*  访问所有资源时
        * 拦截方式配置：
            * 注解配置：
                * 设置dispatcherType属性
                    1. REQUEST：默认值。浏览器直接请求资源
                    2. FORWARD：转发访问资源
                    3. INCLUDE：包含访问资源
                    4. ERROR：错误跳转资源
                    5. ASYNC：异步访问资源
            * xml配置：
                * 设置<dispatcher></dispatcher>标签
    5.过滤器链(多个)
        * 执行顺序(2个)：1->2->资源->2->1
        * 过滤器先后顺序：
            1. 注解配置：按照类名的字符串比较规则比较，值小的先执行
            2. xml配置：<filter-mapping>定义在上边的先执行

## Listener：监听器
* 事件监听机制
    * 事件，事件源(发生的地方)，监听器(对象)
    * 注册监听：事件，事件源，监听器绑在一起。事件源发送某个事件后执行监听器代码
* ServletContextListener：监听ServletContext对象的创建和销毁
    * contextDestroyed()：ServletContext对象被销毁前会调用
    * contextInitialized():ServletContext对象创建后会调用
    * 步骤：
        * 定义类实现ServletContextListener接口，复写，注解配置@WebListener