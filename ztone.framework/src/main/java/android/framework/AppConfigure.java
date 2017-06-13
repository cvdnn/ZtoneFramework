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
import android.math.Maths;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-8-30
 */
public abstract class AppConfigure extends Configure {
    private static String TAG = "AppConfigure";

    private String baseUrl;

    private String tgtURL;
    private String shiro;
    private String mCASURI;

    private String[] meHost;
    private int mePort;
    private String meUserName;
    private String mePassword;

    private String[] dnsResolveHosts;

    @Override
    protected void onLoadProperties() throws Exception {
        baseUrl = get(C.properties.url_base);
        tgtURL = get(C.properties.url_tgt);
        mCASURI = get(C.properties.url_cas);
        shiro = get(C.properties.url_shiro);

        meHost = TextUtilz.blockSort(get(C.properties.me_host));
        mePort = Maths.valueOf(get(C.properties.me_port), -1);
        meUserName = get(C.properties.me_user_name);
        mePassword = get(C.properties.me_password);

        dnsResolveHosts = TextUtilz.blockSort(get(C.properties.dns_resolve_hosts));
    }

    public String meHost() {

        return Assert.notEmpty(meHost) ? meHost[0] : "";
    }

    public String[] meHostArrays() {

        return meHost;
    }

    public String baseUrl() {

        return baseUrl;
    }

    public String shiro() {

        return shiro;
    }

    public String tgt() {

        return tgtURL;
    }

    public String cas() {

        return mCASURI;
    }

    public String[] dnsResolveHosts() {

        return dnsResolveHosts;
    }

    public String mePassword() {

        return mePassword;
    }

    public int mePort() {

        return mePort;
    }

    public String meUserName() {

        return meUserName;
    }
}
