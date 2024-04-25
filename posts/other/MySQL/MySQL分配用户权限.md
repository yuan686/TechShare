# MySQL分配用户权限

> DCL（数据操作语言）
>
> 我们在开发中会出现多个用户管理不同的数据库的情况，而本篇我会讲用户的创建及权限分配操作
>
> 应用场景：
>
> 分配开发、测试、生产数据库的权限

## 1、用户管理

1、查询用户

```sql
use mysql;
select * from user;
```

2、创建用户

```sql
create user '用户名'@'主机名' identified by '密码'
```

**ps：若主机名为localhost，则表示只能本机访问，若为%则表示任意主机都可访问**

3、修改用户密码

```sql
alter user ’用户名'@'主机名' identified with mysql_native_password by '新密码'
```

4、删除用户

```sql
drop user '用户名'@'主机名';
```

## 2、权限控制

1、查询权限

```sql
show grants for '用户名'@'主机名'
```

2、授予权限

```sql
grant 权限列表 on 数据库名.表名 to '用户名'@'主机名';

数据库名.表名： mysql.*    表示授予mysql数据库所有表权限
```

权限列表

| **权限**            | **说明**           |
| ------------------- | ------------------ |
| ALL, ALL PRIVILEGES | 所有权限           |
| SELECT              | 查询数据           |
| INSERT              | 插入数据           |
| UPDATE              | 修改数据           |
| DELETE              | 删除数据           |
| ALTER               | 修改表             |
| DROP                | 删除数据库/表/视图 |
| CREATE              | 创建数据库/表      |

3、撤销权限

```sql
REVOKE 权限列表 ON 数据库名.表名 FROM '用户名'@'主机名';
```

