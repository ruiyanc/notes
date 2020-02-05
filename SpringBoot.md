#SpringBoot
* 基于约定,大多默认(`约定大于配置`)
* 起步依赖,自动配置
    * <parent>:坐标版本控制
* @SpringBootApplication:声明springboot引导类(等价于下面两个注解)
    * @EnableAutoConfiguration:开启自动化配置,@ComponentScan:进行包扫描
    * SpringApplication.run():运行引导类,run参数是引导类的字节码对象
* application.yml配置文件(不支持@PropertySource加载)
    * 语法:key:value
    * 注入bean,@Component和@ComfigurationProperties(prefix="名")
* 分别创建-dev开发和-prod生成环境的配置
    * 设置spring.profiles.active=dev
* 获取配置文件的数据
    * @Value("${name}"):绑定字段
    * @PropertySource:指定配置文件位置
    * @ConfigurationProperties():绑定对象数据,需SetGet方法
* 基础语法:
    * @PathVariable:接收参数名并转成目标参数名
    * @GetMapping=@RequestMapping(method=GET)
    * JSON数据处理
        * @JsonIgnore:忽略
        * @JsonProperty:指定别名
        * @JsonInclude(Include.NON_NULL):空字段不返回
        * @JsonFormat(pattern = "yyyy-MM-dd"):日期格式化
* 全局异常处理
    * @ControllerAdvice:
        * 结合@ExceptionHandler定义全局捕获机制
        * 使用@ModelAttribute:配置全局数据
* 前端的跨域请求(CORS支持)
    * @CrossOrigin(value=url,maxAge=1800,allowedHeaders="*")
        * value:支持跨域的url,maxAge:探测请求的有效期,allowedHeaders:允许的请求头
* 注册拦截器
    * 创建拦截器实现HandlerInterceptor接口
        * 方法顺序:preHandle->Controller->postHandle->afterCompletion
    * 配置拦截器,继承WebMvcConfigurer接口
        * addInterceptors:添加拦截器
            * addPathPatterns:拦截路径,excludePathPatterns:排除的路径
* 配置AOP(面向切面编程)
    * @Aspect:切面类
* 整合mybatis
    * application.properties加入数据库信息
    * 别名扫描包:mybatis.type-aliases-package=rui.model
    * 加载mybatis映射文件:mybatis.mapper-locations=classpath:mapper/*Mapper.xml
* 整合Servlet
    * @ServletComponentScan:启动时扫描@WebServlet并实例化
* 文件上传
    * 默认采用StandardServletMultipartResolver组件
    * @RestController:该类下方法返回值会自动做json格式转换
    * MultipartFile filename
        * transferTo(file):上传
        * getOriginalFilename():获取文件名
* 整合redis
    * StringRedisTemplate:获取redis操作对象
        * opsForXxx:对应的操作
    * redis集群
        * 
* 整合Thymeleaf
    * 特点:对html标记渲染
    * 调用内置对象用\#,大部分以s结尾
    * 语法 ${}
        * th:text : 页面输出值text内容
        * th:value : 将值放入到input标签的value中
        * \#strings.isEmpty(key) : 判断字符串是否为空.
            * contains(key,"T"):是否包含指定的字符串,length:返回字符串长度
            * startsWith():是否以指定字符串开头,endsWith:结尾
            * indexOf():查找字符串的位置,substring():截取字符串
            * toUpperCase():字符串全部大写,toLowerCase():小写
        * \#dates.format(key): 格式化日期,
            * year:年,month:月,day:日
        * th:if : 条件判断 `"${sex} == '男''"`
        * th:swith和th:case : 判断
        * th:each : foreach遍历 
            * 遍历list : `"list,var : ${lists}"`
                * var:状态变量 
                    * index:索引,count:计数,size:被迭代对象长度
                    * odd/even:奇数/偶,first/last:是否为头/尾
            * 遍历map : `"maps : ${map}" th:each="?:${maps}" th:text="${?.key}"`
                * 先遍历map对象,再遍历key,value,取value值(?.value.属性名)
        * 域对象取值操作:
            * \#httpServletRequest.getAttribute('key'):取request对象值
            * session.key:取session对象值
            * application.key:取ServletContext对象值
    * URL表达式: th:href="@{}"
        * 绝对路径不变
        * 相对路径:
            * 相对于项目上下文的: @{/show}
            * 相对于服务器的根路径: @{~/..resources}
        * url中参数传递
            * @{/show(id=1,name=1)}传参 <=> show?id=1&name=1
            * restful风格传参: @{/path/{id}/show(id=1)}
* 服务端表单数据校验
    * @NotBlank:判断字符串是否为null或空字符串(不能通过)
    * @NotEmpty:同上,(可以通过校验)
    * @Length:判断字符串的长度
    * @Min:判断数值最小值,@Max:最大值
    * @Email:判断邮箱是否合法
* Filter和Interceptor的区别
    * 基于回调函数doFilter();基于AOP思想
    * 只在Servlet前后起作用;深入到方法前后、异常抛出前后
    * 依赖于Servlet容器即web应用中;不依赖Servlet,可运行在多种环境
    * 接口调用生命周期里,Filter只在容器初始化时调用一次,Interceptor可以被多次调用
    * 执行顺序:过滤前->拦截前->控制器执行->拦截后->过滤后