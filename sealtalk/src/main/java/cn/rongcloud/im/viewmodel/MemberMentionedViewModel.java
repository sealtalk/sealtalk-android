package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import io.rong.imkit.feature.mention.RongMentionManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class MemberMentionedViewModel extends AndroidViewModel {

    private IMManager imManager;
    private GroupTask groupTask;
    private SingleSourceMapLiveData<Resource<List<GroupMember>>, List<GroupMember>> memberListResult;
    private SingleSourceLiveData<Resource<List<GroupMember>>> memberList = new SingleSourceLiveData<>();
    private MutableLiveData<List<GroupMember>> filterMemberResult = new MutableLiveData<>();

    public MemberMentionedViewModel(@NonNull Application application) {
        super(application);
    }

    public MemberMentionedViewModel(String targerId, Conversation.ConversationType conversationType, @NonNull Application application) {
        super(application);

        imManager = IMManager.getInstance();
        groupTask = new GroupTask(application.getApplicationContext());

        memberListResult = new SingleSourceMapLiveData<>(new Function<Resource<List<GroupMember>>, List<GroupMember>>() {
            @Override
            public List<GroupMember> apply(Resource<List<GroupMember>> input) {
                if (input.status == Status.LOADING || input.data == null || input.data.size() <= 0) {
                    return null;
                }

                List<GroupMember> groupMembers = new ArrayList<>();
                List<GroupMember> originGroupMember = input.data;
                for (GroupMember info : originGroupMember) {
                    if (info != null && !info.getUserId().equals(imManager.getCurrentId())) {
                        GroupMember member = new GroupMember();
                        member.setUserId(info.getUserId());
                        member.setName(info.getName());
                        member.setGroupNickName(info.getGroupNickName());
                        member.setPortraitUri(info.getPortraitUri());
                        String sortString = "#";
                        //汉字转换成拼音
                        String displayName = TextUtils.isEmpty(info.getGroupNickName())?
                                info.getName() : info.getGroupNickName();
                        String pinyin = CharacterParser.getInstance().getSpelling(displayName);
                        if (pinyin != null) {
                            if (pinyin.length() > 0) {
                                sortString = pinyin.substring(0, 1).toUpperCase();
                            }
                        }
                        // 正则表达式，判断首字母是否是英文字母
                        if (sortString.matches("[A-Z]")) {
                            member.setNameSpelling(sortString.toUpperCase());
                        } else {
                            member.setNameSpelling("#");
                        }
                        groupMembers.add(member);
                    }
                }

                Collections.sort(groupMembers, new Comparator<GroupMember>() {
                    @Override
                    public int compare(GroupMember o1, GroupMember o2) {
                        if (o1.getNameSpelling().equals("@") || o2.getNameSpelling().equals("#")) {
                            return -1;
                        } else if (o1.getNameSpelling().equals("#") || o2.getNameSpelling().equals("@")) {
                            return 1;
                        } else {
                            return o1.getNameSpelling().compareTo(o2.getNameSpelling());
                        }
                    }
                });

                GroupMember groupMember = new GroupMember();
                groupMember.setName(application.getResources().getString(R.string.seal_member_mention_all_member));
                groupMember.setNameSpelling(" ");
                groupMember.setUserId("-1");
                groupMember.setPortraitUri("");
                groupMembers.add(0, groupMember);
                return groupMembers;
            }
        });

        memberListResult.setSource(memberList);
        getMemberListData(targerId, conversationType);
    }


    /**
     * 成员列表
     *
     * @return
     */
    public LiveData<List<GroupMember>> getMemberListResult() {
        return memberListResult;
    }

    /**
     * 过滤的成员列表
     *
     * @return
     */
    public LiveData<List<GroupMember>> getFilterMenberList() {
        return filterMemberResult;
    }

    /**
     * 获取群成员
     *
     * @param targerId
     * @param conversationType
     */
    private void getMemberListData(String targerId, Conversation.ConversationType conversationType) {
        if (conversationType.equals(Conversation.ConversationType.GROUP)) {
            getGroupMembers(targerId);
        }
    }

    /**
     * 获取群成员
     *
     * @param targetId
     */
    private void getGroupMembers(String targetId) {
        memberList.setSource(groupTask.getGroupMemberInfoList(targetId));
    }

    /**
     * 过滤的成员列表
     *
     * @param filterStr
     */
    public void filterMember(String filterStr) {
        List<GroupMember> value = memberListResult.getValue();
        if (value == null) {
            return;
        }

        if (TextUtils.isEmpty(filterStr)) {
            filterMemberResult.postValue(value);
            return;
        }

        List<GroupMember> filterMembers = new ArrayList<>();
        for (GroupMember member : value) {
            String name = member.getName();
            if (name != null) {
                if (name != null && (name.indexOf(filterStr) != -1 || CharacterParser.getInstance().getSpelling(name).startsWith(filterStr))) {
                    filterMembers.add(member);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterMembers, new Comparator<GroupMember>() {
            @Override
            public int compare(GroupMember o1, GroupMember o2) {
                if (o1.getNameSpelling().equals("@") || o2.getNameSpelling().equals("#")) {
                    return -1;
                } else if (o1.getNameSpelling().equals("#") || o2.getNameSpelling().equals("@")) {
                    return 1;
                } else {
                    return o1.getNameSpelling().compareTo(o2.getNameSpelling());
                }
            }
        });
        filterMemberResult.postValue(filterMembers);
    }

    /**
     * 设置 @ 成员
     *
     * @param member
     */
    public void setMentionMember(GroupMember member) {
        String groupMemberName = TextUtils.isEmpty(member.getGroupNickName()) ? member.getName():member.getGroupNickName();
        UserInfo userInfo = new UserInfo(member.getUserId(), groupMemberName, Uri.parse(member.getPortraitUri()));
        RongMentionManager.getInstance().mentionMember(userInfo);
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String targerId;
        private Application application;
        private Conversation.ConversationType conversationType;

        public Factory(String targerId, Conversation.ConversationType conversationType, Application application) {
            this.targerId = targerId;
            this.conversationType = conversationType;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(String.class, Conversation.ConversationType.class, Application.class).newInstance(targerId, conversationType, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
