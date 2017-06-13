package android.framework.pull.interceptor;

import android.framework.AppResource;
import android.framework.R;
import android.framework.ResultMessage;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.PullEntity;
import android.framework.pull.PullFollow;
import android.log.Log;
import android.network.NetState;
import android.network.http.HTTPx;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import static android.framework.entity.PullEntity.NETWORK_ERROR;
import static android.framework.entity.PullEntity.RESPONSE_TIME_OUT;
import static android.framework.entity.PullEntity.RESULT_FAIL;
import static android.framework.entity.PullEntity.RESULT_SUCCESS;

final class RequestConnectionPullInterceptor implements PullInterceptor {
    private static final String TAG = "RequestConnectionPullInterceptor";

    @Override
    public <P extends PullEntity> P onIntercept(PullRelate<P> relate) {
        P p = relate.entity();

        PullFollow<P> follow = null;

        final Request request = relate.request();

        Call call = HTTPx.Client.Impl.newCall(request);

        if (relate instanceof PullClan.Chain.Relate) {
            PullClan.Chain.Relate<P> chainRelate = ((PullClan.Chain.Relate<P>) relate);

            chainRelate.setCall(call);

            follow = chainRelate.follow();
        }

        ResultMessage responseResult = ResultMessage.create(RESULT_FAIL);
        Response response = handleRequest(call, responseResult);
        if (responseResult.result == RESULT_SUCCESS) {
            if (follow == null) {
                follow = new PullFollow<>();
            }

            try {
                follow.onResponse(call, response);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        } else {
            p.result = responseResult.result;
            p.message = responseResult.message;
        }

        return p;
    }


    private <P extends PullEntity> Response handleRequest(Call call, ResultMessage result) {
        Response httpResponse = null;

        if (NetState.isConnected(LifeCycleUtils.component().app())) {
            try {
                httpResponse = call.execute();
                result.result = RESULT_SUCCESS;
            } catch (SocketTimeoutException e) {
                Log.e(TAG, e);

                result.result = RESPONSE_TIME_OUT;
                result.message = AppResource.getString(R.string.toast_error_socket_timeout);
            } catch (SocketException e) {
                Log.e(TAG, e);

                result.result = NETWORK_ERROR;
                result.message = AppResource.getString(R.string.toast_error_socket);
            } catch (Exception e) {
                Log.e(TAG, e);
            }

        } else {
            result.result = NETWORK_ERROR;
            result.message = AppResource.getString(R.string.toast_connect_internet_fail);
        }

        return httpResponse;
    }
}
