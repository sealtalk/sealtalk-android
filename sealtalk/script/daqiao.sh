#进入 android-sealtalk 目录，运行此脚本，再编译生成的 SealTalk 可用于测试 Oppo 或 vivo 推送。
#此脚本为本地运行脚本, 不支持 Ubuntu 环境.
sed -i '' -e 's!SEALTALK_SERVER="[^"]*"!SEALTALK_SERVER="http://sealtalk-server-java.rongcloud.net:8080"!g' ../gradle.properties
sed -i '' -e 's!SEALTALK_APP_KEY="[^"]*"!SEALTALK_APP_KEY="c9kqb3rdkbb8j"!g' ../gradle.properties
sed -i '' -e 's!SEALTALK_NAVI_SERVER="[^"]*"!SEALTALK_NAVI_SERVER="navqa.cn.ronghub.com"!g' ../gradle.properties
