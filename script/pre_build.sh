#!/bin/bash

#  Created by qixinbing on 2019/3/7.
#  Copyright (c) 2019 RongCloud. All rights reserved.

#本脚本用于开发进行本地打包测试

#使用方式 ：终端进入当前目录，执行 sh pre_build.sh

#按需修改下面的参数


# app 版本号
export APP_Version="1.0.0"
export Branch="dev"

cd ..

sh script/build.sh
