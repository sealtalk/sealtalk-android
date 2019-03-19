package cn.rongcloud.im.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.CountryZipModel;
import cn.rongcloud.im.ui.activity.BaseActivity;

/**
 * Created by duanliuyi on 2017/5/15.
 */

public class CountryAdapter extends BaseAdapter implements SectionIndexer {

    private Context context;
    private ArrayList<CountryZipModel> countryList;

    public CountryAdapter(Context context, ArrayList<CountryZipModel> countryZipModels) {
        this.context = context;
        countryList = countryZipModels;
    }

    public void updateList(ArrayList<CountryZipModel> list) {
        countryList = list;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return countryList.size();
    }

    @Override
    public Object getItem(int position) {
        return countryList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        final CountryZipModel countryZipModel = countryList.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_register_country, null);
            viewHolder.tvChar = (TextView) convertView.findViewById(R.id.tv_register_char);
            viewHolder.tvCountry = (TextView) convertView.findViewById(R.id.tv_register_country);
            viewHolder.llFirstChar = (LinearLayout) convertView.findViewById(R.id.ll_firstchar);
            viewHolder.rlCountryBg = (RelativeLayout) convertView.findViewById(R.id.ll_country_bg);
            viewHolder.tvCode = (TextView) convertView.findViewById(R.id.tv_register_code);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.llFirstChar.setVisibility(View.VISIBLE);
            viewHolder.tvChar.setText(countryZipModel.getFirstChar());
        } else {
            //viewHolder.tvChar.setVisibility(View.GONE);
            viewHolder.llFirstChar.setVisibility(View.GONE);
        }
        viewHolder.tvCountry.setText(countryZipModel.getCountryName());
        viewHolder.tvCode.setText(countryZipModel.getZipCode());
        viewHolder.rlCountryBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity activityContext = (BaseActivity) context;
                Intent intent = new Intent();
                intent.putExtra("zipCode", countryZipModel.getZipCode());
                intent.putExtra("countryName", countryZipModel.getCountryName());
                intent.putExtra("countryNameCN", countryZipModel.getCountryNameCN());
                intent.putExtra("countryNameEN", countryZipModel.getCountryNameEN());
                activityContext.setResult(Activity.RESULT_OK, intent);
                activityContext.hideInputKeyboard();
                activityContext.finish();
            }
        });
        return convertView;
    }


    @Override
    public int getSectionForPosition(int position) {
        return countryList.get(position).getFirstChar().charAt(0);
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = countryList.get(i).getFirstChar();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }

        return -1;
    }


    final static class ViewHolder {
        /**
         * 首字母
         */
        TextView tvChar;
        /**
         * 昵称
         */
        TextView tvCountry;
        /**
         * 区号
         */
        TextView tvCode;

        LinearLayout llFirstChar;
        RelativeLayout rlCountryBg;
    }

}
