package android.framework.push.mq;

import android.assist.Assert;
import android.concurrent.ThreadUtils;
import android.framework.C;
import android.framework.module.FilePath;
import android.framework.push.MEClient;
import android.framework.push.MEOptions;
import android.framework.push.MEOptions.MESession;
import android.framework.push.MEState;
import android.io.FileUtils;
import android.log.Log;
import android.network.DNSUtils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;

public class MQTTClient extends MEClient implements MqttCallback {
    private static final String TAG = "MQTTClient";

    private MqttClient mMQTTClient;
    private MEOptions mMEOptions;

    public MQTTClient() {

    }

    @Override
    public synchronized void connect(MEOptions meOptions) throws Exception {
        mMEOptions = meOptions;

        connect();
    }

    private synchronized void connect() throws Exception {
        if (mMEOptions != null) {
            if (isConnected()) {
                try {
                    disconnect();
                } catch (Exception e) {
                    Log.d(TAG, e);
                }
            }

            mMQTTClient = new MqttClient(DNSUtils.lookup(mMEOptions.getURI(0)), mMEOptions.clientId, obtainPersistence());

            MqttConnectOptions mqttConnectOptions = toMqttConnectOptions(mMEOptions);

            // 尝试性连接
            // tryToConnect(mMQTTClient, mqttConnectOptions);

            mMQTTClient.setCallback(MQTTClient.this);

            // 正式连接
            mMQTTClient.connect(mqttConnectOptions);

            Log.i(TAG, "ME: MQTT connected: " + mMEOptions.clientId);
        } else {
            throw new MqttSecurityException(MEState.MESTATE_CONNECT_OPTION_ERROR);
        }
    }

    @Override
    public synchronized void disconnect() throws Exception {
        if (mMQTTClient != null) {
            mMQTTClient.disconnect();
        }
    }

    @Override
    public boolean isConnected() {

        return mMQTTClient != null && mMQTTClient.isConnected();
    }

    @Override
    public synchronized void publish(String topic, String text, int qos) throws Exception {
        if (mMQTTClient != null && Assert.notEmpty(topic) && Assert.notEmpty(text)) {
            mMQTTClient.publish(topic, text.getBytes(C.value.encoding), qos, false);

            Log.d(TAG, "ME: [Publish]: " + topic + ", msg: " + text);
        }
    }

    @Override
    public void subscribe(String topic, int qos) throws Exception {
        if (mMQTTClient != null && Assert.notEmpty(topic)) {
            mMQTTClient.subscribe(topic, qos);

            Log.d(TAG, "ME: [Subscribe]: " + topic);
        }
    }

    @Override
    public synchronized void unsubscribe(String... topicNames) {
        if (mMQTTClient != null && Assert.notEmpty(topicNames)) {
            try {
                mMQTTClient.unsubscribe(topicNames);

                Log.d(TAG, "ME: [Unsubscribe]: " + Arrays.asList(topicNames));
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, cause);

        if (mMERuntimeListener != null) {
            mMERuntimeListener.onChanged(MEState.MESTATE_CONNECT_LOSTED, //
                    (cause != null ? cause.getMessage() : "MQTT connection losted"));
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (mMEListener != null && Assert.notEmpty(topic) && message != null) {
            byte[] byteArray = message.getPayload();
            if (Assert.notEmpty(byteArray)) {
                String text = new String(byteArray, C.value.encoding);

                Log.d(TAG, "ME: [Topic]: " + topic + ", [Message]: " + text);

                mMEListener.onMessageArrived(topic, text);
            }
        } else {
            Log.w(TAG, "ME: [messageArrived]: mMEListener: " + //
                    (mMEListener != null ? "ok" : "null") + //
                    (", topic: " + topic) + //
                    (", message: " + message));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    /**
     * @param meProps
     * @return
     */
    private MqttConnectOptions toMqttConnectOptions(MEOptions meProps) {
        MqttConnectOptions options = new MqttConnectOptions();
        // 会影响连接
        // options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        if (meProps != null) {
            if (Assert.notEmpty(mMEOptions.hosts) && mMEOptions.hosts.length > 1) {
                options.setServerURIs(DNSUtils.lookupArrays(mMEOptions.hosts));
            }

            if (Assert.notEmpty(meProps.userName)) {
                options.setUserName(meProps.userName);
            }

            if (Assert.notEmpty(meProps.password)) {
                options.setPassword(meProps.password.toCharArray());
            }

            options.setCleanSession(meProps.meSession != null ? meProps.meSession.value : MESession.CLEAR.value);

            if (meProps.needLastWillTestament && Assert.notEmpty(meProps.topicNameLastWillTestament)
                    && Assert.notEmpty(meProps.msgLastWillTestament)) {
                options.setWill(meProps.topicNameLastWillTestament, meProps.msgLastWillTestament.getBytes(), //
                        MEOptions.QOS_2, false);
            }

            options.setConnectionTimeout(meProps.connectionTimeout);
            options.setKeepAliveInterval(meProps.keepAliveInterval);
        } else {
            options.setCleanSession(true);

            options.setConnectionTimeout(MEOptions.MEO_CONNECTION_TIMEOUT);
            options.setKeepAliveInterval(MEOptions.MEO_KEEP_ALIVE);
        }

        return options;
    }

    private MqttClientPersistence obtainPersistence() {
        MqttClientPersistence persistence = null;

        persistence = new MemoryPersistence();

        ThreadUtils.start(new Runnable() {

            @Override
            public void run() {
                FileUtils.delete(FilePath.mqtt().toFile());
            }

        }, "THREAD_MQTTCLIENT_PERSISTENCE");

        return persistence;
    }

    private void tryToConnect(MqttClient mqttClient, MqttConnectOptions mqttConnectOptions) {
        if (mqttClient != null && mqttConnectOptions != null) {
            try {
                mqttClient.setCallback(null);
                mqttClient.connect(mqttConnectOptions);
                Thread.sleep(300l);
                mqttClient.disconnect(3);
            } catch (Exception e) {
                Log.e(TAG, "ME: tryToConnect fail!");
            }
        }
    }
}
