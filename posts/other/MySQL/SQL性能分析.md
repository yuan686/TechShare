# SQL性能分析

## 1.SQL执行频率查询

MySQL 客户端连接成功后，通过 **show [session|global] status** 命令可以提供服务器状态信息。通过如下指令，可以查看当前数据库的INSERT、UPDATE、DELETE、SELECT的访问频次：

```sql
-- session 是查看当前会话 ;
-- global 是查询全局数据 ;
SHOW GLOBAL STATUS LIKE 'Com_______';
```

## 2.慢查询日志

慢查询日志**记录了所有执行时间超过指定参数（long_query_time，单位：秒，默认10秒）的所有SQL语句的日志**。

MySQL的慢查询日志默认没有开启，我们可以查看一下系统变量 slow_query_log

```sql
show variables like 'slow_query_log';
```

如果要开启慢查询日志，需要在MySQL的配置文件（/etc/my.cnf）中配置如下信息：

```cnf
# 开启MySQL慢日志查询开关
slow_query_log=1

# 设置慢日志的时间为2秒，SQL语句执行时间超过2秒，就会视为慢查询，记录慢查询日志
long_query_time=2
```

配置完毕之后，通过以下指令重新启动MySQL服务器进行测试，查看慢日志文件中记录的信息 /var/lib/mysql/localhost-slow.log。

```bash
systemctl restart mysqld
```

当我们执行sql查询，时间超过2s就会记录到日志当中。

查询日志的命令

```bash
tail -f localhost-slow.log
```

## 3.profile详情

show profiles 能够在做SQL优化时帮助我们了解时间都耗费到哪里去了。通过have_profiling参数，能够看到当前MySQL是否支持profile操作：

```sql
SELECT @@have_profiling ;
```

> 查询profile开关是否关闭

```sql
select @@profiling;
```

> 可以通过set语句在**session/global**级别开启profiling

```sql
SET profiling = 1;
```

> 查询SQL的耗时基本情况

```sql
-- 查看每一条SQL的耗时基本情况
show profiles;

-- 查看指定query_id的SQL语句各个阶段的耗时情况
show profile for query query_id;

-- 查看指定query_id的SQL语句CPU的使用情况
show profile cpu for query query_id;
```

## 4.explain执行计划

**EXPLAIN 或者 DESC**命令获取 MySQL 如何执行 SELECT 语句的信息，包括在 SELECT 语句执行过程中表如何连接和连接的顺序。

```sql
EXPLAIN SELECT 字段列表 FROM 表名 WHERE 条件 ;
```

![image-20240423221808588](SQL%E6%80%A7%E8%83%BD%E5%88%86%E6%9E%90.assets/image-20240423221808588.png)

| **字段**     | **含义**                                                     |
| ------------ | ------------------------------------------------------------ |
| id           | select查询的序列号，表示查询中执行select子句或者是操作表的顺序(**id相同，执行顺序从上到下；id不同，值越大，越先执行**)。 |
| select_type  | 表示 SELECT 的类型，常见的取值有 SIMPLE（简单表，即不使用表连接或者子查询）、PRIMARY（主查询，即外层的查询）、UNION（UNION 中的第二个或者后面的查询语句）、SUBQUERY（SELECT/WHERE之后包含了子查询）等 |
| type         | 表示连接类型，性能由好到差的连接类型为NULL、system、const、eq_ref、ref、range、 index、all 。 |
| possible_key | 显示可能应用在这张表上的索引，一个或多个。                   |
| key          | 实际使用的索引，如果为NULL，则没有使用索引。                 |
| key_len      | 表示索引中使用的字节数， 该值为索引字段最大可能长度，并非实际使用长度，在不损失精确性的前提下， 长度越短越好 。 |
| rows         | MySQL认为必须要执行查询的行数，在innodb引擎的表中，是一个估计值， |
| filtered     | 表示返回结果的行数占需读取行数的百分比， filtered 的值越大越好。 |



































