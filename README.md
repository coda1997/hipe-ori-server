
## Hipe-ori-server

A tiny server for syncing point data to users


### API

#### 查询Point接口

##### 简要描述

- 返回用户的采集位置数据

##### 请求URL
- ` http://xx.com/point/id`

##### 请求方式
- GET



##### 返回示例

``` 
  [
  	{
		"id":1,
		"type":1,
		"time":"2020-09-10 12:00:21",
		"building_name":"shilintong",
		"floor":"1",
		"level":1，
		"latitude":"100.1",
		"longitude":"200.1"
	},
	...
  ]
```

##### 返回参数说明

|参数名|类型|说明|
|:-----  |:-----|-----                           |
|id |int   |id是数据库中点的唯一标记，自增  |
|type|int|1:wifi, 2:ble, 3:mag,4:img  |
|time|string|数据采集的时间|
|building_name|string|采集的大楼name|
|floor|string|采集的具体楼层|
|level|int|信号强度，其中范围是1-100，-1表示该数据的类型没有强度值|
|latitude|string|采集数据的纬度|
|longitude|string|采集数据的经度|

#### 数据库格式


-  采集的点信息

|字段|类型|空|默认|注释|
|:----    |:-------    |:--- |-- -|------      |
|id    |int    |否 |  |             |
|type |int |否 |    |   数据的类型  |
|time |varchar(50) |否   |    |   数据采集的时间    |
|building_name|string|否|||
|floor|string|否|||
|level|int|否|||
|latitude|string|否||采集数据的纬度|
|longitude|string|否||采集数据的经度|
- 备注：无


#### 添加点数据接口

##### 简要描述

- 用户添加采集数据

##### 请求URL
- ` http://xx.com/point `

##### 请求方式
- PATCH

##### 参数

``` 
  [
  	{
		"id":1,
		"type":1,
		"time":"2020-09-10 12:00:21",
		"building_name":"shilintong",
		"floor":"1",
		"level":1，
		"latitude":"100.1",
		"longitude":"200.1"
	}
  ...
  ]
```

##### 返回示例

```
{
	"code": 1
}

```

##### 返回参数说明

|参数名|类型|说明|
|:-----  |:-----|-----                           |
|code |int   |表示成功添加的点个数，-1表示添加失败  |

