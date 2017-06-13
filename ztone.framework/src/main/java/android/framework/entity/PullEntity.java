package android.framework.entity;

import android.assist.Assert;
import android.framework.AppResource;
import android.framework.R;
import android.framework.pull.PullFlag;
import android.json.JSONUtils;
import android.os.Parcel;
import android.support.annotation.StringRes;

import org.json.JSONObject;

import java.io.Serializable;

import static android.framework.AppResource.getString;

public class PullEntity extends Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据结果状态码,客户端定义值为整数, 服务端为负数
     */
    public static final int RESULT_FAIL = -19;
    public static final int RESULT_SUCCESS = 0;

    /**
     * 严重安全问题强制app退出
     */
    public static final int APP_ERROR = -1;
    /**
     * 版本问题,提示用户升级app;
     */
    public static final int VERSION_ERROR = -2;
    /**
     * sign验证失败;
     */
    public static final int SIGN_ERROR = -3;
    /**
     * 需要用户重新登录,此时app必须强制用户重新登录
     */
    public static final int RELOGIN = -5;
    /**
     * 权限受限,此时app必须强制用户重新登录
     */
    public static final int AUTHORITY_ERROR = -6;
    /**
     * 由于异常等情况没有找到值,此时需要服务端说明没值情况
     */
    public static final int VALUE_ERROR = -10;
    /**
     * 请求被中断
     */
    public static final int REQUEST_ABORTED = -15;
    /**
     * 由于网络原因引起请求失败
     */
    public static final int NETWORK_ERROR = -16;
    /**
     * 返回值为空
     */
    public static final int RESPONSE_NULL = -17;
    /**
     * 返回值解析错误
     */
    public static final int RESPONSE_ERROR = -18;
    /**
     * 返回值解析错误
     */
    public static final int RESPONSE_TIME_OUT = -19;
    /**
     * 无法确定的异常
     */
    public static final int UNDEFINE_ERROR = -999;
    /**
     * 自动登录
     */
    public static final int AUTO_LOGIN = 1;

    /**
     * 0--成功; <0失败,失败必须给原由
     */
    @FindJNode(i = RESULT_FAIL)
    public int result;

    /**
     * 提示语, 客户端必须提示
     */
    @FindJNode(jpath = "$msg")
    public String message;

    /**
     * 客户端数据更新间隔时间, 单位毫秒
     */
    public long intervalTime;

    /**
     * 调用方类型寄存
     */
    public PullFlag pullFlag;

    public PullEntity() {
        result = RESULT_FAIL;
    }

    public PullEntity(Parcel src) {
        readFromParcel(src);
    }

    @Override
    public PullEntity parse(JSONObject jsonData) {
        if (jsonData != null) {
            super.parse(jsonData);

            intervalTime = JSONUtils.optInt(jsonData, "$interval", 0) * 1000;
        } else {
            result = RESPONSE_NULL;
            message = getString(R.string.toast_error_response);
        }

        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(result);
        writeString(dest, message);
        dest.writeLong(intervalTime);
        dest.writeParcelable(pullFlag, flags);
    }

    @Override
    public PullEntity readFromParcel(Parcel src) {
        super.readFromParcel(src);

        result = src.readInt();
        message = src.readString();
        intervalTime = src.readLong();
        pullFlag = src.readParcelable(PullFlag.class.getClassLoader());

        return this;
    }

    @Override
    public boolean check() {

        return result == RESULT_SUCCESS;
    }

    public static String getEntityMessage(PullEntity entity, @StringRes int resId) {

        return getEntityMessage(entity, AppResource.getString(resId));
    }

    public static String getEntityMessage(PullEntity entity, String defaultMessage) {

        return entity != null && Assert.notEmpty(entity.message) ? entity.message : defaultMessage;
    }
}
