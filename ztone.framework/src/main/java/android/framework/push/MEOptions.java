package android.framework.push;

import android.assist.Assert;
import android.assist.TextLinker;
import android.assist.TextUtilz;
import android.framework.AppConfigure;
import android.framework.IParcelable;
import android.framework.IRuntime;
import android.framework.module.FilePath;
import android.log.Log;
import android.math.Maths;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import static android.framework.push.MEOptions.MESession.HOLD;

public class MEOptions extends IParcelable {
    private static final String TAG = "MEOptions";

    public static final String SEPARATOR_CACHE = "/";
    public static final String SEPARATOR_CACHE_QOS = "@";

    private static final String FORMAT_MQTT_CONNECT = "tcp://%1$s:%2$d";

    public static final String TOPIC_LAST_WILL_TESTAMENT = "lastWillTestament";
    public static final String MSG_LAST_WILL_TESTAMENT = "{\"sn\":\"%1$s\",\"time\":%2$d}";

    public static final String SUFFIX_ANDROID = "A";
    public static final String LINKER_CLIENT_ID = "-";

    public static final String TAG_CODE = "$code";
    public static final String TAG_TIME = "$t";

    public static final String SEPARATOR_TOPIC_NAME = ":";

    public static final String FORMAT_TOPIC_NAME = "%1$s@%2$d";

    /** http://www.blogjava.net/yongboy/archive/2014/02/15/409893.html */
    /**
     * Delivery Once no confirmation(至多发送一次，发送即丢弃。没有确认消息，也不知道对方是否收到)
     */
    public static final int QOS_0 = 0;
    /**
     * Delevery at least Once with confirmation(消息至少被传输一次)
     */
    public static final int QOS_1 = 1;
    /**
     * Delivery only once with confirmation with handshake(仅仅传输接收一次)
     */
    public static final int QOS_2 = 2;

    /**
     * Topic format for KeepAlives
     */
    public static final String MQTT_KEEP_ALIVE_TOPIC = "keepalive";
    /**
     * Default Keepalive QOS
     */
    public static final int MQTT_KEEP_ALIVE_QOS = QOS_0;

    /**
     * Retry intervals, when the connection is lost.
     */
    public static final long CONNECTION_SHORT_INTERVAL = 5000;
    public static final long CONNECTION_LONG_INTERVAL = 30000;
    public static final long CONNECTION_SHORT_RETRY_INTERVAL = 120000;
    public static final long CONNECTION_LONG_RETRY_INTERVAL = 270000;

    /**
     * keepalivetopic time
     */
    public static final long KAT_RETRY_INTERVAL_KEEP_ALIVE = 1200000;
    public static final long KAT_SHORT_RETRY_INTERVAL_KEEP_ALIVE = 600000;

    /**
     * keepalivetopic timeout
     */
    public static final long KAT_SCHEDULE_KEEP_ALIVE = 10000;

    /**
     * MEOption
     */
    public static final int MEO_SHORT_KEEP_ALIVE = 20;
    public static final int MEO_KEEP_ALIVE = 270;
    public static final int MEO_CONNECTION_TIMEOUT = 20;

    /**
     * 服務被回收後重啓時間
     */
    public static final long ME_RESTART_INTERVAL = 60000l;

    /**
     * 默认的MQTT的端口
     */
    public static final int MQTT_PORT = 1883;

    public static final int RETRY_KEEPALIVE_LOST_COUNT = 2;

    private static final String NODE_HOST = "host";
    private static final String NODE_PORT = "port";
    private static final String NODE_USER_NAME = "user_name";
    private static final String NODE_PASSWORD = "password";
    private static final String NODE_CLIENT_ID = "client_id";
    private static final String NODE_ME_SESSION = "me_session";

    private static final String NODE_KEEP_ALIVE = "keep_alive";
    private static final String NODE_CONNECTION_TIMEOUT = "connection_timeout";

    private static final String NODE_PROJECT_NAME = "project_name";
    private static final String NODE_KEEP_ALIVE_TOPIC = "keep_alive_topic";

    private static final String NODE_NEED_LWT = "need_lwt";
    private static final String NODE_TOPIC_LWT = "topic_lwt";
    private static final String NODE_MESSAGE_LWT = "msg_lwt";

    public static MEOptions mDefaultMEOption;

    public static File obtainMEOptionsFile() {

        return new File(obtainMEOptionsFilePath());
    }

    public static String obtainMEOptionsFilePath() {

        return FilePath.cache().append("MEOptions.ccm").toFilePath();
    }

    public static String formatURI(String uri, int port) {

        return Assert.notEmpty(uri) ? String.format(FORMAT_MQTT_CONNECT, uri, port) : "";
    }

