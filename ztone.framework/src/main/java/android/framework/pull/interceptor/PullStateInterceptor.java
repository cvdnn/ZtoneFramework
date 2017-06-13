package android.framework.pull.interceptor;

import android.extend.view.module.ToastConsole;
import android.framework.AppResource;
import android.framework.C;
import android.framework.R;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.PullEntity;
import android.math.Maths;
import android.network.NetState;
import android.network.http.HTTPx;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import static android.framework.entity.PullEntity.NETWORK_ERROR;
import static android.network.http.HTTPx.VALIDITY_TIME;

/**
 * Created by handy on 17-1-22.
 */

final class PullStateInterceptor implements PullInterceptor {

    @Override
    public <P extends PullEntity> P onIntercept(PullRelate<P> relate) {
        P p = relate.entity();


        if (NetState.isConnected(LifeCycleUtils.component().app())) {
            Request request = relate.request();
            HttpUrl url = request.url();

            if (NetState.isInternetConnected(url.host(), url.port())) {
                p = relate.proceed(relate.request());

            } else {
                p.result = NETWORK_ERROR;
                p.message = AppResource.getString(R.string.toast_connect_internet_fail);
            }

        } else {
            p.result = NETWORK_ERROR;
        }

        return p;
    }

    private static boolean accept(Response response, int api) {
        boolean result = false;

        if (response != null) {
            if (api >= C.value.api_origin_version) { // 起始协议版本 3
                // 有效时间判断
                result = System.currentTimeMillis() - Maths.valueOf(response.headers(HTTPx.Headers.REPLY_TIMESTAMP), 0l) <= VALIDITY_TIME;
                if (!result) {
                    ToastConsole.show(AppResource.getString(R.string.toast_error_response_timeout));
                }

            } else {
                result = true;
            }
        }

        return result;
    }
}
