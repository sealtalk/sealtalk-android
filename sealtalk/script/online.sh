#进入 android-sealtalk 目录，运行此脚本，再编译生成的 SealTalk 可用于测试 Oppo 或 vivo 推送。
#此脚本为本地运行脚本, 不支持 Ubuntu 环境.
sed -i '' -e 's!SEALTALK_SERVER="[^"]*"!SEALTALK_SERVER="http://api-sealtalk.rongcloud.cn"!g' ../gradle.properties
sed -i '' -e 's!SEALTALK_APP_KEY="[^"]*"!SEALTALK_APP_KEY="n19jmcy59f1q9"!g' ../gradle.properties
sed -i '' -e 's!SEALTALK_NAVI_SERVER="[^"]*"!SEALTALK_NAVI_SERVER="nav.cn.ronghub.com"!g' ../gradle.properties
