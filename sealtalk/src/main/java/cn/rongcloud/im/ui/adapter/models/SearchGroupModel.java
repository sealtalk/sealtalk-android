package cn.rongcloud.im.ui.adapter.models;

import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;

public class SearchGroupModel extends SearchModel<GroupEntity> {
    private int groupNameStart;
    private int groupNameEnd;
    private List<GroupMemberMatch> matchedMemberlist;

    public SearchGroupModel(GroupEntity bean, int type, int groupNameStart, int groupNameEnd, List<GroupMemberMatch> memberMatches) {
        super(bean, type);
        priority = SHOW_PRIORITY_GROUP;
        matchedMemberlist = memberMatches;
        this.groupNameStart = groupNameStart;
        this.groupNameEnd = groupNameEnd;
    }

    public int getGroupNameStart() {
        return groupNameStart;
    }

    public int getGroupNameEnd() {
        return groupNameEnd;
    }

    public List<GroupMemberMatch> getMatchedMemberlist() {
        return matchedMemberlist;
    }

    public static class GroupMemberMatch {
        //start 如果为 -1 则没有找到
        private String name;
        private int nameStart;
        private int nameEnd;

        public GroupMemberMatch(String name, int nameStart, int nameEnd) {
            this.name = name;
            this.nameStart = nameStart;
            this.nameEnd = nameEnd;
        }

        public String getName() {
            return name;
        }

        public int getNameStart() {
            return nameStart;
        }

        public int getNameEnd() {
            return nameEnd;
        }
    }



    private CheckType checkType = CheckType.NONE;

    public CheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }
}
