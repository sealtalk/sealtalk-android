#SealTalk App 分析
## 分析分为三个模块:
- 登录分析
- 好友逻辑分析
- 群组逻辑分析

> 个人用不信息逻辑比较简单,例如更换个人名称 和 头像。逻辑和群组逻辑分析中的更换群组头像 和 更换群组昵称一样。所以在此不再对个人信息做过多赘述。

---
## 登录分析:
### Step1: Login
确保当前 Login 的 Phone 在 App server 后台已经注册。

接口: http://api.sealtalk.im/user/login

请求方式: post 

请求参数: 国家码 region ; 手机号 phone ; 密码 password;

返回数据:  
- 结果码 以及 cookie 

```
示例数据:{"id":"t1hWCOGvX","token":"B0DA/kKanJviD5xxUzhwsEFIJad0/86YwGxBwz1417WFQi/Vr2OJay26s5IFDffGZaUYRMAkvN0ikvOcTl7RN9JilKZlosfQ"}
```
此处需要值得注意的是:
***Login 成功后获取到的 cookie 你需要缓存起来，后续接口调用都需要将 cookie 加到请求头当中*。**

---

###  Step 2 : Connect RongCloud Sever
Step 1 的 Login 请求接口结果码为 200 时, 会返回 connect 融云服务器所需要的参数 ***Token*** , 此时调用融云方法 RongIM.connect(String token) 连接融云。回调接口 ConnectCallback 回返回三个方法：

- onTokenIncorrect :  token 错误 或者 token 失效, 此处强烈建议重新获取 token 做 reConnect 重连逻辑。
- onSuccess : connect 融云服务端成功后可见 Step 3。
- onError : connect 失败 errorCode.getValue 可获取错误码, 错误码在融云官网开发指南中 可以找到对应的错误原因

---

### Step 3 : 同步数据(好友,群组等数据)

> 仅仅 App server Login 和 连接 融云服务器 后就进入 App 的 MainPage 是不合理的。一些 MainPage 所需要的展示的数据建议在 登录时机做预加载。一个良好的数据架构肯定是需要做缓存,此处以数据库缓存做例子。

以同步群组数据为例:
接口: http://api.sealtalk.im/user/groups (请求需要依赖 Cookie)

请求方式: get

请求参数: 无

返回数据: 结果码 以及 当前用户的群组信息

```
示例数据:
result : [{"role":0,"group":{"id":"pG4lQsHkY","name":"我的群","portraitUri":"","creatorId":"7w0UxC8IB","memberCount":7}},{"role":0,"group":{"id":"lzKDPFE3i","name":"我的"......}]
```

数据获取下来后解析 Json 数据，将数据存储至 Groups 表。同理同步好友 或者 其他数据也是上面的流程。

> 1: 此处同步数据是考虑 MainPage 马上需要数据来做 UI 展示。如果你自己的 App 数据不需要马上展示 则数据同步的时机可以自行选择。
> 2: 个人数据因为数据量比较少 没有必要单独为其建一张表。可以自己做文本存储。此处 Android 是将个人数据存储在 SharedPreferences 当中。

---

### 至此:
- LogIn
- connect
- 同步数据

全部完成后登录流程逻辑结束, App 进入 MainPage 主界面 。

