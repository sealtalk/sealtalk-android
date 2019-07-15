package cn.rongcloud.im.ui.adapter;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.GroupMember;
import io.rong.imkit.widget.AsyncImageView;

public class MemberMentionedAdapter extends BaseAdapter  implements SectionIndexer {
    private List<GroupMember> memberList = new ArrayList<>();

    /**
     * 更新
     * @param data
     */
    public void updateList(List<GroupMember> data) {

        this.memberList.clear();
        if (data != null && data.size() > 0) {
            this.memberList.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return memberList == null? 0 : memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return  memberList == null? null : memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(io.rong.imkit.R.layout.rc_mention_list_item, null);
            viewHolder.name = (TextView) convertView.findViewById(io.rong.imkit.R.id.rc_user_name);
            viewHolder.portrait = (AsyncImageView) convertView.findViewById(io.rong.imkit.R.id.rc_user_portrait);
            viewHolder.letter = (TextView) convertView.findViewById(io.rong.imkit.R.id.letter);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        GroupMember member = memberList.get(position);
        if (member != null) {
            String name = member.getGroupNickName();
            if (TextUtils.isEmpty(name)) {
                name = member.getAlias();
                if (TextUtils.isEmpty(name)) {
                    name = member.getName();
                }
            }
            viewHolder.name.setText(name);
            if (!TextUtils.isEmpty(member.getPortraitUri())) {
                viewHolder.portrait.setAvatar(Uri.parse(member.getPortraitUri()));
            } else {
                if (member.getUserId().equals("-1")) {
                    viewHolder.portrait.setImageResource(R.drawable.seal_ic_mention_at);
                } else {
                    viewHolder.portrait.setImageResource(R.drawable.rc_default_portrait);
                }
            }
        }

        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.letter.setVisibility(View.VISIBLE);
            viewHolder.letter.setText(memberList.get(position).getNameSpelling());
        } else {
            viewHolder.letter.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = memberList.get(i).getNameSpelling();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return memberList.get(position).getNameSpelling().charAt(0);
    }


    class ViewHolder {
        AsyncImageView portrait;
        TextView name;
        TextView letter;
    }
}
