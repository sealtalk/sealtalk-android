package cn.rongcloud.im.model;

import android.content.res.Resources;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.utils.DataCenter;
import com.google.gson.annotations.SerializedName;
import io.rong.imlib.model.InitOption;
import java.util.ArrayList;
import java.util.List;

/** @author gusd @Date 2023/03/07 */
public class DataCenterJsonModel {

    @SerializedName("dataCenterList")
    private List<DataCenterListDTO> dataCenterList;

    public List<DataCenterListDTO> getDataCenterList() {
        return dataCenterList == null ? new ArrayList<>() : dataCenterList;
    }

    public void setDataCenterList(List<DataCenterListDTO> dataCenterList) {
        this.dataCenterList = dataCenterList;
    }

    public static class DataCenterListDTO implements DataCenter {
        @SerializedName("resourceName")
        private String resourceName;

        @SerializedName("code")
        private String code;

        @SerializedName("naviUrl")
        private String naviUrl;

        @SerializedName("appKey")
        private String appKey;

        @SerializedName("appServer")
        private String appServer;

        @Override
        public int getNameId() {
            try {
                Resources resources = SealApp.getApplication().getResources();
                return resources.getIdentifier(
                        resourceName, "string", SealApp.getApplication().getPackageName());
            } catch (Exception e) {
                return 0;
            }
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public InitOption.AreaCode getAreaCode() {
            if ("north_america".equals(code)) {
                return InitOption.AreaCode.NA;
            }
            if ("singapore".equals(code)) {
                return InitOption.AreaCode.SG;
            }
            // beijing
            return InitOption.AreaCode.BJ;
        }

        @Override
        public String getNaviUrl() {
            return naviUrl;
        }

        public void setNaviUrl(String naviUrl) {
            this.naviUrl = naviUrl;
        }

        @Override
        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        @Override
        public String getAppServer() {
            return appServer;
        }

        public void setAppServer(String appServer) {
            this.appServer = appServer;
        }
    }
}
