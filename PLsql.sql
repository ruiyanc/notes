select * from emp;
--PLSQL入门
/*declare 声明
begin 开始
  end 结束
  dbms_output.put_line()打印 
  &aaa名称为aaa并返回自己输入的值*/
declare 
i varchar2(10):='张三';
begin
  dbms_output.put_line(i);
  end;
  
  --查询7369的工资并打印
declare
--名称 表名的某个属性%type 一个
  vsal emp.sal%type;
  begin
    --将查询出的结果赋值给vsal
    select sal into vsal from emp where empno = 7369;
    
    dbms_output.put_line(vsal);
    end;
    
select * from emp where empno = 7369;
declare 
--名称 表名%rowtype 一行
       vrow emp%rowtype;
       begin 
         select * into vrow from emp where empno = 7369;
         
         dbms_output.put_line('姓名：'||vrow.ename||'  工资  '||vrow.sal);
         end;
         
/* 条件判断
   if then 
     elsif then
       else
         end if;
         */
   declare 
         age number := &aaa;
         begin
           if age < 18 then
             dbms_output.put_line('小屁孩');
             elsif age>=18 and age <= 24 then 
               dbms_output.put_line('老油条');
               elsif age>24 and age < 40 then
                 dbms_output.put_line('老司机');
                 else 
                   dbms_output.put_line('老年人');
                 end if;
                 end ;
                 
declare
i number:=1;
begin
  while i<=10 loop
    dbms_output.put_line(i)
    i := i+1;
    end loop;
end;

declare 
i number :=1;
begin
  --reverse 降序
  for i in 1..10 loop
    dbms_output.put_line(i);
    end loop;
    end;
--loop循环
declare 
  i number :=1;
  begin 
    loop
      exit when i > 50;
      dbms_output.put_line(i);
      i := i+1;
      end loop;
      end; 
--输出菱形
declare 
 m number :=10;
 begin
   for x in -m..m loop
     for y in -m..m loop 
       if abs(x)+abs(y)<=m then
         dbms_output.put('*');
         else 
           dbms_output.put(' ');
           end if;
           end loop;
           dbms_output.put_line('');
           end loop;
           end;
           
/*cursor 游标名 is 查询结果集
1.声明游标
2.打开游标 open游标名
3.从游标中取数据 fetch 游标名 into 变量, %found:找到数据
4.关闭游标  close游标名
*/
--输出所有姓名和工资
declare 
cursor vrows is select * from emp;
vrow emp%rowtype;
begin
  open vrows;
  loop 
    fetch vrows into vrow;
    exit when vrows%notfound;
    dbms_output.put_line('姓名：'||vrow.ename||' 工资 '||vrow.sal);
    end loop;
  close vrows;
  end;
--输出指定部门下的员工姓名和工资
declare
--声明带参游标
cursor vrows(dno number) is select * from emp where deptno=dno;
vrow emp%rowtype;
begin
  --打开游标，指定10号部门
  open vrows(20);
  loop
    fetch vrows into vrow;
    exit when vrows%notfound;
     dbms_output.put_line('姓名：'||vrow.ename||' 工资 '||vrow.sal);
    end loop;
    close vrows;
  end ;
/**系统引用游标
1.声明游标：游标名 sys_refcursor
2.
*/
declare 
vrows sys_refcursor;
vrow emp%rowtype;
begin 
  open vrows for select * from emp;
  loop
    fetch vrows into vrow;
    exit when vrows%notfound;
    dbms_output.put_line('姓名：'||vrow.ename||' 工资 '||vrow.sal);
    end loop;
    close vrows;
    end;
    
--使用for循环遍历游标,自己声明变量以及打开关闭游标
declare
cursor vrows is select * from emp;
begin 
  for vrow in vrows loop
    dbms_output.put_line('姓名：'||vrow.ename||' 工资 '||vrow.sal);
    end loop;
    end;
--所有人一起涨工资，屁民400，老板1000
declare 
cursor vrows is select * from emp;
vrow emp%rowtype;
begin
  open vrows;
  loop
    fetch vrows into vrow;
    exit when vrows%notfound;
    if vrow.job='PRESIDENT' then
      update emp set sal=sal+1000 where empno=vrow.empno;
      elsif vrow.job='MANAGER' then
        update emp set sal=sal+800 where empno=vrow.empno;
        else
          update emp set sal=sal+400 where empno=vrow.empno;
          end if;
    end loop;
    close vrows;
  end;
/* 程序运行的过程发生异常
异常名 exception 
raise  抛出异常
others 其他异常
*/
  select * from emp;
  /*存储过程：封装在服务器上一段plsql代码片段 
  比如：insert，declare关键字方法，客户端直接调用
  create [or replace] procedure 名称（参数 in|out 类型）
  is | as声明部分
  存储函数：create [or replace] function 名称（参数 in|out 类型）is | as声明部分
  */

  create or replace procedure proc_updatesal(vempno in number,vnum in number)
  is
  vsal number;
  begin
    select sal into vsal from emp where empno=vempno;
    dbms_output.put_line('涨工资前'||vsal);
    update emp set sal=vsal+vnum where empno=vempno;
    dbms_output.put_line('涨工资后'||(vsal+vnum));
    end;
--方式一
    call proc_updatesal(7788,10);
--方式二，好用
declare

begin
  proc_updatesal(7788,100);
  end;
--查询指定员工的年薪
create or replace function func_sal(vempno number) retuen number 
is
vtotalsal number;
begin
  select sal*12+nvl(comm,0) into vtotalsal from emp where empno=vempno;
  return vtotalsal;
  end;
/* 触发器：用户执行了insert，update，delete操作之后，触发一系列其他的动作
   create[or replace] triger触发器的名称
   before|after
   insert|update|delete
   on 表名[for each row]  （for each row表示行级触发器）
   declare begin end;
*/
--新员工入职后输出一句话
create or replace trigger tri_test1
after
insert on emp
declare
begin
   dbms_output.put_line('欢迎');
   end;
   insert into emp(empno,ename) values (9527,'shui');
   select to_char(sysdate,'day') from dual;
--当前星期不能插入数据
create or replace trigger tri_test2
before
insert on emp
declare 
vday varchar2(10);
begin
  select trim(to_char(sysdate,'day')) into vday from dual;
  if vday='星期五' then
    dbms_output.put_line('老板不在可以去睡他的小姨子');
    raise_application_error(-20001,'老板不在可以去睡他的小姨子');
    end if;
    end;
   insert into emp(empno,ename) values(9852,'shui');
   drop trigger tri_test2;
/*触发器分类
语句级：一次
行级：影响多少行输出多少句；
*/
/*
*/
