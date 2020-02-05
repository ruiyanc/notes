##Vue
* 用于构建用户界面的渐进式框架
* cdn引入:`<script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>`
* var vm=new Vue({}):创建一个vue实例
    * `文本`{{}}:输出对象属性和函数返回值
    * v-text:更新文本textContent
    * `html`v-html:更新innerHTML
* 语法
    * el:对应id
    * data:定义属性
    * methods:定义函数
    * filters:过滤器
* `:class`:绑定class,`:style`:绑定style对象
* v-:指令
    * `属性`bind:响应地更新HTML属性
        *  v-bind:style:直接设置样式
    * if:if判断显示
    * for:`site in sites`绑定数组数据来渲染项目列表
        * `value in object`:通过对象的属性来迭代数据
        * `(key,value) in object` {{key}}:{{value}}
    * on:添加事件监听
        * 事件修饰符(@:click.stop="")
            * .stop:阻止单击事件冒泡
            * .prevent:提交事件不再重载页面
            * .capture:添加事件侦听器时使用事件捕获模式
            * .self:只当事件在该元素本身（而不是子元素）触发时触发回调
            * .once:事件只能响应一次
        * 案件修饰符(全部按键别名)
            * .enter .tab .delete (捕获 "删除" 和 "退格" 键) .esc
            * .space .up .down .left .right .ctrl .alt .shift .meta
    * model:实现表单输入和应用状态之间的双向数据绑定
        * `自动收集表单数据`:
        * selected,optionradio,checkbox,
        *  修饰符.lazy:转变为在change事件中同步
        * .number:自动将用户的输入值转为Number类型
        * .trim:自动过滤用户输入的首尾空格
    * show:根据条件展示元素,样式隐藏
    * 修饰符.指明的特殊后缀:用于指出一个指令应该以特殊方式绑定
        * .prevent:告诉v-on 指令对于触发的事件调用 event.preventDefault()
    * 缩写
        * <a v-bind:href=.> = <a :href=.>
        * <a v-on:click=.> = <a @click=.>
* 计算属性`computed`
     * get得到属性, set监视属性
     * computed基于它的依赖缓存,多次读取只执行一次getter,只有相关依赖发生改变时才会重新取值,大量搜索时节省内存
     * methods在重新渲染的时候,函数总会全部重新调用执行。
* 监听属性
    * $watch:来响应数据的变化
* Vue动画
    * transition:过渡
    * .xxx-enter-active,.xxx-leave-active:显示/隐藏的过渡效果
* 注册组件
    * Vue.component(tagName, options)
        * <tagName>:组件名，options:配置选项
        * props:数据传给子组件
##Vue CLI
* vue create 项目名:创建vue-cli项目
* vue serve:配置启动服务器,vue-ui:图形化界面
* vue build:构建文件   vue==>npm run
    * 构建到dist目录
* npm run serve:运行项目
* vue add router:添加路由插件, vue add vuex:添加vuex插件
* npx vue-cli-service:
    * serve: 启动服务器并热重载
    * build:在dist/目录产生用于生产环境的包
        *  --modern:现老浏览器版本
* public/index.html:模板
    * <%= VALUE %>:用来做不转义插值；
    * <%- VALUE %>:用来做 HTML 转义插值；
    * <% expression %>:用来描述 JavaScript 流程控制
* 屏蔽插件:vue.config.js
    *         *   module.exports = {
          // 去掉文件名中的 hash
          filenameHashing: false,
          // 删除 HTML 相关的 webpack 插件
          chainWebpack: config => {
            config.plugins.delete('html')
            config.plugins.delete('preload')
            config.plugins.delete('prefetch')
          }
        }
* 多页应用:配置下pages添加同级index
* less
    * \@primary-color:red; 引入:@import "xx.less"
    * 创建vue.config.js里配置自动引入css文件
* 环境变量
    * .env:定义变量(需重启服务器生效)
        * .env.local:本地的不被git
        * .production:发布模式
        * 变量以VUE_APP开头,如VUE_APP_TITLE=hello
