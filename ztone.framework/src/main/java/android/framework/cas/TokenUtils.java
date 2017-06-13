package android.framework.cas;

import android.assist.Assert;
import android.framework.AppConfigure;
import android.framework.AppResource;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.PersistenceUtils;
import android.framework.R;
import android.framework.builder.URLBuilder;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.PullEntity;
import android.framework.entity.ResultEntity;
import android.framework.util.ValidateUtils;
import android.log.Log;
import android.network.NetState;
import android.network.http.HTTPx;
import android.network.http.MIME;
import android.network.http.ResponseTransform;

import org.json.JSONObject;

import java.util.Arrays;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.framework.IRuntime.appConfig;

/**
 * Created by handy on 16-11-3.
 */
public class TokenUtils {
    private static final String TAG = "TokenUtils";

    public static final String TAG_TOKEN_TGT = "__token_tgt";

    public static final String COOKIE_CASTGC = "CASTGC";

    private static String mTicketGrantingTicket;

    public static String getSettingTGT() {

        return PersistenceUtils.get(TAG_TOKEN_TGT);
    }

    public static String getTicketGrantingTicket() {

        return mTicketGrantingTicket;
    }

    /**
     * 获取TGT令牌，需要客户端保存，登录第三方子系统需要通过此令牌生成ST令牌
     *
     * @param requestBody
     * @return
     */
    public static SSOEntity handleTicketGrantingTicket(RequestBody requestBody) {
        final SSOEntity entity = new SSOEntity();

        AppConfigure config = appConfig();
        if (Assert.notEmpty(config.tgt())) {
            HttpUrl url = HttpUrl.parse(config.tgt());
            if (NetState.isInternetConnected(url.host(), url.port())) {
                if (requestBody != null) {
                    try {
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        Response response = HTTPx.Client.execute(request);
                        JSONObject json = ResponseTransform.toJSON(response);

                        entity.parse(json);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }

                    if (ValidateUtils.check(entity)) {
                        mTicketGrantingTicket = entity.tgt;

                        LifeCycleUtils.component().setting().put(TAG_TOKEN_TGT, mTicketGrantingTicket);

                        String strURL = IRuntime.appConfig().cas();
                        HttpUrl httpUrl = HttpUrl.parse(strURL);

                        Cookie tgcCookie = new Cookie.Builder().domain(httpUrl.host()).name(COOKIE_CASTGC).value(mTicketGrantingTicket).build();

                        HTTPx.Client.Impl.cookieJar().saveFromResponse(httpUrl, Arrays.asList(tgcCookie));

//                        CookieUtils.sync(LifeCycleUtils.component().app(), httpUrl.toString(), Arrays.asList(tgcCookie));
                    }
                } else {
                    entity.message = AppResource.getString(R.string.toast_data_error);
                }
            } else {
                entity.result = PullEntity.NETWORK_ERROR;
                entity.message = AppResource.getString(R.string.toast_connect_internet_fail);
            }
        }

        return entity;
    }

    /**
     * 获取ST令牌（一次性令牌），此令牌由TGT令牌生成<br>
     * 登录其他子系统只需要在参数中加入此令牌，如：http://aaa.com?ticket=xxxxx
     *
     * @return
     */
    @Deprecated
    public static ResultEntity handleServiceTicket() {
        ResultEntity result = new ResultEntity();

        if (Assert.notEmpty(mTicketGrantingTicket) && Assert.notEmpty(IRuntime.appConfig().tgt())) {
            String shiro = IRuntime.appConfig().shiro();

            // ST
            RequestBody formBody = RequestBody.create(MIME.TEXT_PLAIN, "service=" + URLBuilder.encode(shiro));
            Request request = new Request.Builder().url(IRuntime.appConfig().tgt() + "/" + mTicketGrantingTicket).post(formBody).build();
            Response response = HTTPx.Client.execute(HTTPx.Client.Impl, request);
            SSOEntity ssoEntity = (SSOEntity) new SSOEntity().parse(ResponseTransform.toJSON(response));
            if (ValidateUtils.check(ssoEntity)) {

                // shiro-cas
                HttpUrl casHttp = HttpUrl.parse(shiro).newBuilder().setQueryParameter(C.tag.ticket, ssoEntity.st).build();
                Request casRequest = new Request.Builder().url(casHttp).build();
                response = HTTPx.Client.execute(HTTPx.Client.Impl, casRequest);
                result.parse(ResponseTransform.toJSON(response));
            }
        }

        return result;
    }
}
