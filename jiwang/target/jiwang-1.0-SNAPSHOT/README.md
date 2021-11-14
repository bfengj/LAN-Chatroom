# 行为说明

## 初始化用户

请求路径：/start POST

用于：告诉后端当前用户的用户名、avatarid，接收到此命令后后端才开始之后的行为。注意，此命令并不会被执行一次，每次当用户修改头像或者用户名时都会发起本请求，此时后端需要更新用户名与avatarid并更新心跳信息。

### 请求字段

| 字段名   | 字段值     |
| -------- | ---------- |
| name     | 任意字符串 |
| avatarid | 整数ID     |

### 响应字段

| 字段名 | 字段值        | 备注             |
| ------ | ------------- | ---------------- |
| type   | success/error | 标记成功或者失败 |
| msg    | 错误信息      | 当请求错误时必须 |

## 发送消息

请求路径：/send

### 请求字段

类型 FormData

#### 文本/拍一拍/视频聊天

| 字段名   | 字段值             | 备注                                                         |
| -------- | ------------------ | ------------------------------------------------------------ |
| name     | 任意字符串         |                                                              |
| avatarid | 整数ID             |                                                              |
| type     | text/tap/videochat |                                                              |
| content  | 任意长度字符串     | 当类型为tap和videochat时，内容为拍一拍信息与视频通话信息，由前端进行处理，可当成text信息直接转发 |

#### 图片/音频/视频/文件

| 字段名   | 字段值                             | 备注 |
| -------- | ---------------------------------- | ---- |
| name     | 任意字符串                         |      |
| avatarid | 整数ID                             |      |
| type     | image/audio/video/file             |      |
| content  | 对应格式文件的JavaScript的File对象 |      |

### 响应字段

格式 json

| 字段名 | 字段值        | 备注             |
| ------ | ------------- | ---------------- |
| type   | success/error | 标记成功或者失败 |
| msg    | 错误信息      | 当请求错误时必须 |

## 接受消息

方式:Ajax轮询

接口路径:/receive GET

返回类型：JSON数组

消息体定义如下：

| 字段名   | 字段值                                    | 备注                                         |
| -------- | ----------------------------------------- | -------------------------------------------- |
| name     | 任意字符串                                |                                              |
| avatarid | 整数ID                                    |                                              |
| type     | text/tap/videochat/image/audio/video/file |                                              |
| content  | 任意字符串                                | 仅当类型为text时存在此字段                   |
| src      | 对应文件路径                              | 仅当类型为image/audio/video/file时存在此字段 |

示例：

```json
[{
    "name": "Mion",
    "avatarid": "2",
    "type": "text",
    "content": "您吃了了吗？"
}, {
    "name": "rys<",
    "avatarid": "4",
    "type": "tap",
    "content": "rys< 踢了踢 于美"
}, {
    "name": "Mion",
    "avatarid": "2",
    "type": "audio",
    "src": "127.0.0.1:9000/upload/233.mp3"
}]
```

## 心跳列表

方式:Ajax轮询

接口路径: /heart GET

返回类型：JSON数组，结构如下：

```json
{
    "count":"人数",
    "list":[{
        	"name": "Mion",
        	"avatarid": "2",
    	},
        {
        "name": "rys<",
        "avatarid": "4",
    	}]
}
```

## TODO

使用electron打包

Electron：

- [ ] 实现文件系统级拖动
- [ ] 新消息系统级通知