    private static String[] formatURIs(String[] hosts, int port) {
        String[] hostArray = null;

        if (Assert.notEmpty(hosts)) {
            hostArray = new String[hosts.length];
            if (Assert.notEmpty(hosts)) {
                for (int i = 0; i < hosts.length; i++) {
                    hostArray[i] = formatURI(hosts[i], port);
                }
            }
        }

        return hostArray;
    }

	/* *************************************
     *
	 * 
	 * ************************************* */

    public String[] hosts;
    public String userName;
    public String password;
    public String clientId;
    public MESession meSession;
    public int keepAliveInterval;
    public int connectionTimeout;

    public String projectName;
    /**
     * 用于监控topic
     */
    public long keepAliveTopicInterval;

    public boolean needLastWillTestament;
    public String topicNameLastWillTestament;
    public String msgLastWillTestament;

    private MEOptions() {

    }

    private MEOptions(Parcel src) {
        readFromParcel(src);
    }

    public static MEOptions obtain() {
        if (mDefaultMEOption == null) {
            synchronized (MEOptions.class) {
                if (mDefaultMEOption == null) {
                    mDefaultMEOption = new MEOptions();

                    AppConfigure appConfigure = IRuntime.appConfig();

                    mDefaultMEOption.hosts = formatURIs(appConfigure.meHostArrays(), appConfigure.mePort());

                    mDefaultMEOption.userName = appConfigure.meUserName();
                    mDefaultMEOption.password = appConfigure.mePassword();

                    String clientId = TextLinker.create(LINKER_CLIENT_ID)
                            .append(IRuntime.vriables().getAppCode())
                            .append(IRuntime.vriables().getClientId()).toString();

                    mDefaultMEOption.clientId = TextLinker.create(LINKER_CLIENT_ID)
                            .append(SUFFIX_ANDROID)
                            .append(TextUtilz.toTrim(clientId)).toString();

                    mDefaultMEOption.meSession = HOLD;
                    mDefaultMEOption.connectionTimeout = MEO_CONNECTION_TIMEOUT;
                    mDefaultMEOption.keepAliveInterval = MEO_KEEP_ALIVE;

                    mDefaultMEOption.projectName = IRuntime.vriables().getAppCode();
                    mDefaultMEOption.keepAliveTopicInterval = KAT_RETRY_INTERVAL_KEEP_ALIVE;

                    mDefaultMEOption.needLastWillTestament = true;

                    mDefaultMEOption.topicNameLastWillTestament = TextLinker.create(SEPARATOR_TOPIC_NAME)
                            .append(mDefaultMEOption.projectName)
                            .append(TOPIC_LAST_WILL_TESTAMENT).toString();

                    mDefaultMEOption.msgLastWillTestament = String.format(MSG_LAST_WILL_TESTAMENT,
                            IRuntime.vriables().getClientId(), System.currentTimeMillis());
                }
            }
        }

        return mDefaultMEOption;
    }

    public String getURI(int index) {

        return Assert.notEmpty(hosts) && index >= 0 && hosts.length > index ? hosts[index] : "";
    }

    public String toJSON() {
        String strJSON = null;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NODE_HOST, hosts);
            jsonObject.put(NODE_USER_NAME, userName);
            jsonObject.put(NODE_PASSWORD, password);
            jsonObject.put(NODE_CLIENT_ID, clientId);
            jsonObject.put(NODE_ME_SESSION, meSession != null ? meSession.pseudo : MESession.CLEAR.pseudo);
            jsonObject.put(NODE_KEEP_ALIVE, keepAliveInterval);
            jsonObject.put(NODE_CONNECTION_TIMEOUT, connectionTimeout);
            jsonObject.put(NODE_PROJECT_NAME, projectName);
            jsonObject.put(NODE_KEEP_ALIVE_TOPIC, keepAliveTopicInterval);
            jsonObject.put(NODE_NEED_LWT, needLastWillTestament);
            jsonObject.put(NODE_TOPIC_LWT, topicNameLastWillTestament);
            jsonObject.put(NODE_MESSAGE_LWT, msgLastWillTestament);

