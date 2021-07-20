
# Hipe-ori-server

A tiny server for syncing point data to users


## API

### 楼层信息查询

#### 简要描述

- 返回可以上传指纹库的楼层信息（暂时仅可以查询，后续可以添加）

#### 请求url

- GET http://xxx/infos

#### 返回样例

```json
[
    {
        "bid": "shilintong",
        "fids": [
            1,
            2,
            3,
            4,
            5
        ],
        "features": [
          {
            "type": "wifi",
            "fid": 1,
            "path": "/uploads/shilingtong/654f8ef1-ee0b-4185-95b9-d105d11f561f-shilintong.db"
          }
        ]
    },
    {
        "bid": "wukan",
        "fids": [
            -2,
            -1
        ],
        "feature": []
    }
]
```

#### 添加楼层信息（指纹文件不在此添加）
- URL `POST http://xxx/infos`

- post body：
```json
[
  {
    "bid": "xxx",
    "fids": [
      1,
      2,
      3,
      4,
      -1
    ],
    "feature": []
  }
]
```
注：其中feature必须是空对象数组，即[]。

#### 删除楼层信息

- URL `DELETE http://xxx/infos/bid`

### 指纹文件上传

#### 简要描述

- 上传采集的指纹库文件

#### 请求url

- baseurl/feature/wifi/{bid}/{fid}
- baseurl/feature/ble/{bid}
- baseurl/feature/pic/{bid}
- baseurl/feature/mag/{bid}/{fid}

bid为大楼名（字母）、fid为楼层（数字）

#### 请求方式

- POST multipart files
上传成功返回200 OK状态码
  
### 指纹文件版本显示

- baseurl/feature/bid

#### 简要描述

通过接口可以访问制定大楼（bid）的指纹消息，以json的格式返回

#### 请求方式

- GET baseurl/feature/version/{bid}

#### 返回事例

```json
[
  {
    "bid": "shilintong",
    "model_num": 1,
    "update_num": 2,
    "signal_type": "wifi/ble/pic/mag"
  }  
]
```

### 指纹下载

#### 简要描述

提供简单的下载指纹文件接口，其中所有文件使用zip压缩

#### 请求url

- GET baseurl/feature/{bid}
- 返回一个zip后缀的文件

### 查询Point接口

#### 简要描述

- 返回用户的采集位置数据

#### 请求URL
- ` http://xx.com/point/id`

#### 请求方式
- GET



#### 返回示例

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

#### 返回参数说明

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

### 数据库格式


-  采集的点信息


|字段|类型|空|默认|注释|
|:----    |:-------    |:--- |:---|------      |
|id    |int    |否 |  |             |
|type |int |否 |    |   数据的类型  |
|time |varchar(50) |否   |    |   数据采集的时间    |
|building_name|string|否|||
|floor|string|否|||
|level|int|否|||
|latitude|string|否||采集数据的纬度|
|longitude|string|否||采集数据的经度|

- 备注：无


### 添加点数据接口

#### 简要描述

- 用户添加采集数据

#### 请求URL
- ` http://xx.com/point `

#### 请求方式
- PATCH

#### 参数

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

#### 返回示例

```
{
	"code": 1
}

```

#### 返回参数说明

|参数名|类型|说明|
|:-----  |:-----|-----                           |
|code |int   |表示成功添加的点个数，-1表示添加失败  |

## 采集Wi-Fi Android app

repo：https://github.com/coda1997/Hipe-localization