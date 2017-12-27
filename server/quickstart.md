1.首先执行对应的sql：
```
#创建dbtest1 
drop database if exists dbtest1; 
create database dbtest1; 
use dbtest1; 

create table tb1(
  id int not null,
  gmt datetime
);

#创建dbtest2 
drop database if exists dbtest2; 
create database dbtest2; 
use dbtest2;

create table tb2(
  id int not null,
  val varchar(256)
);

#创建dbtest3 
drop database if exists dbtest3; 
create database dbtest3; 
use dbtest3; 

create table tb2(
  id int not null,
  val varchar(256)
);
```


2.配置相应的规则：

2.1 schema.xml配置如下(注意:schema.xml包含MySQL的IP、端口、用户名、密码等配置，您需要按照注释 替换为您的MySQL信息。)

```
<?xml version="1.0" encoding="UTF-8"?> 
<!DOCTYPE cobar:schema SYSTEM "schema.dtd"> 
<cobar:schema xmlns:cobar="http://cobar.alibaba.com/">
  <!-- schema定义 --> 
  <schema name="dbtest" dataNode="dnTest1">
    <table name="tb2" dataNode="dnTest2,dnTest3" rule="rule1"/>
  </schema>
   
  <!--数据节点定义，数据节点由数据源和其他一些参数组织而成。--> 
  <dataNode name="dnTest1">
    <property name="dataSource"> 
      <dataSourceRef>dsTest[0]</dataSourceRef>
    </property> 
   </dataNode> 
   
  <dataNode name="dnTest2">
    <property name="dataSource"> 
      <dataSourceRef>dsTest[1]</dataSourceRef>
    </property> 
   </dataNode> 

   <dataNode name="dnTest3">
     <property name="dataSource"> 
       <dataSourceRef>dsTest[2]</dataSourceRef>
     </property> 
   </dataNode>

   <!--数据源定义，数据源是一个具体的后端数据连接的表示。--> 
   <dataSource name="dsTest" type="mysql">
     <property name="location"> 
       <location>192.168.0.1:3306/dbtest1</location><!--注意:替换为您的MySQLIP和Port--> 
       <location>192.168.0.1:3306/dbtest2</location><!--注意:替换为您的MySQLIP和Port--> 
       <location>192.168.0.1:3306/dbtest3</location><!--注意:替换为您的MySQLIP和Port--> 
     </property>
  
     <property name="user">test</property><!--注意:替换为您的MySQL用户名-->
     <property name="password"></property><!--注意:替换为您的MySQL密码--
     <property name="sqlMode">STRICT_TRANS_TABLES</property>
   </dataSource> 
</cobar:schema>
```
2.2 rule.xml配置如下(本文仅以数字类型的id字段作为拆分字段，将数据拆分到两个库中。)
```
<?xml version="1.0" encoding="UTF-8"?> 
<!DOCTYPE cobar:rule SYSTEM "rule.dtd"> 
<cobar:rule xmlns:cobar="http://cobar.alibaba.com/">
  <!--路由规则定义，定义什么表，什么字段，采用什么路由算法。--> 
  <tableRule name="rule1">
    <rule>
      <columns>id</columns> 
      <algorithm><![CDATA[func1(${id})]]></algorithm>
    </rule> 
  </tableRule>

  <!--路由函数定义，应用在路由规则的算法定义中，路由函数可以自定义扩展。-->
  <function name="func1" class="com.alibaba.cobar.route.function.PartitionByLong">
    <property name="partitionCount">2</property>
    <property name="partitionLength">512</property> 
  </function>
</cobar:rule>      

server.xml配置如下
<?xml version="1.0" encoding="UTF-8"?> 
<!DOCTYPE cobar:server SYSTEM "server.dtd"> 
<cobar:server xmlns:cobar="http://cobar.alibaba.com/">
  <!--定义Cobar用户名，密码--> 
  <user name="test">
    <property name="password">test</property>
    <property name="schemas">dbtest</property> 
  </user>
</cobar:server>
```

访问Cobar同访问MySQL的方式完全相同, 常用访问方式如下(注意:本文将Cobar部署在192.168.0.1这台机 器上，否则请替换为您的Cobar所在IP，其他信息不变)

2.3 命令行
```
mysql -h192.168.0.1 -utest -ptest -P8066 -Ddbtest
```


3.JDBC(建议5.1以上的mysqldriver版本) 
```
Class.forName("com.mysql.jdbc.Driver");
Connection conn=DriverManager.getConnection("jdbc:mysql://192.168.0.1:8066/dbtest","test", "test");
......
```


4.SQL执行示例，执行语句时与使用传统单一数据库无区别
```
mysql>show databases; 
#dbtest1、dbtest2、dbtest3对用户透明

+----------+ 
|DATABASE  | 
+----------+ 
|dbtest    | 
+----------+

mysql>show tables; 
#dbtest 中有两张表tb1和tb2
+-------------------+
|Tables_in_dbtest1  |
+-------------------+ 
|tb1                | 
|tb2                |
+-------------------+

mysql>insert into tb1(id,gmt) values (1,now()); 
#向表 tb1插入一条数据
mysql>insert into tb2(id,val) values (1,"part1"); 
#向表 tb2插入一条数据 
mysql>insert into tb2(id,val) values (2,"part1"),(513,"part2");
#向表 tb2同时插入多条数据
mysql>select * from tb1; 
#查询表 tb1，验证数据被成功插入
+----+---------------------+
|id  |gmt                  |
+----+---------------------+ 
| 1  |2012-06-1215:00:42   | 
+----+---------------------+
mysql>select * from tb2; 
#查询 tb2，验证数据被成功插入
+-----+-------+
|id   |val    |
+-----+-------+ 
| 1   |part1  | 
| 2   |part1  | 
|513  |part2  | 
+-----+-------+
mysql>select * from tb2 where id in(1,513);
 #根据id 查询
+-----+-------+
|id   |val    |
+-----+-------+ 
| 1   |part1  | 
|513  |part2  |
+-----+-------+

查看后端MySQL数据库dbtest1，dbtest2和dbtest3，验证数据分布在不同的库中

```

