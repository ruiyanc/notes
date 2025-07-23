### go语言学习笔记

1. 参考地址：[《Go 入门指南》 | Go 技术论坛](https://learnku.com/docs/the-way-to-go)

2. 区分GOROOT和GOPATH的重要性

   1. GOROOT -> go的安装目录
   2. GOPATH -> 写代码的地方

3. 设置模块国内代理

   1. go env -w GO111MODULE=on (强制启用模块化)
      go env -w GOPROXY=https://goproxy.cn,direct (阿里云源)
   2. 安装gopls（二进制方式）
      1. go install golang.org/x/tools/gopls@latest

4. go语法简记

   1. 声明变量并赋值（两种方式等价）

      ```go
      1.  power := 1000
      2.  var power int 
      	power = 1000
      ```

   2. 不允许拥有已定义但未使用的变量

   3. ```go
      函数声明
      1. func add(a int, b int) (int, bool) {}
      2. result,_ = add(5,4) -> 使用_丢弃返回值
      ```

   4. 结构体与对象

      1. 结构体字段名以首字母小写开始，则只有包内可以访问；用首字母大写开始，则都能引用。 比如Person内的name和age

      2. 值传递  -> 定义结构体并返回接收

      3. 指针/引用传递

         1. `&` 获取值的地址（ *取地址* 操作符)

         2. *X是指向类型X值的指针

         3. ```go
            goku := &Saiyan{"Power",9000}
            Super(goku) -> 输出19000
            func Super(s *Saiyan) {
              s.Power += 10000
            }  -> *X是指向类型X值的指针
            ```

   5. 结构类型 （Arrays、切片、映射hash）

   6. 包管理

      1. 把所有源代码文件放到 `$GOPATH/src/XXX/` 目录中

   7. 错误处理

      1. Go 首选错误处理方式是返回值，而不是异常。考虑 `strconv.Atoi` 函数，它将接受一个字符串然后将它转换为一个整数。
      2. 通过导入 `errors` 包然后使用它的 `New` 函数创建我们自己的错误

   8. Defer关键字  --> 等价于Java中finally关键字，常用于释放资源

   9. 函数是一种类型，可以在任何地方作为字段类型，参数或返回值

   10. 并发

       1. 协程 -> 使用go关键字

       2. sync.Mutex -> 互斥量

       3. 通道是协程之间用于传递数据的共享管道

          1. c := make(chan int)
          2. 通道发送数据 -> CHANNEL <- DATA 
          3. 通道接收数据 -> VAR := <-CHANNEL

       4. select删除消息

          1. `select` 的主要目的是管理多个通道，`select` 将阻塞直到第一个通道可用

             

### GOTM学习摘要

1. 参考地址：[GORM 指南 | 入门指南 |《GORM 中文文档 v2》| Go 技术论坛](https://learnku.com/docs/gorm/v2/index/9728)

```go
1. 安装
go get -u gorm.io/gorm
go get -u gorm.io/driver/mysql
```

2. GORM 倾向于约定，而不是配置。默认情况下，GORM 使用 ID 作为主键，使用结构体名的 蛇形复数 作为表名，字段名的 蛇形 作为列名，并使用 CreatedAt、UpdatedAt 字段追踪创建、更新时间

3. GORM 定义一个 `gorm.Model` 结构体，其包括字段 `ID`、`CreatedAt`、`UpdatedAt`、`DeletedAt`

   ```go
   // gorm.Model 的定义
   type Model struct {
     ID        uint           `gorm:"primaryKey"`
     CreatedAt time.Time
     UpdatedAt time.Time
     DeletedAt gorm.DeletedAt `gorm:"index"`
   }
   ```

4. 对于正常的结构体字段，也可以通过标签 `embedded` 将其嵌入

5. ```go
   1. dsn := "username:password@tcp(address:port)/database?charset=utf8mb4&parseTime=True&loc=Local"
   2. db, _ := gorm.Open(mysql.Open(dsn), &gorm.Config{})
   ### CRUD
   3. db.CreateInBatches(tests, 100)
   4. var tests []Test
      db.Where("Age = ?", 0).Find(&tests)
   5. db.Model(Test{}).Where("id > ?", 62).Updates(Test{Age: 109})
   6. db.Where("id > ?", 56, "Age", 0).Delete(Test{})
   ```



### Filber

1. 参考地址：[🚀 应用 | Fiber 框架](https://docs.fiber.org.cn/api/app/)

2. 一个hello world样例
   * ```
      package main
      import "github.com/gofiber/fiber/v2"
      func main() {
          app := fiber.New()
          app.Get("/", func(c *fiber.Ctx) error {
          return c.SendString("Hello World!!!")
          })
          app.Listen(":3000")
      }
      ```
