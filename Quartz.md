<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Quartz](#quartz)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

### Quartz

* Job(Task) -> 定义实现定时功能的接口
  1. Job和JobDetail
     * Job是一个接口，只有execute() -> 编写业务逻辑
     * JobDetail用来绑定Job，为Job实例提供属性，属性有name、group、jobClass、jobDataMap。
     * JobDetail绑定指定的Job，每次Scheduler调度执行一个Job的时候，首先会拿到对应的Job，然后创建该Job实例，再去执行Job中的execute()的内容，任务执行结束后，关联的Job对象实例会被释放，且会被JVM GC清除。
     * JobDetail定义的是任务数据，而真正的执行逻辑是在Job中。
  2. JobExecutionContext
     * JobExecutionContext中包含Quartz运行的环境及Job本身的数据详情
     * 当Schedule调度执行Job的时候，就会讲JobExecutionContext传递给该Job的execute()，Job就可以通过JobExecutionContext对象获取信息。
  3. JobDataMap
     * JobDataMap实现了Map接口，以key-value键值对的形式存储数据。
     * JobDetail和Trigger都可以用JobDataMap来设置一些参数或信息

* Trigger -> 实现触发任务执行的触发器 -> 会去通知Schedule何时执行对应Job
  * 功能:制定Job的执行时间、间隔、运行次数

  1. SimpleTrigger -> (**精准指定间隔**)可以实现在一个指定时间段内执行一次作业任务或一个时间段内多次执行
     * startAt():首次被触发的时间 endAt():结束触发时间
  2. CronTrigger -> 基于日历的作业调度，Cron表达式

* Scheduler -> 调度器，结合Job和Trigger去调度任务

  