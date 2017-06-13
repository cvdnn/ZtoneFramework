package android.framework;

import android.assist.Assert;

/**
 * Created by handy on 17-2-9.
 */

public final class Vriables {
    private String mAppName;

    /**
     * 渠道ID
     */
    private String mChannelId;

    /**
     * 应用标识
     */
    private String mAppId;

    /**
     * 应用在sdcard上创建的根路径
     */
    private String mAppPath;

    private String mUUID;

    /**
     * app签名
     */
    private String mCertificate;

    /**
     * 是否加载了本地配置文件, 即调试模式
     */
    private boolean mIsLoadLocalConfigFile;

    private String mCASSecret;

    /**
     * 资源所在的插件包名
     */
    private String mPackageName;

    private String mAppCode;

    protected Vriables() {
        mAppId = "";
        mAppName = "";
        mChannelId = "";
        mAppPath = "";
        mUUID = "";
        mCertificate = "";
        mCASSecret = "";
        mPackageName = "";
        mAppCode = "";
    }

    public String getAppId() {
        return mAppId;
    }

    public Vriables setAppId(String appId) {
        this.mAppId = appId;

        return this;
    }

    public String getAppName() {
        return mAppName;
    }

    public Vriables setAppName(String appName) {
        this.mAppName = appName;

        return this;
    }

    public String getAppPath() {
        return mAppPath;
    }

    public Vriables setAppPath(String appPath) {
        this.mAppPath = appPath;

        return this;
    }

    public String getCASSecret() {
        return mCASSecret;
    }

    public Vriables setCASSecret(String casSecret) {
        this.mCASSecret = casSecret;

        return this;
    }

    public String getCertificate() {
        return mCertificate;
    }

    public Vriables setCertificate(String certificate) {
        this.mCertificate = certificate;

        return this;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public Vriables setChannelId(String channelId) {
        this.mChannelId = channelId;

        return this;
    }

    public boolean isLoadLocalConfiguration() {
        return mIsLoadLocalConfigFile;
    }

    public Vriables setIsLoadLocalConfigFile(boolean isLoadLocalFile) {
        this.mIsLoadLocalConfigFile = isLoadLocalFile;

        return this;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public Vriables setPackageName(String packageName) {
        this.mPackageName = packageName;

        return this;
    }

    public String getUUID() {
        return mUUID;
    }

    public Vriables setUUID(String uuid) {
        this.mUUID = uuid;

        return this;
    }

    public synchronized String getClientId() {

        return Assert.notEmpty(mUUID) ? mUUID.replace("-", "") : "00000000000000";
    }

    public String getAppCode() {

        return mAppCode;
    }

    public Vriables setAppCode(String appCode) {
        this.mAppCode = appCode;

        return this;
    }
}
