package cn.rongcloud.im.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import java.util.List;

import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.ui.adapter.item.SelectCountryItem;


/**
 * 国家地区列表的 Adapter
 */
public class CountryAdapter extends BaseAdapter implements SectionIndexer {

    private List<CountryInfo> countryList;

    public void updateList(List<CountryInfo> list) {
        countryList = list;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return countryList == null? 0 : countryList.size();
    }

    @Override
    public Object getItem(int position) {
        return countryList == null? null : countryList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new SelectCountryItem(parent.getContext());
        }

        final CountryInfo data = countryList.get(position);
        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        ((SelectCountryItem)convertView).setData(data, position == getPositionForSection(section));
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

}
