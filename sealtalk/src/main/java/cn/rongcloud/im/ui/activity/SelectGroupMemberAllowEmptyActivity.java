package cn.rongcloud.im.ui.activity;


/**
 * 选择当前群组 groupId 内的人,允许选择空时点击确认，以用于清空选项
 */
public class SelectGroupMemberAllowEmptyActivity extends SelectGroupMemberActivity {
    @Override
    public boolean confirmEnabledWhenNoChecked() {
        return true;
    }
}
