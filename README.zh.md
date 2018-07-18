# sealtalk-android
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)<br/>


[The English Version](https://github.com/sealtalk/sealtalk-android/blob/master/README.md)  <br/>
Android 应用 SealTalk 由 融云 RongCloud 出品

## 特点
- Android iOS [Web](http://web.sealtalk.im/) PC(Windows Mac) 多端互通,如果需要你的产品也可以这样实现
- [音视频通话](http://www.rongcloud.cn/docs/android_callkit.html) 功能
- 地理位置,位置共享功能
- 随心的 [消息自定义](http://www.rongcloud.cn/docs/android.html#消息自定义) , [功能模块自定义](http://www.rongcloud.cn/docs/android.html#4、会话扩展功能自定义)
- 完整的好友体系代码示例
- 完整的群组功能代码示例
- 单聊,群组,聊天室多种社交场景满足你的需求,如果还想要 [视频直播](http://rongcloud.cn/live) 都可以
- 一行代码搞定 [客服](http://rongcloud.cn/customservice) ? 你没有听错,比你想象的还要简单
- 未完待续...



## Gif
### 新增红包
![image](https://github.com/sealtalk/sealtalk-android/blob/master/gif/redpacket.gif)<br/>
### 强大的全局搜索
![image](https://github.com/sealtalk/sealtalk-android/blob/master/gif/search.gif)<br/>
### 单人、多人音视频
![image](https://github.com/sealtalk/sealtalk-android/blob/master/gif/audio_video.gif)<br/>
### 客服/机器人服务
![image](https://github.com/sealtalk/sealtalk-android/blob/master/gif/customer_service.gif)<br/>
### 群组
![image](https://github.com/sealtalk/sealtalk-android/blob/master/gif/group.gif)<br/>
## APK
[下载 Apk](http://rongcloud.cn/sealtalk)<br/>

## Jcenter & Maven

![image](https://github.com/sealtalk/sealtalk-android/blob/master/screenshots/maven.png)<br/>

```Java

dependencies {

    compile 'cn.rongcloud.android:IMLib:2.8.6'
    compile 'cn.rongcloud.android:IMKit:2.8.6'
    compile 'cn.rongcloud.android:CallLib:2.8.6'
    compile 'cn.rongcloud.android:CallKit:2.8.6'
    compile 'cn.rongcloud.android:RedPacket:2.8.6'
}

```

[示例 和 使用](https://github.com/13120241790/RongCloudJcenter)<br/>



## UML
 登录类图
 ![image](https://github.com/sealtalk/sealtalk-android/blob/master/screenshots/SealTalk_UML.png)<br/>
 
  Http UML
 
 ![image](https://cloud.githubusercontent.com/assets/15966403/23929940/22a00a3c-0964-11e7-9300-0f86bcee3bda.png)<br/>
 
 感谢 [JerryMissTom](https://github.com/JerryMissTom) 供图
## 使用
#### Step 1:
[下载融云 SDK](http://rongcloud.cn/downloads) Android IMKit SDK包
#### Step 2:
将 IMKit SDK 包当做 Model 导入 Android Studio , 再从 app 工程对其进行引用.
#### Step 3:
阅读 Android [Android 开发文档](http://www.rongcloud.cn/docs/android.html),熟悉 IM 相关概念 和 接口以及初步集成
#### Step 4:
[下载融云 Demo](https://github.com/sealtalk/sealtalk-android) , 在初步集成和依赖好 IMKit 包后, 阅读 demo 源码 Get 你需要的功能模块


## 支持
 - [App 解析文档](https://github.com/sealtalk/sealtalk-android/blob/master/sealtalk_parser.md)
 - [知识库](http://support.rongcloud.cn/)
 - [工单](https://developer.rongcloud.cn/signin?returnUrl=%2Fticket),需要登录融云开发者账号
 - [Android 视频教程](http://www.rongcloud.cn/docs/android_video_tutorials.html)
 
## 鸣谢
- [androidone](https://github.com/devinhu/androidone)
- [android-async-http](https://github.com/loopj/android-async-http)
- [greendao](https://github.com/greenrobot/greenDAO)
- [fastjson](https://github.com/alibaba/fastjson)


#### 比你想象的更强大, 敬请期待更多精彩! <br/>
[融云官网](http://rongcloud.cn/downloads)
