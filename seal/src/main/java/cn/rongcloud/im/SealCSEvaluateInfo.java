package cn.rongcloud.im;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.rongcloud.im.model.SealCSEvaluateItem;


/**
 * Created by yuejunhong on 17/9/20.
 */

public class SealCSEvaluateInfo {
    List<SealCSEvaluateItem> sealCSEvaluateInfoList = new ArrayList<>();

    public SealCSEvaluateInfo(JSONObject jsonObj) {
        try {
            JSONObject evaluateJsonObj = jsonObj.optJSONObject("evaluation");
            JSONArray jsonArray = evaluateJsonObj.getJSONArray("satisfaction");
            sealCSEvaluateInfoList.clear();
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    SealCSEvaluateItem sealCSEvaluateItem = new SealCSEvaluateItem();
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    sealCSEvaluateItem.setConfigId(jsonObject.optString("configId"));
                    sealCSEvaluateItem.setCompanyId(jsonObject.optString("companyId"));
                    sealCSEvaluateItem.setGroupId(jsonObject.optString("groupId"));
                    sealCSEvaluateItem.setGroupName(jsonObject.optString("groupName"));
                    sealCSEvaluateItem.setLabelId(jsonObject.optString("labelId"));
                    String labelNames = jsonObject.optString("labelName");
                    sealCSEvaluateItem.setLabelNameList(Arrays.asList(labelNames.split(",")));
                    sealCSEvaluateItem.setQuestionFlag(jsonObject.optInt("isQuestionFlag", 0) == 1 ? true : false);
                    sealCSEvaluateItem.setScore(jsonObject.optInt("score"));
                    sealCSEvaluateItem.setScoreExplain(jsonObject.optString("scoreExplain"));
                    sealCSEvaluateItem.setTagMust(jsonObject.optInt("isTagMust", 0) == 1 ? true : false);
                    sealCSEvaluateItem.setInputMust(jsonObject.optInt("isInputMust", 0) == 1 ? true : false);
                    sealCSEvaluateItem.setInputLanguage(jsonObject.optString("inputLanguage"));
                    sealCSEvaluateItem.setCreateTime(jsonObject.optLong("createTime", 0));
                    sealCSEvaluateItem.setSettingMode(jsonObject.optInt("settingMode"));
                    sealCSEvaluateItem.setUpdateTime(jsonObject.optLong("updateTime", 0));
                    sealCSEvaluateItem.setOperateType(jsonObject.optInt("operateType"));
                    sealCSEvaluateInfoList.add(sealCSEvaluateItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<SealCSEvaluateItem> getSealCSEvaluateInfoList() {
        return sealCSEvaluateInfoList;
    }
}
