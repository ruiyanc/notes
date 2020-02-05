## Struts2
* struts框架就是对Servlet的封装
* 注意：2.5之后filter的位置去掉ng包的路径
*     *<filter>
            <filter-name>struts2</filter-name>
            <filter-class>org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter</filter-class>
       </filter>
* struts.xml配置：
    * package + name:包名，唯一
        * extends:继承，默认struts-default
        * abstract:抽象包，被用于继承(抽象包里不声明action)
        * namespace:命名空间，访问路径，默认值是空(模块化)
    * action：动作，相当于->Servlet
        * name相当于Servlet的映射路径(@WebServlet)
        * class:处理请求的类->Servlet类
        * method:处理请求的方法
        * 可用通配符\*统一处理 `<name="*" method={1}>`
    * `一般写个Action类继承于ActionSupport`实现ModelDriven<T>接口
    * result：结果，写返回jsp的页面
        * name:
            * success:成功，跳转
            * error:错误
            * none:不跳转
            * input:回显
        * type:
            * dispatcher:转发到页面，默认
            * redirect:重定向到页面
            * chain:转发到另一个(不同包)action
                * \<param name="namespace">命名空间
                * \<param name="actionName">动作名
            * redirectAction:重定向到另一个(不同包)action
* 修改struts的访问后缀
    
    * \<constant name="struts.action.extension" value="php">\</constant>
* 开发模式：显示详细的错误信息
    
    * name="struts.devMode" value="true"
* Action类：
* 文件上传: 基于表单的HTML文件上传
    
    * 表单设置：enctype="multipart/form-data" \<s:file>
* 拦截器：
    * \<interceptor-ref name="params"/>
        * params:设置action上的请求参数。
        * validation:提供action的验证支持
        * fileUpload:便于文件上传
        * ServletConfig:提供可访问各种基于Servlet信息的action
        * token:检查action的有效性，以防止重复提交表单。
        * createSession:自动创建HTTP会话
* status标签库
    * <s:form>表单,要有name
    * <s:head>生成Struts2 应用程序所需的javascript和stylesheet元素。
    * <s:div>用于呈现HTML Div元素,s:text用于在屏幕上呈现文本
    * textfield文本，requiredLabel代表必填项
    * <s:property value="">action取值
    * 样例：\<s:checkboxlist list="#{'代码':'代码','泡妞':'泡妞'}" label="爱好">
* 非空验证(回显显示错误)：
    * 全局：覆写validate方法。加注解@SkipValidation可跳过验证
    * 局部校验：validate+方法名
*     * public void validate() {
            if (StringUtils.isEmpty(user.getUsername())) {
                  addFieldError("username", "用户名不能为空");}}
* xml验证：.xml文件命名'[action-class]' - validation.xml
* 国际化(i18n): 设置配置文件global_zh_cn.properties:中文环境
* OGNL的作用:相当于EL表达式
    1. 取值或输出到数据页面
        * <s:property value=>:不写引号是取值，写就是字符串
    2. 调用方法
    3. 访问静态属性/方法
        * 先打开静态属性的访问权限
    4. 封装list数据,花括号表示
        * <s:radio list="{'男','女'}" name="gender" label="性别"/>
    5. 封装map数据
        * \#{'key1':'value1',...}表示
    * struts.xml里${@java.net.URLEncoder@encode(fileName,"UTF-8")}
* contextMap:存储数据
    * ActionContext(动作上下文对象)
        * context对象:ActionContext context = ActionContext.getContext();
        * session对象:`Map<String, Object> session = context.getSession();`   
        * request对象:`HttpServletRequest request = ServletActionContext.getRequest();`
        * application对象:`Map<String, Object> application = context.getApplication();`
    * ValueStack(值栈)
        * ValueStack valueStack = ActionContext.getContext().getValueStack();
        * setValue()
    * 取contextMap,写#key名字,取ValueStack,直接key名字
    *     * <s:iterator value="sList" var="s">
            <s:property value="#s.name"/>...->${s.name}
    * <s:set>标签:往作用域里存数据
        * value:'值',var:变量名,scope:作用域(默认action+session....)
    * <s:action>标签:在jsp里访问action
        * executeResult="true":把action的数据显示在当前页面
    * <s:url>标签:声明url路径
    * <s:a>:跳转
    * 主题:theme属性
* 防止用户重复提交表单
    * 重定向到另一个jsp页面
    * 验证码
    * <s:token/>配合拦截器<interceptor-ref name="tokenSession"/>