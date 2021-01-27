package cn.rongcloud.im.model.qrcode;

/**
 * 二维码扫描结果
 */
public class QRCodeResult {
    private QRCodeType type;
    private Object result;

    public QRCodeResult(QRCodeType type, Object result) {
        this.type = type;
        this.result = result;
    }

    /**
     * 获取二位码扫描结果类型
     * 根据结果类型获取相应的结果信息
     * @return
     */
    public QRCodeType getType() {
        return type;
    }

    /**
     * 群组信息结果
     *
     * @return
     */
    public QRGroupInfo getGroupInfoResult() {
        if (result instanceof QRGroupInfo) {
            return (QRGroupInfo) result;
        } else {
            return null;
        }
    }

    /**
     * 用户信息结果
     *
     * @return
     */
    public QRUserInfo getUserInfoResult() {
        if (result instanceof QRUserInfo) {
            return (QRUserInfo) result;
        } else {
            return null;
        }
    }

    public Object getResult() {
        return result;
    }

}
