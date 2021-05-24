#进入 android-sealtalk 目录，运行此脚本，再编译生成的 SealTalk 可用于测试 Oppo 或 vivo 推送。
#此脚本为本地运行脚本, 不支持 Ubuntu 环境.
sed -i '' -e 's!applicationId "[^"]*"!applicationId "cn.rongcloud.sealtalk"!g' ../build.gradle
sed -i '' -e 's!"package_name": "[^"]*"!"package_name": "cn.rongcloud.sealtalk"!g' ../agconnect-services.json
sed -i '' -e 's!"package_name": "[^"]*"!"package_name": "cn.rongcloud.sealtalk"!g' ../google-services.json