            strJSON = TextUtilz.toFake(jsonObject.toString());
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        return strJSON;
    }

    public boolean fromJSON(String text) {
        boolean result = false;
        if (Assert.notEmpty(text)) {
            String json = TextUtilz.fromFake(text);
            if (Assert.notEmpty(json)) {
                try {
                    JSONObject jsonObject = new JSONObject(json);

                    if (jsonObject.has(NODE_HOST)) {
                        JSONArray jsonArray = jsonObject.optJSONArray(NODE_HOST);
                        if (Assert.notEmpty(jsonArray)) {
                            int len = jsonArray.length();
                            hosts = new String[len];
                            for (int i = 0; i < len; i++) {
                                hosts[i] = jsonArray.optString(i);
                            }
                        }
                    }

                    if (jsonObject.has(NODE_USER_NAME)) {
                        userName = jsonObject.optString(NODE_USER_NAME);
                    }

                    if (jsonObject.has(NODE_PASSWORD)) {
                        password = jsonObject.optString(NODE_PASSWORD);
                    }

                    if (jsonObject.has(NODE_CLIENT_ID)) {
                        clientId = jsonObject.optString(NODE_CLIENT_ID);
                    }

                    if (jsonObject.has(NODE_ME_SESSION)) {
                        meSession = MESession.from(jsonObject.optInt(NODE_ME_SESSION));
                    }

                    if (jsonObject.has(NODE_KEEP_ALIVE)) {
                        keepAliveInterval = jsonObject.optInt(NODE_KEEP_ALIVE);
                    }

                    if (jsonObject.has(NODE_CONNECTION_TIMEOUT)) {
                        connectionTimeout = jsonObject.optInt(NODE_CONNECTION_TIMEOUT);
                    }

                    if (jsonObject.has(NODE_PROJECT_NAME)) {
                        projectName = jsonObject.optString(NODE_PROJECT_NAME);
                    }

                    if (jsonObject.has(NODE_KEEP_ALIVE_TOPIC)) {
                        keepAliveTopicInterval = jsonObject.optInt(NODE_KEEP_ALIVE_TOPIC);
                    }

                    if (jsonObject.has(NODE_NEED_LWT)) {
                        needLastWillTestament = jsonObject.optBoolean(NODE_NEED_LWT);
                    }

                    if (jsonObject.has(NODE_TOPIC_LWT)) {
                        topicNameLastWillTestament = jsonObject.optString(NODE_TOPIC_LWT);
                    }

                    if (jsonObject.has(NODE_MESSAGE_LWT)) {
                        msgLastWillTestament = jsonObject.optString(NODE_MESSAGE_LWT);
                    }

                    result = true;
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {

        return new StringBuilder().append("host=").append(hosts) //
                .append(", userName=").append(userName) //
                .append(", password=").append(password) //
                .append(", clientId=").append(clientId) //
                .append(", projectName=").append(projectName).toString();
    }

    @Override
    public boolean equals(Object o) {

        return o != null && toString().equals(o.toString());
    }

    @Override
    public int hashCode() {

        return toString().hashCode();
    }

    @Override
    public int describeContents() {

        return 0;
    }

    public void readFromParcel(Parcel src) {
        if (src != null) {
            hosts = TextUtilz.blockSort(src.readString());
            userName = src.readString();
            password = src.readString();

            clientId = src.readString();

            meSession = MESession.from(src.readInt());

            keepAliveInterval = src.readInt();
            connectionTimeout = src.readInt();

            projectName = src.readString();
            keepAliveTopicInterval = src.readLong();

            needLastWillTestament = Maths.valueOf(src.readString());
            topicNameLastWillTestament = src.readString();
            msgLastWillTestament = src.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            writeString(dest, TextLinker.create().append(hosts).toString());
            writeString(dest, userName);
            writeString(dest, password);

            writeString(dest, clientId);

            dest.writeInt(meSession != null ? meSession.pseudo : MESession.CLEAR.pseudo);

            dest.writeInt(keepAliveInterval);
            dest.writeInt(connectionTimeout);

            writeString(dest, projectName);
            dest.writeLong(keepAliveTopicInterval);

            writeString(dest, Boolean.toString(needLastWillTestament));
            writeString(dest, topicNameLastWillTestament);
            writeString(dest, msgLastWillTestament);
        }
    }

    public static final Parcelable.Creator<MEOptions> CREATOR = new Parcelable.Creator<MEOptions>() {

        @Override
        public MEOptions createFromParcel(Parcel in) {

            return new MEOptions(in);
        }

        @Override
        public MEOptions[] newArray(int size) {

            return new MEOptions[size];
        }
    };

    public enum MESession {
        CLEAR(true, 0), HOLD(false, 1);

        public boolean value;
        public int pseudo;

        MESession(boolean value, int pseudo) {
            this.value = value;
            this.pseudo = pseudo;
        }

        public static MESession from(boolean value) {
            return value ? CLEAR : HOLD;
        }

        public static MESession from(int pseudo) {

            return pseudo == 1 ? HOLD : CLEAR;
        }
    }
}