![这里写图片描述](http://img.blog.csdn.net/20160906114024001)

---

## 好友模块分析:

### Step1: 获取好友列表

接口: http://api.sealtalk.im/friendship/all

请求方式: get

请求参数：无 

返回数据:  
- 结果码 以及 好友列表数据。
此接口可以在用户登陆成功之后调用，用于同步当前用户最新的好友列表信息，建议在本地创建数据库，将由接口获取到的好友列表数据存储在本地的数据库中。

```
示例数据:{
    code = 200;
    result =     (
                {
            displayName = "xiao ming";
            message = "\U542c\U96ea\U542c\U4f60\U8bf7\U6c42\U6dfb\U52a0\U4f60\U4e3a\U597d\U53cb";
            status = 20;
            updatedAt = "2016-04-12T09:34:41.000Z";
            user =             {
                id = Uz6Sw8GXx;
                nickname = "\U90d1\U82f1\U541b";
                portraitUri = "http://7xogjk.com1.z0.glb.clouddn.com/Uz6Sw8GXx1472469920221231934";
            };
        }
}
```
* code = 200表示请求成功。
* result对于的json是请求到的好友列表数据。
	1.displayName: 好友备注。
	2.message: 在发送好友邀请时附带的留言。
	3.status: 与好友的关系。下面是好友关系的对照表，上面数据得到的status 值是20，表示和这个用户已经是好友了。
	
	| 对自己的状态|好友 | 自己 | 对好友的状态
	|---
	| 发出了好友邀请 | 10 | 11 | 收到了好友邀请
	| 发出了好友邀请 | 10 | 21 | 忽略了好友邀请
	| 已是好友 | 20 | 20 | 已是好友
	| 已是好友 | 20 | 30 | 删除了好友关系
	| 删除了好友关系 | 30 | 30 | 删除了好友关系
	4.updatedAt：好友数据更新的时间，可以在客户端给好友排序时使用。
	5.user：好友的用户信息。包括好友的用户Id、昵称和头像地址。

### Step2: 按手机号查找用户，判断是否是好友
Tips：由于这个功能没有对应的接口，需要使用通过手机号查找用户和获取到的好友列表信息配合去实现。

####1.通过手机号查找用户
接口: http://api.sealtalk.im/user/find/:region/:phone

请求方式: get 

请求参数: 国家码 region ; 手机号 phone ; 

返回数据:
- 结果码 以及 被查找用户的用户信息数据。

```
示例数据:{
    code = 200;
    result =     {
        id = ozB1VNeh5;
        nickname = joseph;
        portraitUri = "http://7xogjk.com1.z0.glb.clouddn.com/ozB1VNeh51466786331272350098";
    };
}
```
demo 截图

![这里写图片描述](http://img.blog.csdn.net/20160901175354173)

####2.判断此用户是否是好友
查询结果中有userId，我们可以使用这个userId去遍历好友列表数据，从中判断我们通过手机号查询到的用户和自己的好友关系，由此来决定接下来的UI要如何展示。
如果不是好友，那么就应该显示发送好友邀请的UI。
如果是好友，那么就应该显示与该好友发起聊天的UI。

###Step3:发送好友邀请

接口: http://api.sealtalk.im/friendship/invite

请求方式: post 

请求参数：被邀请用户的Id和好友邀请留言。

```
NSDictionary *params = @{
    @"friendId" : userId,
    @"message" : [NSString stringWithFormat:@"我是%@",[RCIM sharedRCIM].currentUserInfo.name]};
```

返回数据:  
 - 结果码。

---

## 群组逻辑分析:

### 细分功能如下
1.  群组创建
2.  群组邀请
3.  群组退出
4.  群组解散
5.  群组移除成员
6.  群组信息变更
7.  群组成员信息
8.  获取我的群组
9.  群组通知


>  1：下列所有接口的 Host 均为： http://api.sealtalk.im/
>  2:     以下所有接口调用请确保请求中已包含 Cookie
>  3： 示例数据中的状态码均为 200. 实际参考 [SealTalk Server](https://github.com/sealtalk/sealtalk-server)

---

## Group: Creator && Member
下面的用例图展示 群组 和 群组成员,对于某个群可执行的逻辑

![这里写图片描述](http://img.blog.csdn.net/20160901153831115)

> 1 . 此图展示仅为 SealTalk Group Creator && Member 逻辑展示 ，你的产品可根据产品需求做对应调整
> 2 .当前用户在群组中的权限 由 ***role*** 字段表示

---



## 移动端 群组表 & 群组成员表
Group table Column:

| groupId | name | portraitUri |displayName|role|timestamp|
|---

<br>
GroupMember table Column:

|groupId|userId|name|portraitUri|displayName|
|---


---




### 一:群组创建
接口: http://api.sealtalk.im/group/create

请求方式: post

请求参数: 

- name 群组名 ;
- memberIds 个数至少大于 1 的一组群组成员 id 数据；

返回数据: 

```
 示例数据:
 code : 200
 result : {"id":"ArVtlWJSv"}
```


> memberIds 从自己的好友列表中获取
> 创建群组的时候可选同时上传自己的群组头像(接口见下列群组信息变更)

![图片在联网状态下可见](http://img.blog.csdn.net/20160901151426730)

---

## 二:群组邀请

> 群组邀请 即是 群组中的成员已定的情况下另外邀请用户加入你的群组, 已在群组中的用户不允许重复邀请, 一次可以邀请一个或者多个用户。


接口: http://api.sealtalk.im/group/add

请求方式 : post

请求参数 : 

- groupId 群组 Id
- memberIds 个数至少大于 1 的一组群组成员 id 数据；

返回数据:

```
code : 200
```

![图片在联网状态下可见](http://img.blog.csdn.net/20160901153225841)

---

## 三:群组退出

接口: http://api.sealtalk.im/group/quit

请求方式 : post

请求参数 : 

- groupId 群组 Id

返回数据:

```
code : 200
```

![图片在联网状态下可见](http://img.blog.csdn.net/20160901154916275)

> quit 接口供群成员自行退出群组使用，另外建议在 IM 融云模块中 如果用户自行退出群组, 建议将本地 会话列表 的 item && cell 会话删除。

---

## 四:群组解散

接口: http://api.sealtalk.im/group/dismiss

请求方式 : post

请求参数 : 

- groupId 群组 Id

返回数据:

```
code : 200
```

> 1. 群组解散和上面退出的配图基本一致, 所以此处不展示配图
> 2. 群组解散后 群组成员将从服务端被退出该群组
> 3. 群组解散接口只有该群创建者可使用

---

## 五: 群组移除成员

接口: http://api.sealtalk.im/group/kick

请求方式 : post

请求参数 : 

- groupId 群组 Id
- memberIds 个数至少大于 1 的一组群组成员 id 数据；

返回数据:

```
code : 200
```

![图片在联网状态下可见](http://img.blog.csdn.net/20160901160850235)

> 1.被移除的 memberId 必须为群组成员 userId 
> 2.仅群组 创建者 有权限执行此操作

---

## 六:群组信息变更

- 群组名称变更
- 群组头像变更

### （1）群组名称变更

接口: http://api.sealtalk.im/group/rename

请求方式 : post

请求参数 : 

- groupId 群组 Id
- name     新的群组名称

返回数据:

```
code : 200
```

![这里写图片描述](http://img.blog.csdn.net/20160901162209368)

### (2)  群组头像变更


接口: http://api.sealtalk.im/group/set_portrait_uri

请求方式 : post

请求参数 : 

- groupId 群组 Id
- portraitUri     群组头像的 Url

返回数据:

```
code : 200
```

![这里写图片描述](http://img.blog.csdn.net/20160901164147635)

> SealTalk 的上传头像是存储 http:// url , 实际存储是在 七牛 云存储上。自己的产品可以自行选择图片存储方式

---

## 七:群组成员信息

> 获取群组成员信息有两种方式 : 1. 获取某个群的群组成员信息  2. 获取全群组的群组成员信息

### (1) 获取某个群的群组成员信息

接口: http://api.sealtalk.im/group/:groupId/members

请求方式 : get

请求参数 : 

- groupId 群组 Id

返回数据:

```
code : 200
result : [{"displayName":"","role":1,"createdAt":"2016-01-26T08:50:57.000Z","user":{"id":"6nx4DGtCu","nickname":"超时分辨率","portraitUri"......}]
```

> 此接口适用于点击群组详情页检查该群成员是否有数据变更, 如果有数据变更则更新 GroupMember 数据库

![这里写图片描述](http://img.blog.csdn.net/20160901170131476)

### (2) 获取全群组的群组成员


接口: http://api.sealtalk.im/user/sync/:version

请求方式 : get

请求参数 : 

- 无

返回数据:

```
  code : 200
  result : {"version":1234567894,"user":{"id":"sdf9sd0df98","nickname":"Tom","portraitUri":"http://test.com/user/abc123.jpg","timestamp":1234567891},"blacklist":[{"friendId":"sdf9sd0df98","status":true,"timestamp":1234567891}],"friends":[{"friendId":"sdf9sd0df98","displayName":"Jerry","status":20,"timestamp":1234567892}],"groups":[{"displayName":"Ironman","role":1,"isDeleted":true,"group":{"id":"sdf9sd0df98","name":"Team 1","portraitUri":"http://test.com/group/abc123.jpg","timestamp":1234567893}}],"group_members":[{"groupId":"cvx989vxc9","memberId":"sdf9sd0df98","displayName":"Ironman","role":1,"isDeleted":true,"timestamp":1234567893,"user":{"nickname":"Tom","portraitUri":"http://test.com/user/abc123.jpg"}}]}
```

> 此接口适用于登录同步数据 和 检查数据变更，数据内包含 个人数据，好友数据，黑名单数据，群组数据，群组成员数据。此处获取到群组成员数据可插入 GroupMember 供后续 UI 展示调用

---

## 八:获取我的群组

接口: http://api.sealtalk.im/user/groups

请求方式 : get

请求参数 : 

- 无

返回数据:

```
 code : 200
 result : [{"role":0,"group":{"id":"pG4lQsHkY","name":"我的群","portraitUri":"","creatorId":"7w0UxC8IB","memberCount":7}}]
```

> 此接口适用于登录的时候预加载群组数据至本地 Groups 数据库, 数据提供给 UI 界面展示调用。此接口可使用  七: 群组成员 当中的 sync 接口替代。

![这里写图片描述](http://img.blog.csdn.net/20160901170859858)

---

## 九: 群组通知

> 群组通知是群成员 或者 群创建者 对群做操作交互通知。群组中的成员皆会收到这个通知。以便做对应的 UI 展示 和 数据更新

目前 SealTalk 以 GroupNotificationMessage (ObjectName:RC:GrpNtf) 来做通知下发，当然你也可以自定义消息。此消息由 App server 下发到 客户端，SealTalk 被通知的情况有:

- add 邀请 或者 加入群组时
- quit 退出群组时
- kicked 被创建者移除群组时
- rename 群组更名时
- bulletin 群公告变更时
- create 群组创建时

![这里写图片描述](http://img.blog.csdn.net/20160901172148566)

---

## End:

### 上面已经将登录模块，好友模块，群组模块做了详细介绍,相信你如果仔细阅读了上面文档 IM 的相关逻辑对你来说应该没有问题(建议下载 [安装包](http://rongcloud.cn/sealtalk))边看文档边直接操作。