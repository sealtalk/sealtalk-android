package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.RegionResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import io.rong.imkit.utils.language.LangUtils;

public class CountryViewModel extends AndroidViewModel {


    private final UserTask userTask;
    private SingleSourceMapLiveData<Resource<List<RegionResult>>, Resource<List<CountryInfo>>> countryInfoList;
    private MediatorLiveData<Resource<List<CountryInfo>>> filterCountryList = new MediatorLiveData<>();
    //汉字转换成拼音的类
    private CharacterParser characterParser;
    private LangUtils.RCLocale locale;


    public CountryViewModel(@NonNull Application application) {
        super(application);
        userTask = new UserTask(application);
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        LangUtils.RCLocale local = local(application);

        // 监听国家接口请求变化， 然后重写封装数据。
        countryInfoList = new SingleSourceMapLiveData<>(new Function<Resource<List<RegionResult>>, Resource<List<CountryInfo>>>() {
            @Override
            public Resource<List<CountryInfo>> apply(Resource<List<RegionResult>> input) {
                if (input.status == Status.LOADING) {
                    return Resource.loading(null);
                }

                if (input.status == Status.ERROR) {
                    return Resource.error(input.code, null);
                }
                List<RegionResult> regionList = input.data;
                List<CountryInfo> countryInfos = new ArrayList<>();
                if (regionList != null) {
                    for (RegionResult region : regionList) {
                        CountryInfo countryInfo = new CountryInfo();

                        if (local == LangUtils.RCLocale.LOCALE_CHINA) {
                            countryInfo.setCountryName(region.locale.zh);
                        } else if (local == LangUtils.RCLocale.LOCALE_US) {
                            countryInfo.setCountryName(region.locale.en);
                        } else {
                            countryInfo.setCountryName(region.locale.en);
                        }
                        countryInfo.setCountryNameCN(region.locale.zh);
                        countryInfo.setCountryNameEN(region.locale.en);
                        countryInfo.setZipCode("+" + region.region);
                        //汉字转换成拼音
                        String namePinyin = characterParser.getSpelling(countryInfo.getCountryName());
                        String firstChar = namePinyin.substring(0, 1).toUpperCase();
                        // 正则表达式，判断首字母是否是英文字母
                        if (firstChar.matches("[A-Z]")) {
                            countryInfo.setFirstChar(firstChar.toUpperCase());
                        } else {
                            countryInfo.setFirstChar("#");
                        }
                        countryInfos.add(countryInfo);
                        Collections.sort(countryInfos, new Comparator<CountryInfo>() {
                            @Override
                            public int compare(CountryInfo lhs, CountryInfo rhs) {
                                return lhs.getFirstChar().compareTo(rhs.getFirstChar());
                            }
                        });
                    }
                }

                Resource<List<CountryInfo>> resource = new Resource<>(input.status, countryInfos, input.code);
                return resource;
            }
        });

    }

    /**
     * 获取当前的本地化语言
     *
     * @param context
     * @return
     */
    private LangUtils.RCLocale local(Context context) {
        LangUtils.RCLocale appLocale = LangUtils.getAppLocale(context);
        if (LangUtils.RCLocale.LOCALE_CHINA == appLocale
                || LangUtils.RCLocale.LOCALE_US == appLocale) {
            return appLocale;
        } else {
            //todo
            return LangUtils.RCLocale.LOCALE_CHINA;
//            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
//            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
//                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, context);
//                return LangUtils.RCLocale.LOCALE_CHINA;
//            } else {
//                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, context);
//                return LangUtils.RCLocale.LOCALE_US;
//            }
        }
    }

    /**
     * 国家信息监听
     *
     * @return
     */
    public LiveData<Resource<List<CountryInfo>>> getFilterCountryList() {
        return filterCountryList;
    }

    /**
     * 获取国家信息
     *
     * @param filterStr
     */
    public void loadCountryDatas(String filterStr) {
        if (TextUtils.isEmpty(filterStr)) {
            if (countryInfoList.getValue() == null || countryInfoList.getValue().status != Status.SUCCESS) {
                filterCountryList.addSource(countryInfoList, new Observer<Resource<List<CountryInfo>>>() {
                    @Override
                    public void onChanged(Resource<List<CountryInfo>> listResource) {
                        if (listResource.status != Status.LOADING) {
                            filterCountryList.removeSource(countryInfoList);
                        }

                        filterCountryList.postValue(listResource);
                    }
                });
                countryInfoList.setSource(userTask.getRegionList());
            } else {
                filterCountryList.postValue(countryInfoList.getValue());
            }

        } else {
            List<CountryInfo> infos = countryInfoList.getValue().data;
            if (infos != null) {
                List<CountryInfo> countryInfos = new ArrayList<>();

                for (CountryInfo info : infos) {
                    String name = info.getCountryName();
                    if (name.indexOf(filterStr) != -1 || characterParser.getSpelling(name).startsWith(filterStr)) {
                        countryInfos.add(info);
                    }
                }
                // 根据a-z进行排序
                Collections.sort(countryInfos, new Comparator<CountryInfo>() {
                    @Override
                    public int compare(CountryInfo lhs, CountryInfo rhs) {
                        return lhs.getFirstChar().compareTo(rhs.getFirstChar());
                    }
                });

                Resource<List<CountryInfo>> resource = new Resource<>(countryInfoList.getValue().status, countryInfos, countryInfoList.getValue().code);
                filterCountryList.postValue(resource);
            }
        }
    }

}
