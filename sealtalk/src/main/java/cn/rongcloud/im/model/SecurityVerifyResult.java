package cn.rongcloud.im.model;

import android.text.TextUtils;

public class SecurityVerifyResult {
    /** 风险级别:PASS：通过 REVIEW：审核 REJECT：拒绝 VERIFY:验证 */
    public String riskLevel;

    public boolean isKickOut() {
        return TextUtils.equals(riskLevel, "REJECT");
    }
}
