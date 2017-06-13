/*
 * URLBuilder.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.builder;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.sign.Sign;
import android.log.Log;
import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-10-29
 */
public final class URLBuilder {
    private static final String TAG = "URLBuilder";

    private static final LinkedHashMap<String, String> sGeneralMetas = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> sHeaderMetas = new LinkedHashMap<>();

    /**
     * 创建URLBuilder实例
     *
     * @return
     */
    public static URLBuilder create() {

        return new URLBuilder(IRuntime.appConfig().baseUrl());
    }

    /**
     * 创建URLBuilder实例
     *
     * @param baseURL
     * @return
     */
    public static URLBuilder create(final String baseURL) {
        return new URLBuilder(baseURL);
    }

    /**
     * 设置url共用参数
     *
     * @param key
     * @param value
     */
    public synchronized static <V> void setHeaderMeta(final String key, final V value) {
        sHeaderMetas.put(key, TextUtilz.toString(value));
    }

    /**
     * 删除url中共有参数
     *
     * @param key
     */
    public synchronized static void removeHeaderMeta(final String key) {
        if (Assert.containsKey(sHeaderMetas, key)) {
            sHeaderMetas.remove(key);
        }
    }

    /**
     * 获取共有参数
     *
     * @param key
     * @return
     */
    public synchronized static String getHeaderMeta(final String key) {

        return Assert.containsKey(sHeaderMetas, key) ? sHeaderMetas.get(key) : "";
    }

    /**
     * 设置url共用参数
     *
     * @param key
     * @param value
     */
    public synchronized static <V> void setGeneralMeta(final String key, final V value) {
        sGeneralMetas.put(key, TextUtilz.toString(value));
    }

    /**
     * 删除url中共有参数
     *
     * @param key
     */
    public synchronized static void removeGeneralMeta(final String key) {
        if (Assert.containsKey(sGeneralMetas, key)) {
            sGeneralMetas.remove(key);
        }
    }

    /**
     * 获取共有参数
     *
     * @param key
     * @return
     */
    public synchronized static String getGeneralMeta(final String key) {

        return Assert.containsKey(sGeneralMetas, key) ? sGeneralMetas.get(key) : "";
    }

