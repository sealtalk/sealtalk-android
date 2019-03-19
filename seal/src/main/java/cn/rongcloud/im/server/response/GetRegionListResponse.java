package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * 获取所有区域信息。
 */
public class GetRegionListResponse {
    private int code;
    private List<Region> result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Region> getResult() {
        return result;
    }

    public void setResult(List<Region> result) {
        this.result = result;
    }

    public static class Region {
        public String region;
        public Locale locale;

        public static class Locale {
            public String en;
            public String zh;
        }
    }
}
