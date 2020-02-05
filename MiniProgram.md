### 微信小程序

* 小程序中路径都要用相对路径
* pages : [] 加载pages页面，默认加载第一行
* tabBar : 切换页面
* rpx : 手机上自适应的px, @import "" : 导入样式，import导入js，include导入代码块
* \`{{ }}\`: 波浪符获取表达式 
* 生命周期
  * onLaunch:初始化，只调用一次
  * onShow:跳到前台，onHide:跳到背景
  * debugger:打断点
* Page
  * navigateTo():转发页面,前一个hide,可onShow返回
  * redirectTo():重定向页面,不能返回
  * bindtap:点击组件在对应页面触发处理->作为中间件使用
  * data-  :自定义
* \<view\>:页面标签 。hidden:只是隐藏了但都加载在页面了
* \<scroll-view>:滚动的view
* \<block\>:块级标签,可以多个view
* \<template name=\>:模板 , is:引入模板, ...data:引入对象
* flex布局(盒装弹性布局)
  * display:flex , flex-direction:排列方向
  * flex-wrap:换行规则 wrap换行(默认不换行，压缩并排)
  * justify-content:对齐规则 center:居中 space-around:周围包裹空白
* 组件
  * hover-start-time:按住后多久出现点击态
  * hover-stay-time:手指松开后点击态保留多久
  * bindscrolltoupper:滚轮到顶部触发 upper-threshold:距离顶部多远
  * bindscrolltolower:滚到底部触发   lower-threshold:距离底部多远
  * bindscroll:滚动触发事件(滚动就触发)
  * enable-back-to-top=true:点击顶部框滚动条返回到顶部
  * scroll-with-animation=true:动画过渡
  * scroll-into-view:以id为条件滚动
  * scroll-x:横向滚动,scroll-y:纵向滚动
* icon图标:type,size,color
* text文本:selectable可选,space连续空格,decode解码
* rich-text富文本:推荐数组[name标签名,attrs:{}标签属性]
* progress进度条:percent百分比,show-info右侧显示,active动画效果active-mode:forwards继续播放
* 表单组件:
  * button按钮,size=mini小的,plain:背景色透明,loading加载图标
  * checkbox-group多选按钮组 radio-group单选组
  * input:focus焦点,cursor光标位置
  * picker选择器,mode切换选择器
* 导航组件=>a标签
  * \<navigator\>页面链接,navigate跳转,可以回退(传参?)redirect跳转
* 视频组件
  * image图片,lazy-load懒加载(局部刷新加载)
  * video视频
* 小程序与后台通信
  * wx.request(OBJECT)->类似于ajax，success回调函数