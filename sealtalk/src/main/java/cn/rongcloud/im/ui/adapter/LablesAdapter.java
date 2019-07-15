package cn.rongcloud.im.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;

public class LablesAdapter extends BaseAdapter {

    List<String> datas = new ArrayList<>();
    List<String> selecteds = new ArrayList<>();

    public void updateList(List<String> datas) {
        if (datas == null) {
            return;
        }
        this.selecteds.clear();
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas == null? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas == null? 0 : datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_lable, null);
        }

        String lable = datas.get(position);
        CheckBox checkBox = ((CheckBox)convertView);
        checkBox.setText(lable);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!selecteds.contains(lable)) {
                        selecteds.add(lable);
                    }
                } else {
                    if (selecteds.contains(lable)) {
                        selecteds.remove(lable);
                    }
                }
            }
        });
        return convertView;
    }

    public List<String> getSeletedLables() {
        return selecteds;
    }

    public String getSeletedLablesString() {
        if (selecteds == null || selecteds.size() <= 0) {
            return "";
        }
        StringBuffer lableStr = new StringBuffer();
        for (int i=0; i<selecteds.size(); i++) {
            lableStr.append(selecteds.get(i));
            if (i != selecteds.size() -1) {
                lableStr.append(",");
            }
        }
        return lableStr.toString();
    }
}
