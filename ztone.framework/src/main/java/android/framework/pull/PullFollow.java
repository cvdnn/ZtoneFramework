package android.framework.pull;

import android.assist.Assert;
import android.framework.AppResource;
import android.framework.C;
import android.framework.Loople;
import android.framework.R;
import android.framework.entity.PullEntity;
import android.framework.pull.interceptor.PullClan;
import android.framework.util.ValidateUtils;
import android.log.Log;
import android.network.http.HTTPx;
import android.network.http.ResponseTransform;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import static android.framework.entity.PullEntity.NETWORK_ERROR;
import static android.framework.entity.PullEntity.RESPONSE_NULL;
import static android.framework.entity.PullEntity.RESPONSE_TIME_OUT;
import static android.framework.entity.PullEntity.RESULT_FAIL;

/**
 * Created by handy on 17-4-1.
 */
public class PullFollow<E extends PullEntity> implements OnPullListener<E>, okhttp3.Callback {
    private static final String TAG = "PullFollow";

    @Override
    public void onPulled(int position, @NonNull E entity, String url) {

    }

    @Override
    public void onError(int errorCode, String message) {

    }

    @Override
    public final void onResponse(Call call, Response response) throws IOException {
        Request request = response.request();

        PullClan.Holder<E> pHolder = getRequestHolder(request);
        if (pHolder != null && pHolder.check()) {
            String strJson = ResponseTransform.toText(response);
            if (Assert.notEmpty(strJson)) {
                pHolder.entity.parse(strJson);

            } else {
                pHolder.entity.result = RESPONSE_NULL;
                pHolder.entity.message = AppResource.getString(R.string.toast_error_response);
            }

            HttpUrl httpUrl = request.url();
            String url = httpUrl.toString(), action = HTTPx.query(httpUrl, C.tag.action);
            Log.d(TAG, "NN: [%s]: %s" , action , url);
            Log.d(TAG, "NN: [%s]: %s", action, pHolder.entity.format().toString());
        }
    }

    @Override
    public final void onFailure(final Call call, final IOException e) {
        Request request = call.request();

        PullClan.Holder<E> pHolder = getRequestHolder(request);
        if (pHolder != null && pHolder.check()) {
            pHolder.entity.result = NETWORK_ERROR;

            if (e instanceof SocketTimeoutException) {
                pHolder.entity.result = RESPONSE_TIME_OUT;
                pHolder.entity.message = AppResource.getString(R.string.toast_error_socket_timeout);

            } else if (e instanceof SocketException) {
                pHolder.entity.result = NETWORK_ERROR;
                pHolder.entity.message = AppResource.getString(R.string.toast_error_socket);

            } else {
                pHolder.entity.result = NETWORK_ERROR;

                Log.e(TAG, e);
            }
        }
    }

    public PullClan.Holder<E> getRequestHolder(Request request) {
        PullClan.Holder<E> pHolder = null;

        Object tempHolder = request.tag();
        if (tempHolder instanceof PullClan.Holder) {
            pHolder = (PullClan.Holder<E>) tempHolder;
        }

        return pHolder;
    }

    public final void post(final int position, final E entity, final String url) {
        Loople.post(new Runnable() {

            @Override
            public void run() {
                if (ValidateUtils.check(entity)) {
                    try {
                        onPulled(position, entity, url);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                } else {
                    int code = RESULT_FAIL;
                    String message = "";

                    if (entity != null) {
                        code = entity.result;
                        message = entity.message;
                    }

                    try {
                        onError(code, message);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                }
            }
        });
    }

    /**
     * ******************************************************
     * <p>
     * <p>
     * ********************************************************
     */

    private static class Wrap<E extends PullEntity> extends PullFollow<E> {
        private final OnPullListener<E> mPullListener;

        public Wrap(OnPullListener<E> pullListener) {
            this.mPullListener = pullListener;
        }

        @Override
        public void onPulled(int position, @NonNull E entity, String url) {
            super.onPulled(position, entity, url);

            if (mPullListener != null) {
                mPullListener.onPulled(position, entity, url);
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            super.onError(errorCode, message);

            if (mPullListener != null) {
                mPullListener.onError(errorCode, message);
            }
        }

    }

    public static <E extends PullEntity> PullFollow<E> wrap(OnPullListener<E> listener) {

        return new Wrap<>(listener);
    }
}
