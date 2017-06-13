/*
 * AppConfog.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.log.Log;
import android.math.Maths;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-8-30
 */
public final class RuntimeConfigure extends Configure {
    private static String TAG = "RuntimeConfigure";

    private static RuntimeConfigure sRuntimeConfigure;

    private boolean mIsShowToConsole;

    private String mChannelId;

    private String mAppId;
    private String mAppCode;
    private String mAppPath;
    private String mAppConfig;

    private String mImplSign;

    private String mPullFilter;
    private String[] mEventFilter;

    private String mB, mC;

    private int mAPI;

    private String mOS;

    /**
     * Process工具版本号
     */
    private int mProcessVersion;

    private String mAppSecret;

    private String mAlibabaAppkey;
    private String mAlibabaAppSecret;
    private String mAlibabaAccountId;
    private String mAlibabaChannel;

    public static RuntimeConfigure obtain() {
        if (sRuntimeConfigure == null) {
            synchronized (RuntimeConfigure.class) {
                if (sRuntimeConfigure == null) {
                    try {
                        sRuntimeConfigure = new RuntimeConfigure();
                        sRuntimeConfigure.load(AppResource.getInputStreamFromAssets(C.file.runtime_config), true);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                }
            }
        }

        return sRuntimeConfigure;
    }

    public void onDestroy() {
        sRuntimeConfigure = null;
    }

    @Override
    protected void onLoadProperties() throws Exception {
        mIsShowToConsole = Maths.valueOf(get(C.properties.is_show_to_console));

        mChannelId = get(C.properties.channel_id, C.value.default_channel_id);

        mAppId = get(C.properties.app_id, C.value.default_app_id);
        mAppSecret = TextUtilz.fromFake(get(C.properties.app_secret));

        mAppPath = get(C.properties.app_path, C.value.default_path_app);
        mAppCode = get(C.properties.app_code, C.value.default_app_code);
        mAppConfig = get(C.properties.app_config);

        mImplSign = get(C.properties.impl_sign);

        mPullFilter = get(C.properties.pull_filter);
        mEventFilter = TextUtilz.blockSort(get(C.properties.event_filter));
        mB = get(C.properties.b);
        mC = get(C.properties.c);

        mAPI = Maths.valueOf(get(C.properties.api), 0);
        mOS = get(C.properties.os);
        mProcessVersion = Maths.valueOf(get(C.properties.process_version), 0);

        // Alibaba
        mAlibabaAppkey = get("alibaba_appkey");
        mAlibabaAppSecret = get("alibaba_appsecret");
        mAlibabaAccountId = get("alibaba_accountid");
        mAlibabaChannel = get("alibaba_channel");
    }

    public boolean isShowToConsole() {

        return mIsShowToConsole;
    }

    public String channelId() {

        return mChannelId;
    }

    public String appId() {

        return mAppId;
    }

    public String appCode() {

        return mAppCode;
    }

    public String appPath() {

        return mAppPath;
    }

    public String appConfig() {
        return mAppConfig;
    }

    public String signImpl() {

        return mImplSign;
    }

    public String pullFilter() {

        return mPullFilter;
    }

    public String[] eventFilter() {

        return mEventFilter;
    }

    public String b() {

        return mB;
    }

    public String c() {

        return mC;
    }

    public int api() {

        return mAPI;
    }

    public String os() {

        return Assert.notEmpty(mOS) ? mOS : C.value.os;
    }

    public int processVersion() {

        return mProcessVersion;
    }

    public String appSecret() {

        return mAppSecret;
    }

    public String alibabaAppkey() {

        return mAlibabaAppkey;
    }

    public String alibabaAppSecret() {

        return mAlibabaAppSecret;
    }

    public String alibabaAccountId() {

        return mAlibabaAccountId;
    }

    public String alibabaChannel() {

        return mAlibabaChannel;
    }
}
