package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.ui.activity.SelectCountryActivity;
import cn.rongcloud.im.ui.interfaces.OnSearchFriendClickListener;

import static android.app.Activity.RESULT_OK;
import static cn.rongcloud.im.ui.activity.SelectCountryActivity.RESULT_PARAMS_COUNTRY_INFO;

public class SearchFriendNetFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SearchFriendNetFragment";
    private static final int REQUEST_COUNTRY_CODE = 0;
    private View selectCountry;
    private TextView tvCountryName;
    private TextView tvRegion;
    private TextView tvSearch;
    private TextView tvPhone;
    private OnSearchFriendClickListener onSearchFriendClick;

    public void setOnSearchFriendClickListener(OnSearchFriendClickListener onSearchFriendClick) {
        this.onSearchFriendClick = onSearchFriendClick;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search_friend_net, container, false);
        selectCountry = view.findViewById(R.id.search_country_select);
        tvCountryName = view.findViewById(R.id.search_country_name);
        tvRegion = view.findViewById(R.id.search_country_code);
        tvSearch = view.findViewById(R.id.search_search);
        tvPhone = view.findViewById(R.id.search_phone);
        tvSearch.setOnClickListener(this);
        selectCountry.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_country_select:
                Intent intent = new Intent(getContext(), SelectCountryActivity.class);
                startActivityForResult(intent, REQUEST_COUNTRY_CODE);
                break;
            case R.id.search_search:
                if (onSearchFriendClick != null) {
                    String region = tvRegion.getText().toString();
                    String phone = tvRegion.getText().toString();
                    if (!TextUtils.isEmpty(region) && !TextUtils.isEmpty(phone)) {
                        onSearchFriendClick.onSearchClick(region.substring(1), tvPhone.getText().toString());
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_COUNTRY_CODE:
                    CountryInfo info = data.getParcelableExtra(RESULT_PARAMS_COUNTRY_INFO);
                    tvCountryName.setText(info.getCountryName());
                    tvRegion.setText(info.getZipCode());
                    break;
                default:
                    break;
            }
        }
    }
}
