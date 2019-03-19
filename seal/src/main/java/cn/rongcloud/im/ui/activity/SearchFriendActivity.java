package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.GetUserInfoByPhoneResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imlib.model.UserInfo;

public class SearchFriendActivity extends BaseActivity implements View.OnClickListener  {
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1000;
    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final int SEARCH_PHONE = 10;
    private static final int ADD_FRIEND = 11;
    private EditText mEtSearch;
    private EditText mSearchEt;
    private LinearLayout searchItem;
    private TextView searchName;
    private TextView mCountryNameTv, mCountryCodeTv;
    private LinearLayout mSearchContainerLl;
    private SelectableRoundedImageView searchImage;
    private String mPhone;
    private String mRegion;
    private String addFriendMessage;
    private String mFriendId;
    private Friend mFriend;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle((R.string.search_friend));
        sp = getSharedPreferences("config", MODE_PRIVATE);

        mHeadRightText.setVisibility(View.GONE);
        mHeadRightText.setText(getString(R.string.cancel));
        mHeadRightText.setOnClickListener(this);

        mEtSearch = (EditText) findViewById(R.id.search_edit);
        searchItem = (LinearLayout) findViewById(R.id.search_result);
        searchName = (TextView) findViewById(R.id.search_name);
        searchImage = (SelectableRoundedImageView) findViewById(R.id.search_header);
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchItem.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mHeadRightText.performClick();
                }
                return false;
            }
        });

        mCountryNameTv = (TextView)findViewById(R.id.search_country_name);
        mCountryCodeTv = (TextView)findViewById(R.id.search_country_code);
        mSearchEt = (EditText)findViewById(R.id.search_phone);
        TextView searchTv = (TextView)findViewById(R.id.search_search);

        mSearchContainerLl = (LinearLayout)findViewById(R.id.search_search_container);
        View selectCountryView = findViewById(R.id.search_country_select);
        selectCountryView.setOnClickListener(this);
        searchTv.setOnClickListener(this);
        initRegion();
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case SEARCH_PHONE:
                return action.getUserInfoFromPhone(mRegion, mPhone);
            case ADD_FRIEND:
                return action.sendFriendInvitation(mFriendId, addFriendMessage);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case SEARCH_PHONE:
                    final GetUserInfoByPhoneResponse userInfoByPhoneResponse = (GetUserInfoByPhoneResponse) result;
                    if (userInfoByPhoneResponse.getCode() == 200) {
                        mSearchContainerLl.setVisibility(View.GONE);
                        mHeadRightText.setVisibility(View.VISIBLE);

                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "success");
                        mFriendId = userInfoByPhoneResponse.getResult().getId();
                        searchItem.setVisibility(View.VISIBLE);
                        String portraitUri = null;
                        if (userInfoByPhoneResponse.getResult() != null) {
                            GetUserInfoByPhoneResponse.ResultEntity userInfoByPhoneResponseResult = userInfoByPhoneResponse.getResult();
                            UserInfo userInfo = new UserInfo(userInfoByPhoneResponseResult.getId(),
                                    userInfoByPhoneResponseResult.getNickname(),
                                    Uri.parse(userInfoByPhoneResponseResult.getPortraitUri()));
                            portraitUri = SealUserInfoManager.getInstance().getPortraitUri(userInfo);
                        }
                        ImageLoader.getInstance().displayImage(portraitUri, searchImage, App.getOptions());
                        searchName.setText(userInfoByPhoneResponse.getResult().getNickname());
                        searchItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isFriendOrSelf(mFriendId)) {
                                    Intent intent = new Intent(SearchFriendActivity.this, UserDetailActivity.class);
                                    intent.putExtra("friend", mFriend);
                                    intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                                    startActivity(intent);
                                    SealAppContext.getInstance().pushActivity(SearchFriendActivity.this);
                                    return;
                                }
                                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.add_text), getString(R.string.add_friend), new DialogWithYesOrNoUtils.DialogCallBack() {
                                    @Override
                                    public void executeEvent() {

                                    }

                                    @Override
                                    public void updatePassword(String oldPassword, String newPassword) {

                                    }

                                    @Override
                                    public void executeEditEvent(String editText) {
                                        if (!CommonUtils.isNetworkConnected(mContext)) {
                                            NToast.shortToast(mContext, R.string.network_not_available);
                                            return;
                                        }
                                        addFriendMessage = editText;
                                        if (TextUtils.isEmpty(editText)) {
                                            addFriendMessage = getString(R.string.inivte_firend_descprtion_format, getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_NAME, ""));
                                        }
                                        if (!TextUtils.isEmpty(mFriendId)) {
                                            LoadDialog.show(mContext);
                                            request(ADD_FRIEND);
                                        } else {
                                            NToast.shortToast(mContext, "id is null");
                                        }
                                    }
                                });
                            }
                        });

                    }
                    break;
                case ADD_FRIEND:
                    FriendInvitationResponse fres = (FriendInvitationResponse) result;
                    if (fres.getCode() == 200) {
                        NToast.shortToast(mContext, getString(R.string.request_success));
                        LoadDialog.dismiss(mContext);
                    } else {
                        NToast.shortToast(mContext, mContext.getString(R.string.quest_failed_error_code) + fres.getCode());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADD_FRIEND:
                NToast.shortToast(mContext, mContext.getString(R.string.you_are_already_friends));
                LoadDialog.dismiss(mContext);
                break;
            case SEARCH_PHONE:
                if (state == AsyncTaskManager.HTTP_ERROR_CODE || state == AsyncTaskManager.HTTP_NULL_CODE) {
                    super.onFailure(requestCode, state, result);
                } else {
                    NToast.shortToast(mContext, mContext.getString(R.string.account_not_exist));
                }
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hintKbTwo();
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private boolean isFriendOrSelf(String id) {
        String inputPhoneNumber = mPhone;
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        String selfPhoneNumber = sp.getString(SealConst.SEALTALK_LOGING_PHONE, "");
        if (inputPhoneNumber != null) {
            if (inputPhoneNumber.equals(selfPhoneNumber)) {
                mFriend = new Friend(sp.getString(SealConst.SEALTALK_LOGIN_ID, ""),
                        sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""),
                        Uri.parse(sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "")));
                return true;
            } else {
                mFriend = SealUserInfoManager.getInstance().getFriendByID(id);
                if (mFriend != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_right:
                mSearchContainerLl.setVisibility(View.VISIBLE);
                mHeadRightText.setVisibility(View.GONE);
                searchItem.setVisibility(View.GONE);
                break;
            case R.id.search_search:
                String phone = mSearchEt.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    NToast.shortToast(mContext, getString(R.string.phone_number_is_null));
                    return;
                }
                mPhone = phone;
                String region = mCountryCodeTv.getText().toString();
                if(region.startsWith("+")){
                    region = region.substring(1);
                }
                mRegion = region;
                hintKbTwo();
                LoadDialog.show(mContext);
                request(SEARCH_PHONE, true);
                break;
            case R.id.search_country_select:
                startActivityForResult(new Intent(this, SelectCountryActivity.class), REQUEST_CODE_SELECT_COUNTRY);
                break;
        }
    }

    private void initRegion(){
        String oldRegion = sp.getString(SealConst.SEALTALK_LOGIN_REGION,"");
        LangUtils.RCLocale appLocale = LangUtils.getAppLocale(this);
        if (LangUtils.RCLocale.LOCALE_CHINA == appLocale) {
            String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_CN, "");
            if(!TextUtils.isEmpty(countryName)){
                mCountryNameTv.setText(countryName);
                mCountryCodeTv.setText("+" + oldRegion);
            }
        } else if (LangUtils.RCLocale.LOCALE_US == appLocale) {
            String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_EN, "");
            if(!TextUtils.isEmpty(countryName)){
                mCountryNameTv.setText(countryName);
                mCountryCodeTv.setText("+" + oldRegion);
            }
        } else {
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_CN, "");
                if(!TextUtils.isEmpty(countryName)){
                    mCountryNameTv.setText(countryName);
                    mCountryCodeTv.setText("+" + oldRegion);
                }
            } else {
                String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_EN, "");
                if(!TextUtils.isEmpty(countryName)){
                    mCountryNameTv.setText(countryName);
                    mCountryCodeTv.setText("+" + oldRegion);
                }
            }
        }
    }

    /**
     * 验证手机号输入格式，并获取手机号和区号
     * @return
     */
    private boolean checkPhoneFormatIsValid(){
        boolean isValid = false;
        String inputText = mEtSearch.getText().toString().trim();
        if(TextUtils.isEmpty(inputText)) return false;

        int indexRegionStart = inputText.indexOf("(");
        if(indexRegionStart == -1){
            //如果没有区号则用该帐号所在的区号
            boolean matchesNumber = inputText.matches("\\d+");
            if(matchesNumber){
                isValid = true;
                mPhone = inputText;
                mRegion = sp.getString(SealConst.SEALTALK_LOGIN_REGION, "86");
            }
        } else {
            int indexRegionEnd = inputText.indexOf(")", indexRegionStart);
            if(indexRegionEnd != -1){
                //获取区号
                String region = inputText.substring(indexRegionStart + 1, indexRegionEnd);
                if(!TextUtils.isEmpty(region) && region.matches("\\d+")){
                    //获取电话号
                    String phone = inputText.substring(indexRegionEnd + 1);
                    if(!TextUtils.isEmpty(phone) && phone.matches("\\d+")){
                        isValid = true;
                        mPhone = phone;
                        mRegion = region;
                    }
                }
            }
        }
        return isValid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_SELECT_COUNTRY && resultCode == RESULT_OK){
            String zipCode = data.getStringExtra("zipCode");
            String countryName = data.getStringExtra("countryName");
            mCountryCodeTv.setText(zipCode);
            mCountryNameTv.setText(countryName);
        }
    }
}
