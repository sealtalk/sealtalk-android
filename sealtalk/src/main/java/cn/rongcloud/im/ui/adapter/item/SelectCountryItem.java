package cn.rongcloud.im.ui.adapter.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.CountryInfo;

public class SelectCountryItem extends RelativeLayout {
    private TextView tvChar;
    private TextView tvCountry;
    private LinearLayout llFirstChar;
    private RelativeLayout rlCountryBg;
    private TextView tvCode;

    public SelectCountryItem(Context context) {
        super(context);
        initView();
    }

    public SelectCountryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SelectCountryItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.login_item_country, this);
        tvChar = (TextView)findViewById(R.id.tv_char);
        tvCountry = (TextView)findViewById(R.id.tv_country);
        llFirstChar = (LinearLayout)findViewById(R.id.ll_firstchar);
        rlCountryBg = (RelativeLayout) findViewById(R.id.ll_country_bg);
        tvCode = (TextView) findViewById(R.id.tv_code);
    }


    public void setData(CountryInfo data, boolean isFirst) {
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (isFirst) {
            llFirstChar.setVisibility(View.VISIBLE);
            tvChar.setText(data.getFirstChar());
        } else {
            llFirstChar.setVisibility(View.GONE);
        }
        tvCountry.setText(data.getCountryName());
        tvCode.setText(data.getZipCode());
    }
}
