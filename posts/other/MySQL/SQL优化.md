# SQL优化

## 插入数据

### insert优化

方案一：批量插入数据（建议不超过1000条）

```sql
Insert into tb_test values(1,'Tom'),(2,'Cat'),(3,'Jerry'); 
```

方案二：手动控制事务

```sql
start transaction;

insert into tb_test values(1,'Tom'),(2,'Cat'),(3,'Jerry');
insert into tb_test values(4,'Tom'),(5,'Cat'),(6,'Jerry');
insert into tb_test values(7,'Tom'),(8,'Cat'),(9,'Jerry');

commit;
```

方案三：主键顺序插入，性能高于顺序插入

### 大批量插入数据

如果一次性需要插入大批量数据(比如: 几百万的记录)，使用insert语句插入性能较低，此时可以使用MySQL数据库提供的load指令进行插入。操作如下：

```sql
-- 客户端连接服务端时，加上参数 -–local-infile
mysql –-local-infile -u root -p

-- 设置全局参数local_infile为1，开启从本地加载文件导入数据的开关
set global local_infile = 1;

-- 执行load指令将准备好的数据，加载到表结构中
load data local infile '/root/sql1.log' into table tb_user fields
terminated by ',' lines terminated by '\n' ;
```

## 主键优化

###  数据组织方式

在InnoDB存储引擎中，表数据都是**根据主键顺序组织存放**的，这种存储方式的表称为<font color='red'>索引组织表（index organized table IOT）</font>

###  页分裂

> 在InnoDB引擎中，数据行是记录在逻辑结构 page 页中的，而每一个页的大小是固定的，默认16K。那也就意味着， 一个页中所存储的行也是有限的，如果插入的数据行row在该页存储不小，将会存储到下一个页中，页与页之间会通过指针连接。

页可以为空，也可以填充一半，也可以填充100%。**每个页包含了2-N行数据(如果一行数据过大，会行溢出)**，根据主键排列

 在乱序插入时，由于索引结构的叶子节点是有顺序的，而当插入的位置页数据达到16k时，会见此页后一半数据移动到其他页，再重新设置链表指针。

![image-20240424221955030](SQL%E4%BC%98%E5%8C%96.assets/image-20240424221955030.png)

![image-20240424222002414](SQL%E4%BC%98%E5%8C%96.assets/image-20240424222002414.png)

![image-20240424222039253](SQL%E4%BC%98%E5%8C%96.assets/image-20240424222039253.png)

### 页合并

当我们对已有数据进行删除时，实际上记录并没有被物理删除，只是记录被标记（flaged）为删除并且它的空间变得允许被其他记录声明使用。

![image-20240424222149991](SQL%E4%BC%98%E5%8C%96.assets/image-20240424222149991.png)

当页中删除的记录达到 MERGE_THRESHOLD（默认为页的50%），InnoDB会开始寻找最靠近的页（前或后）看看是否可以将两个页合并以优化空间使用。

![image-20240424222209992](SQL%E4%BC%98%E5%8C%96.assets/image-20240424222209992.png)

<font color='red'>MERGE_THRESHOLD：合并页的阈值，可以自己设置，在创建表或者创建索引时指定。</font>

### 主键设计原则

- 满足业务需求的情况下，**尽量降低主键的长度**。
- 插入数据时，**尽量选择顺序插入，选择使用AUTO_INCREMENT自增主键**。
- **尽量不要使用UUID做主键**或者是其他自然主键，如身份证号。
- 业务操作时，**避免对主键的修改**。

## order by优化

> MySQL的排序，有两种方式

Using filesort : 通过表的索引或全表扫描，读取满足条件的数据行，然后**在排序缓冲区sortbuffer中完成排序操作**，所有**不是通过索引直接返回排序结果的排序都叫 FileSort 排序**。

Using index : **通过有序索引顺序扫描直接返回有序数据**，这种情况即为 using index，不需要额外排序，**操作效率高。**





