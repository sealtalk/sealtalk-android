package cn.rongcloud.im.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvaluateInfo {

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

    public EvaluateInfo() {
    }



    public static List<EvaluateInfo> getEvaluateInfoList(JSONObject jsonObj) {
        List<EvaluateInfo> evaluateInfoList = new ArrayList<>();

        try {
            JSONObject evaluateJsonObj = jsonObj.optJSONObject("evaluation");
            JSONArray jsonArray = evaluateJsonObj.getJSONArray("satisfaction");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    EvaluateInfo evaluateInfo = new EvaluateInfo();
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    evaluateInfo.setConfigId(jsonObject.optString("configId"));
                    evaluateInfo.setCompanyId(jsonObject.optString("companyId"));
                    evaluateInfo.setGroupId(jsonObject.optString("groupId"));
                    evaluateInfo.setGroupName(jsonObject.optString("groupName"));
                    evaluateInfo.setLabelId(jsonObject.optString("labelId"));
                    String labelNames = jsonObject.optString("labelName");
                    evaluateInfo.setLabelNameList(Arrays.asList(labelNames.split(",")));
                    evaluateInfo.setQuestionFlag(jsonObject.optInt("isQuestionFlag", 0) == 1 ? true : false);
                    evaluateInfo.setScore(jsonObject.optInt("score"));
                    evaluateInfo.setScoreExplain(jsonObject.optString("scoreExplain"));
                    evaluateInfo.setTagMust(jsonObject.optInt("isTagMust", 0) == 1 ? true : false);
                    evaluateInfo.setInputMust(jsonObject.optInt("isInputMust", 0) == 1 ? true : false);
                    evaluateInfo.setInputLanguage(jsonObject.optString("inputLanguage"));
                    evaluateInfo.setCreateTime(jsonObject.optLong("createTime", 0));
                    evaluateInfo.setSettingMode(jsonObject.optInt("settingMode"));
                    evaluateInfo.setUpdateTime(jsonObject.optLong("updateTime", 0));
                    evaluateInfo.setOperateType(jsonObject.optInt("operateType"));
                    evaluateInfoList.add(evaluateInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return evaluateInfoList;
    }
}