    /**
     * 统一中文编码
     *
     * @param value
     * @return
     */
    public static String encode(String value) {
        String result = value;

        if (Assert.notEmpty(value)) {
            try {
                result = URLEncoder.encode(value, C.value.encoding);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        return result;
    }

	/* **********************************************
     *
	 * 
	 * *********************************************
	 */

    private final String mBaseURL;

    private final ArrayList<String> mPathList = new ArrayList<>();
    private final LinkedHashMap<String, String> mValueContent = new LinkedHashMap<>();

    private String mAction;

    private URLBuilder(String baseURL) {
        mBaseURL = baseURL;
    }

    /**
     * 设置服务路径
     *
     * @param paths
     * @return
     */
    public URLBuilder path(final String... paths) {
        mPathList.addAll(Arrays.asList(paths));

        return this;
    }

    /**
     * 设置服务路径
     *
     * @param path
     * @return
     */
    public URLBuilder appendPath(final String path) {
        mPathList.add(path);

        return this;
    }

    /**
     * 设置服务跳转action
     *
     * @param action
     * @return
     */
    public URLBuilder action(final String action) {
        mAction = action;

        return this;
    }

    public URLBuilder appendContentLength(final int contentLength) {
        mValueContent.put(C.tag.cl, TextUtilz.toString(contentLength));

        return this;
    }

    /**
     * 添加键值，如果键值重复，以后一个键值为主
     *
     * @param key
     * @param value
     * @return
     */
    public <V> URLBuilder append(final String key, final V value) {
        mValueContent.put(key, TextUtilz.toString(value));

        return this;
    }

    /**
     * 获取URLMeta
     *
     * @param key
     * @return
     */
    public String get(String key) {

        return Assert.notEmpty(key) && Assert.containsKey(mValueContent, key) ? mValueContent.get(key) : "";
    }

    /**
     * 移除URLMeta
     *
     * @param key
     * @return
     */
    public String remove(String key) {

        return Assert.notEmpty(key) && Assert.containsKey(mValueContent, key) ? mValueContent.remove(key) : null;
    }

    /**
     * to url
     *
     * @return
     */
    public String toURL() {
        String url = "";

        if (URLUtil.isNetworkUrl(mBaseURL)) {
            final long t = System.currentTimeMillis();

            final HttpUrl.Builder httpURLBuilder = HttpUrl.parse(mBaseURL).newBuilder();

            // path
            appendPath(httpURLBuilder);

            // QueryParameter
            LinkedHashMap<String, String> valueContent = new LinkedHashMap<>();

            // 添加当前url builder参数
            valueContent.put(C.tag.action, mAction);

            // 合并通用参数
            appendMetas(sGeneralMetas);
            appendMetas(sHeaderMetas);

            // app签名
            append(C.tag.cert, IRuntime.vriables().getCASSecret());

            // 时间戳
            append(C.tag.t, t);

            valueContent.putAll(mValueContent);

            // 添加到URLBuilder
            setURLQueryParameter(httpURLBuilder, valueContent);

            // sign
            Sign signImpl = IRuntime.getSign();
            if (signImpl != null) {
                httpURLBuilder.setQueryParameter(C.tag.sign, signImpl.generate(valueContent));
            }

            HttpUrl httpURL = null;

            try {
                httpURL = httpURLBuilder.build();
            } catch (Exception e) {
                Log.e(TAG, e);
            }

            url = httpURL != null ? httpURL.toString() : "";
        }

        return url;
    }

    /**
     * to url
     *
     * @return
     */
    @NonNull
    private Request.Builder toHTTPRequest() throws Exception {
        Request.Builder httpBuilder = new Request.Builder();

        if (URLUtil.isNetworkUrl(mBaseURL)) {
            final HttpUrl.Builder httpURLBuilder = HttpUrl.parse(mBaseURL).newBuilder();

            // path
            appendPath(httpURLBuilder);

            // 合并通用参数
            appendMetas(sHeaderMetas);
            appendMetas(sGeneralMetas);

            // 添加当前url builder参数
            append(C.tag.action, mAction);

            // 添加到sign算法
            if (Assert.notEmpty(mValueContent)) {
                Set<Map.Entry<String, String>> entrySet = mValueContent.entrySet();
                for (Map.Entry<String, String> meta : entrySet) {
                    if (meta != null) {
                        String key = meta.getKey(), value = meta.getValue();
                        if (Assert.notEmpty(key) && Assert.notEmpty(value)) {
                            httpURLBuilder.setQueryParameter(key, TextUtilz.toString(value));
                        }
                    }
                }
            }

            HttpUrl httpURL = null;

            try {
                httpURL = httpURLBuilder.build();
            } catch (Exception e) {
                Log.e(TAG, e);
            }

            String url = httpURL != null ? httpURL.toString() : "";
            if (Assert.notEmpty(url)) {
                httpBuilder.url(url);

                httpBuilder.header(C.tag.t, TextUtilz.toString(System.currentTimeMillis()));

                Sign signImpl = IRuntime.getSign();
                if (signImpl != null) {
                    // sign
                    httpBuilder.header(C.tag.sign, signImpl.generate(mValueContent));
                }
            }
        }

        return httpBuilder;
    }

    private void setURLQueryParameter(@NonNull HttpUrl.Builder httpURLBuilder, Map<String, String> valueContent) {
        if (Assert.notEmpty(valueContent)) {
            Set<Map.Entry<String, String>> entrySet = valueContent.entrySet();
            for (Map.Entry<String, String> meta : entrySet) {
                if (meta != null) {
                    String key = meta.getKey(), value = meta.getValue();
                    if (Assert.notEmpty(key) && Assert.notEmpty(value)) {
                        httpURLBuilder.setQueryParameter(key, TextUtilz.toString(value));
                    }
                }
            }
        }
    }

    /**
     * 添加通用参数到url参数列表中
     */
    private void appendMetas(Map<String, String> metaMap) {
        synchronized (URLBuilder.class) {
            if (Assert.notEmpty(metaMap)) {
                if (Assert.notEmpty(mValueContent)) {
                    Set<Map.Entry<String, String>> entrySet = metaMap.entrySet();
                    for (Map.Entry<String, String> meta : entrySet) {
                        if (meta != null) {
                            String key = meta.getKey(), value = meta.getValue();
                            if (Assert.notEmpty(key) && Assert.containsKey(mValueContent, key)) {
                                if (Assert.notEmpty(value)) {
                                    mValueContent.put(key, value);
                                }
                            } else {
                                mValueContent.put(key, value);
                            }
                        }
                    }
                } else {
                    mValueContent.putAll(metaMap);
                }
            }
        }
    }

    private void appendPath(HttpUrl.Builder httpURLBuilder) {
        if (Assert.notEmpty(mPathList) && httpURLBuilder != null) {
            for (String path : mPathList) {
                if (Assert.notEmpty(path)) {
                    httpURLBuilder.addPathSegment(path);
                }
            }
        }
    }
}
