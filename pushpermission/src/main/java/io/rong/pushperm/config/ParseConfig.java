package io.rong.pushperm.config;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import io.rong.pushperm.log.LLog;



/**
 * 用于 res/xml/ config 文件的解析。 根据机型解析对应的 xml 里面配置信息。
 * 并封装成 {@link ModelInfo}.
 */
class ParseConfig {
	
	private static final String TAG = "ParseConfig";

	private static final String AUTO_START = "AutoStart";
	private static final String NOTIFICATION = "Notification";
	private static final String CLEAN = "Clean";

	private static final String INTENT_INFO = "IntentInfo";
	private static final String PACKAGE_NAME = "Package";
	private static final String CLASS_NAME = "Clazz";
	private static final String ACTION = "Action";
	private static final String CATEGOTY = "Category";
	private static final String SET_APTH = "SetPath";
	private static final String DATA = "Data";
	private static final String EXTRA = "Extra";
	private static final String KEY = "Key";
	private static final String VALUE = "Value";

	private static final String DEF_CONFIG = "config";



	/**
	 * 解析 当前机型的 xml 配置信息
	 * @param context 上下文对象
	 * @param manufacturer 机型
	 * @return 返回封装好的配置信息 {@link ModelInfo}
	 */
	public static ModelInfo parse(Context context, String manufacturer) {
		return parse(context, DEF_CONFIG, manufacturer);
	}

	/**
	 * 解析 当前机型的 xml 配置信息
	 * @param context 上下文对象
	 * @param xmlResName 配置文件资源
	 * @param manufacturer 机型
	 * @return 返回封装好的配置信息 {@link ModelInfo}
	 */

	public static ModelInfo parse(Context context, String xmlResName, String manufacturer) {
		try {
			int xmlResId = context.getResources().getIdentifier(xmlResName, "xml", context.getPackageName());
			if (xmlResId <= 0) {
				LLog.e(TAG, "config.xml is not found");
				return null;
			}
			XmlResourceParser xml = context.getResources().getXml(xmlResId);
			return parseXmlToJavaBean(xml, manufacturer);
		} catch (Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	// 解析逻辑
	private static ModelInfo parseXmlToJavaBean( XmlResourceParser xmlPullParser, String manufacturer ) {
		ModelInfo modelInfo = null;
		try {
			ModelInfo.Info info = null;
			ModelInfo.Param param = null;
			String currTag = "";
			int event = xmlPullParser.getEventType();
			boolean isEnd = false;
			while (event != XmlPullParser.END_DOCUMENT && !isEnd ) {
				switch (event) {
					case XmlPullParser.START_TAG:
						LLog.d(TAG, "START_TAG => " + xmlPullParser.getName());
						if (xmlPullParser.getName().equalsIgnoreCase(manufacturer)) {
							LLog.d(TAG, "Parse started =>" + manufacturer);
							modelInfo = new ModelInfo();
							modelInfo.manufacturer = manufacturer;
						} else if (xmlPullParser.getName().equals(AUTO_START)){
							if (modelInfo != null) {
								if (modelInfo.autoStart == null) {
									modelInfo.autoStart = new ArrayList<>();
								}
								currTag = xmlPullParser.getName();
							}
						}  else if (xmlPullParser.getName().equals(NOTIFICATION)){
							if (modelInfo != null) {
								if (modelInfo.notification == null) {
									modelInfo.notification = new ArrayList<>();
								}
								currTag = xmlPullParser.getName();
							}
						} else if (xmlPullParser.getName().equals(CLEAN)){
							if (modelInfo != null) {
								if (modelInfo.lockClean == null) {
									modelInfo.lockClean = new ArrayList<>();
								}
								currTag = xmlPullParser.getName();
							}
						} else if (xmlPullParser.getName().equals(INTENT_INFO)) {
								info = new ModelInfo.Info();
						} else if (xmlPullParser.getName().equals(PACKAGE_NAME)){
							String packageName = xmlPullParser.nextText();
							if (info != null && !TextUtils.isEmpty(packageName)) {
								info.packageName = packageName.trim();
							}

						} else if (xmlPullParser.getName().equals(CLASS_NAME)) {
							String clazzName = xmlPullParser.nextText();
							if (info != null  && !TextUtils.isEmpty(clazzName)) {
								info.clazzName = clazzName.trim();
							}

						} else if (xmlPullParser.getName().equals(ACTION)) {
							String actionName = xmlPullParser.nextText();
							if (info != null && !TextUtils.isEmpty(actionName)) {
								info.actionName = actionName.trim();
							}
						} else if (xmlPullParser.getName().equals(CATEGOTY)) {
							String catagoty = xmlPullParser.nextText();
							if (info != null && !TextUtils.isEmpty(catagoty)) {
								info.catagoty = catagoty.trim();
							}
						} else if (xmlPullParser.getName().equals(SET_APTH)) {
							String setPath = xmlPullParser.nextText();
							if (info != null && !TextUtils.isEmpty(setPath)) {
								info.setPath = setPath.trim();
							}
						} else if (xmlPullParser.getName().equals(DATA)) {
							if (info != null) {
								param = new ModelInfo.Param();
							}
						}else if (xmlPullParser.getName().equals(EXTRA)) {
							if (info != null) {
								param = new ModelInfo.Param();
							}
						}else if (xmlPullParser.getName().equals(KEY)) {
							String key = xmlPullParser.nextText();
							if (param != null && !TextUtils.isEmpty(key)) {
								param.key = key.trim();
							}
						}else if (xmlPullParser.getName().equals(VALUE)) {
							String value = xmlPullParser.nextText();
							if (param != null && !TextUtils.isEmpty(value)) {
								param.value = value.trim();
							}
						}
						break;
					case XmlPullParser.END_TAG:
						LLog.d(TAG, "END_TAG => " + xmlPullParser.getName());
						if (xmlPullParser.getName().equalsIgnoreCase(manufacturer)) {
							LLog.d(TAG, "Parse completed => " + manufacturer);
                            isEnd = true;
						} else if (xmlPullParser.getName().equals(AUTO_START) ||xmlPullParser.getName().equals(NOTIFICATION)
								|| xmlPullParser.getName().equals(CLEAN)){
								currTag = null;
						} else if (xmlPullParser.getName().equals(INTENT_INFO)) {
								if (modelInfo != null  && info != null) {
									if (AUTO_START.equals(currTag) && modelInfo.autoStart != null) {
										modelInfo.autoStart.add(info);
									} else if (NOTIFICATION.equals(currTag) && modelInfo.notification != null) {
										modelInfo.notification.add(info);
									}  else if (CLEAN.equals(currTag) && modelInfo.lockClean != null) {
										modelInfo.lockClean.add(info);
									}
								}
						} else if (xmlPullParser.getName().equals(DATA)) {
							if (info != null) {
								info.dataUri = param;
							}
						}else if (xmlPullParser.getName().equals(EXTRA)) {
							if (info != null) {
								if (info.extras == null) {
									info.extras = new ArrayList<>();
								}
								info.extras.add(param);
							}
						}
						break;
				}
				event = xmlPullParser.next();
			}
		} catch (Exception e) {
			LLog.e(TAG, "parse xml exception" + e.getMessage());
			throw new RuntimeException("parse xml exception" + e.getMessage());
		} finally {
			xmlPullParser.close();
		}
		return modelInfo;
	}


}
