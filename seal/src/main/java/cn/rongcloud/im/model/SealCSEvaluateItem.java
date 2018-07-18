package cn.rongcloud.im.model;

import java.util.ArrayList;
import java.util.List;

public class SealCSEvaluateItem {

    private String configId;
    private String companyId;
    private String groupId;
    private String groupName;
    private String labelId;
    private List<String> labelNameList = new ArrayList<>();
    private boolean isQuestionFlag;
    private int score;
    private String scoreExplain;
    private boolean isTagMust;
    private boolean isInputMust;
    private String inputLanguage;
    private long createTime;
    private int settingMode;
    private long updateTime;
    private int operateType;


    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelNameList(List<String> labelNameList) {
        this.labelNameList = labelNameList;
    }

    public List<String> getLabelNameList() {
        return labelNameList;
    }

    public void setQuestionFlag(boolean questionFlag) {
        this.isQuestionFlag = questionFlag;
    }

    public boolean getQuestionFlag() {
        return isQuestionFlag;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScoreExplain(String scoreExplain) {
        this.scoreExplain = scoreExplain;
    }

    public String getScoreExplain() {
        return scoreExplain;
    }

    public void setTagMust(boolean tagMust) {
        this.isTagMust = tagMust;
    }

    public boolean getTagMust() {
        return isTagMust;
    }

    public void setInputMust(boolean inputMust) {
        this.isInputMust = inputMust;
    }

    public boolean getInputMust() {
        return isInputMust;
    }

    public void setInputLanguage(String inputLanguage) {
        this.inputLanguage = inputLanguage;
    }

    public String getInputLanguage() {
        return inputLanguage;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setSettingMode(int settingMode) {
        this.settingMode = settingMode;
    }

    public int getSettingMode() {
        return settingMode;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }

    public int getOperateType() {
        return operateType;
    }

    public SealCSEvaluateItem() {
    }
}
