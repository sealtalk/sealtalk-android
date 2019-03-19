package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.CountryZipModel;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.SideBar;
import cn.rongcloud.im.server.response.GetRegionListResponse;
import cn.rongcloud.im.ui.adapter.CountryAdapter;
import cn.rongcloud.im.utils.CharacterParser;
import io.rong.common.RLog;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.utilities.LangUtils;


public class SelectCountryActivity extends BaseActivity implements Comparator<CountryZipModel> {
    private static final String TAG = SelectCountryActivity.class.getSimpleName();
    private static final int REQUEST_REGION_LIST = 1;
    private ListView listView;
    private SideBar sideBar;
    //汉字转换成拼音的类
    private CharacterParser characterParser;

    private ArrayList<CountryZipModel> countryZipModels;
    private CountryAdapter adapter;
    private EditText mSearchEditText;
    private ImageView mPressBackImageView;
    private LangUtils.RCLocale mLocale;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_country);
        setHeadVisibility(View.GONE);
        initView();

        countryZipModels = new ArrayList<>();

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        LangUtils.RCLocale appLocale = LangUtils.getAppLocale(this);
        if (LangUtils.RCLocale.LOCALE_CHINA == appLocale
            || LangUtils.RCLocale.LOCALE_US == appLocale) {
            mLocale = appLocale;
        }else {
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, this);
                mLocale = LangUtils.RCLocale.LOCALE_CHINA;
            } else {
                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, this);
                mLocale = LangUtils.RCLocale.LOCALE_US;
            }
        }

//        String[] counties = getResources().getStringArray(R.array.country_zip_code_list);
//        for (int i = 0; i < counties.length; i++) {
//            String[] countryInfo = counties[i].split("\\*");
//            CountryZipModel countryZipModel = new CountryZipModel();
//            countryZipModel.setCountryName(countryInfo[0]);
//            countryZipModel.setZipCode("+" + countryInfo[1]);
//            //汉字转换成拼音
//            String namePinyin = characterParser.getSelling(countryInfo[0]);
//            String firstChar = namePinyin.substring(0, 1).toUpperCase();
//            // 正则表达式，判断首字母是否是英文字母
//            if (firstChar.matches("[A-Z]")) {
//                countryZipModel.setFirstChar(firstChar.toUpperCase());
//            } else {
//                countryZipModel.setFirstChar("#");
//            }
//            countryZipModels.add(countryZipModel);
//        }
//
//        Collections.sort(countryZipModels, this);

        adapter = new CountryAdapter(this, countryZipModels);
        listView.setAdapter(adapter);

        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    listView.setSelection(position);
                }
            }
        });

        request(REQUEST_REGION_LIST);
    }


    public void initView() {
        listView = (ListView) findViewById(R.id.lv_select_country);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        mSearchEditText = (EditText) findViewById(R.id.ac_et_search);
        mPressBackImageView = (ImageView) findViewById(R.id.ac_iv_press_back);

        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mSearchEditText.getRight() - 2 * mSearchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        mSearchEditText.setText("");
                        mSearchEditText.clearFocus();
                        return true;
                    }
                }
                return false;
            }
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterData(s.toString());
            }
        });

        mPressBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputKeyboard();
                finish();
            }
        });
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr 过滤值
     */
    private void filterData(String filterStr) {
        ArrayList<CountryZipModel> filterCountryList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterCountryList = countryZipModels;
        } else {
            filterCountryList.clear();
            for (CountryZipModel model : countryZipModels) {
                String name = model.getCountryName();
                if (name.indexOf(filterStr) != -1 || characterParser.getSelling(name).startsWith(filterStr)) {
                    filterCountryList.add(model);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterCountryList, this);
        adapter.updateList(filterCountryList);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
       if(requestCode == REQUEST_REGION_LIST){
           return action.getRegionListResponse();
       }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if(result == null) return;

        if(requestCode == REQUEST_REGION_LIST) {
            GetRegionListResponse response = (GetRegionListResponse) result;
            if(response.getCode() == 200){
                List<GetRegionListResponse.Region> regionList = response.getResult();
                if(regionList != null){
                    initRegionList(regionList);
                }
            }
        }
    }

    private void initRegionList(List<GetRegionListResponse.Region> regionList){
        countryZipModels.clear();
        for (GetRegionListResponse.Region region : regionList) {
            CountryZipModel countryZipModel = new CountryZipModel();
            if(mLocale == LangUtils.RCLocale.LOCALE_CHINA){
                countryZipModel.setCountryName(region.locale.zh);
            }else if(mLocale == LangUtils.RCLocale.LOCALE_US){
                countryZipModel.setCountryName(region.locale.en);
            }else {
                countryZipModel.setCountryName(region.locale.en);
            }
            countryZipModel.setCountryNameCN(region.locale.zh);
            countryZipModel.setCountryNameEN(region.locale.en);
            countryZipModel.setZipCode("+" + region.region);
            //汉字转换成拼音
            String namePinyin = characterParser.getSelling(countryZipModel.getCountryName());
            String firstChar = namePinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if (firstChar.matches("[A-Z]")) {
                countryZipModel.setFirstChar(firstChar.toUpperCase());
            } else {
                countryZipModel.setFirstChar("#");
            }
            countryZipModels.add(countryZipModel);

            Collections.sort(countryZipModels, this);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if(requestCode == REQUEST_REGION_LIST){
            RLog.e(TAG, "Get region list failed, state:" + state);
        }
    }

    @Override
    public int compare(CountryZipModel lhs, CountryZipModel rhs) {
        return lhs.getFirstChar().compareTo(rhs.getFirstChar());
    }
}
