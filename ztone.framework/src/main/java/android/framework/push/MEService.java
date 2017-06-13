package android.framework.push;

import android.assist.Assert;
import android.concurrent.ThreadUtils;
import android.content.Intent;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.R;
import android.framework.context.FrameService;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.push.MEClient.OnMERuntimeListener;
import android.framework.push.entity.METopicEntity;
import android.framework.push.mq.MQTTClient;
import android.io.FileUtils;
import android.log.Log;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static android.framework.C.value.charset_encoding;
import static android.framework.push.MEState.MESTATE_AVAILABLE;
import static android.framework.push.MEState.MESTATE_CONNECT_DESTROY;
import static android.framework.push.MEState.MESTATE_CONNECT_FAIL;
import static android.framework.push.MEState.MESTATE_CONNECT_FAILED_AUTHENTICATION;
import static android.framework.push.MEState.MESTATE_CONNECT_LOSTED;
import static android.framework.push.MEState.MESTATE_CONNECT_LOSTED_LONG_TIME;
import static android.framework.push.MEState.MESTATE_CONNECT_OPTION_ERROR;
import static android.framework.push.MEState.MESTATE_CONNECT_RETRY;
import static android.framework.push.MEState.MESTATE_CONNECT_SUCCESS;
import static android.framework.push.MEState.MESTATE_NONE;
import static android.framework.push.MEState.MESTATE_PUBLISH_FAIL;
import static android.framework.push.MEState.MESTATE_START_MQTT_RUNNING;
import static android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY;

public final class MEService extends FrameService {
    private static final String TAG = "MEService";

    private static final int M_SERVICE_ID = R.id.me_service;

    private static final int REPEAT_CONNECT_COUNT = 5;
    private static final int REPEAT_LONG_CONNECT_COUNT = 8;
    private static final int REPEAT_MAX_CONNECT_COUNT = 10;

    private static final byte[] TOKEN_RETRY_CONNECT = {};

    private static final MEDeamon mMEDemon = new MEDeamon();
    private static final MEClient mMEClient = new MQTTClient();

    private static final ArrayList<METopicEntity> mPublishQueue = new ArrayList<METopicEntity>();
    private static final ArrayList<METopicEntity> mSubscribeQueue = new ArrayList<METopicEntity>();

    /**
     * 需要作topic監控的隊列
     */
    private static final ArrayMap<String, METopicEntity> mKeepAliveMap = new ArrayMap<String, METopicEntity>();
    private static final ArrayMap<String, byte[]> mKeepAliveTokenMap = new ArrayMap<String, byte[]>();

    private static final String NODE_KEEP_ALIVE = "keepalive";

    public static void startAppClient() {
        startAppClient(false);
    }

    public static void startAppClient(boolean daemonFlag) {
        Intent appIntent = new Intent(IRuntime.obtainAPPClientAction());
        appIntent.setPackage(IRuntime.getPackageName());

        appIntent.putExtra(C.tag.deamon_flag, daemonFlag ? C.flag.app_daemon : C.flag.none);

        LifeCycleUtils.component().app().startService(appIntent);
    }

	/* **********************************************
     *
	 *
	 * **********************************************
	 */

    private static MEOptions mMEOptions;

    /**
     * Main lock guarding all access
     */
    private final ReentrantLock mLock = new ReentrantLock(false);

    /**
     * Condition for waiting takes
     */
    private final HandleMETopicThread mPublishMETopicHandle;
    private final HandleMETopicThread mSubscribeMETopicHandle;

    private int mMEState;
    /**
     * 本次连接时重连标志,当离线后触发重连会设置该标志
     */
    private int mMERetryConnectState = MESTATE_NONE;

    private final Handler mMEThreadHandler;

    private int mRepeatConnectCount;

    /**
     * 在重连时是否需要重新订阅队列标识
     */
    private int mNeedResubscribeFlag;

    public MEService() {
        HandlerThread mKeepAliveArrivedThread = new HandlerThread("KEEP_ALIVE_ARRIVED_THREAD");
        mKeepAliveArrivedThread.start();
        mMEThreadHandler = new Handler(mKeepAliveArrivedThread.getLooper());

        // ME core thread
        mPublishMETopicHandle = new HandleMETopicThread("ME_PUBLISH_THREAD", mPublishRunnable).startHandle();
        mSubscribeMETopicHandle = new HandleMETopicThread("ME_SUBSCRIBE_THREAD", mSubscribeRunnable).startHandle();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setStartFlag(START_STICKY);

        Process.setThreadPriority(THREAD_PRIORITY_URGENT_DISPLAY);

        startForeground(C.flag.none, true);

        mMEClient.setOnMERuntimeListener(mMERuntimeListener);
    }

    @Override
    public void postStartCommand(final Intent intent, int flags, int startId) {
        super.postStartCommand(intent, flags, startId);

        if (intent != null) {
            String action = intent.getAction();
            if (MEAction.messageExchange().equals(action)) {
                String clazz = intent.getStringExtra(C.tag.me_clazz);
                int meFlag = intent.getIntExtra(C.tag.me_flag, C.flag.none);

                Log.i(TAG, "ME: call By clazz: " + clazz + ", action: " + meFlag);

                if (meFlag != C.flag.none) {
                    if (meFlag == C.flag.me_start_sevice) {
                        if (isMEClientConnected()) {
                            // 如果当前连接可用, 直接知会调用方,无需改变MEState状态
                            MEState.sendMEStateBroadcast(MESTATE_AVAILABLE, "MESTATE_AVAILABLE");

                        } else if (mMEState != MESTATE_START_MQTT_RUNNING) {
                            MEOptions tempOptions = intent.getParcelableExtra(C.tag.me_options);
                            if (tempOptions != null) {
                                mMEOptions = tempOptions;
                                writeMEOptions();

                            } else {
                                tempOptions = MEOptions.obtain();
                                if (tempOptions.fromJSON(readMEOptions())) {
                                    mMEOptions = tempOptions;

                                } else {
                                    cleanMEOptions();
                                }
                            }

                            if (mMEOptions != null) {
                                mMEThreadHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        start();
                                    }
                                });
                            } else {
                                // 后面情况预留调用者自己处理
                                sendMEStateBroadcast(MESTATE_CONNECT_OPTION_ERROR, "MQTT connected error");
                            }
                        }
                    } else if (meFlag == C.flag.me_stop_sevice) {
                        stop();

                    } else if (meFlag == C.flag.me_sevice_reconnection) {
                        reconnect(true);

                    } else if (meFlag == C.flag.me_start_foreground) {
                        stopForeground();

                        startForeground(M_SERVICE_ID, true);
                    } else if (meFlag == C.flag.me_stop_foreground) {
                        stopForeground();

                    } else if (meFlag == C.flag.me_check_subscribe) {
                        String topicName = intent.getStringExtra(C.tag.me_topic);
                        checkSubscribe(topicName);

                    } else {
                        // METopicEntity kaMEEntity = getKeepAliveMEEntity(intent);

                    }
                } else {
                    if (intent.getIntExtra(C.tag.deamon_flag, C.flag.none) == C.flag.app_daemon) {
                        startAppClient(true);
                    }
                }
            } else {
                Log.i(TAG, "ME: Starting service with no action Probably from a crash");
            }

        } else if (mMEState != MESTATE_NONE) {
            Log.i(TAG, "ME: Starting service by system");

            if (mMEOptions == null) {
                MEOptions tempOptions = MEOptions.obtain();
                if (tempOptions.fromJSON(readMEOptions())) {
                    mMEOptions = tempOptions;
                }
            }

            reconnect(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mMEBinderStub;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

        Log.i(TAG, "ME: onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "ME: onUnbind");

        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        startAppClient(true);

        mMEDemon.setRetryInterval(MEOptions.ME_RESTART_INTERVAL);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level >= TRIM_MEMORY_MODERATE) {
            startAppClient(true);

            mMEDemon.setRetryInterval(MEOptions.ME_RESTART_INTERVAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "ME: onDestroy!!!");

        try {
            mMEClient.disconnect();
        } catch (Throwable t) {
            Log.v(TAG, TAG);
        }

        mMEDemon.stop();

        stopForeground();
    }

    private void start() {
        if (mMEState != MESTATE_START_MQTT_RUNNING) {
            mMEState = MESTATE_START_MQTT_RUNNING;

            mLock.lock();

            try {
                if (mMEOptions != null) {
                    if (!mMEClient.isConnected()) {
                        mMEClient.connect(mMEOptions);
                    }

                    mPublishMETopicHandle.notifyToHandle();
                    mSubscribeMETopicHandle.notifyToHandle();

                    mMEDemon.setKeepAliveTopicInterval(mMEOptions.keepAliveTopicInterval);
                    mMEDemon.start(MEService.this);

                    // 只有当重新连接时才需要重新订阅
                    if (mNeedResubscribeFlag == MESTATE_CONNECT_SUCCESS) {
                        resubscribeKeepAliveMETopic();
                    } else {
                        // 设置重新连接时订阅队列
                        mNeedResubscribeFlag = MESTATE_CONNECT_SUCCESS;
                    }

                    mMERetryConnectState = MESTATE_NONE;
                    mRepeatConnectCount = 0;

                    // 通知应用ME已经连接成功
                    sendMEStateBroadcast(MESTATE_CONNECT_SUCCESS, "MTQQ connect success");
                } else {
                    cleanMEOptions();

                    sendMEStateBroadcast(MESTATE_CONNECT_OPTION_ERROR, "MQTT connected error");
                }

            } catch (MqttSecurityException e) {
                Log.e(TAG, e);

                cleanMEOptions();

                int reasonCode = e != null ? e.getReasonCode() : MESTATE_CONNECT_OPTION_ERROR;
                int resultCode = reasonCode == MqttException.REASON_CODE_FAILED_AUTHENTICATION ? MESTATE_CONNECT_FAILED_AUTHENTICATION
                        : MESTATE_CONNECT_OPTION_ERROR;
                sendMEStateBroadcast(resultCode, "MQTT connected error");
            } catch (Exception e) {
                Log.e(TAG, e);

                sendMEStateBroadcast(MESTATE_CONNECT_FAIL, //
                        (e != null ? e.getMessage() : "MTQQ connect fail"));

                retryConnect();
            } finally {
                mLock.unlock();
            }

        } else {
            Log.i(TAG, "ME: MQTT is connecting, waiting for a moment");
        }
    }

    private void stop() {
        mMEState = MESTATE_NONE;
        mMERetryConnectState = MESTATE_NONE;

        mMEDemon.stop();

        mMEThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isMEClientConnected()) {
                    mLock.lock();

                    try {
                        mMEClient.disconnect();

                        sendMEStateBroadcast(MESTATE_CONNECT_DESTROY, "MQTT connected destroy");

                        stopSelf();
                    } catch (Exception e) {
                        Log.d(TAG, e);

                    } finally {
                        mLock.unlock();
                    }
                }
            }
        });
    }

    /**
     * 重连
     *
     * @param cleaRetryConnectState 是否需要清理重连状态, true--重头开始重连
     */
    private void reconnect(boolean cleaRetryConnectState) {
        Log.i(TAG, "ME: MQTT reconnect");

        // 清理重连状态, 重头开始
        if (cleaRetryConnectState) {
            if (mMEThreadHandler != null) {
                mMEThreadHandler.removeCallbacksAndMessages(TOKEN_RETRY_CONNECT);
            }

            mMERetryConnectState = MESTATE_NONE;

            mRepeatConnectCount = 0;
        }

        ThreadUtils.start(new Runnable() {

            @Override
            public void run() {
                if (isMEClientConnected()) {
                    try {
                        mMEClient.disconnect();
                    } catch (Exception e) {
                        Log.d(TAG, e);
                    }
                }

                start();
            }
        }, "ME_START_THREAD");

    }

    public boolean isMEClientConnected() {

        return mMEState == MESTATE_CONNECT_SUCCESS && mMEClient.isConnected();
    }

    private void checkSubscribe(String topicName) {
        if (Assert.notEmpty(topicName)) {

        }
    }

    private void sendMEStateBroadcast(int meState, String msg) {
        if (mMEState != meState) {
            MEState.sendMEStateBroadcast(meState, msg);

            mMEState = meState;
        }
    }

    private final OnMERuntimeListener mMERuntimeListener = new OnMERuntimeListener() {

        @Override
        public void onChanged(int flag, String msg) {
            Log.d(TAG, "ME: [MERUNTIME]: flag: " + flag + ", msg: " + msg);

            if (flag == MESTATE_CONNECT_LOSTED) {
                if (mMEState == MESTATE_CONNECT_SUCCESS) {
                    mMEDemon.setRetryInterval(MEOptions.CONNECTION_SHORT_INTERVAL);
                }
            }

            sendMEStateBroadcast(MESTATE_CONNECT_FAIL, msg);
        }
    };

    private final IMEBinder.Stub mMEBinderStub = new IMEBinder.Stub() {

        @Override
        public IMEBinder publish(final String topic, final String text, final int qos) throws RemoteException {
            if (Assert.notEmpty(topic) && Assert.notEmpty(text)) {
                METopicEntity meTopic = getMETopic(mPublishQueue, topic);
                if (meTopic != null) {
                    meTopic.qos = qos;
                    meTopic.payload = text;

                } else {
                    mPublishQueue.add(METopicEntity.create(topic, text, qos));
                }

                Log.v(TAG, "ME: add publish queue: " + topic);

                try {
                    mLock.lock();

                    mPublishMETopicHandle.notifyToHandle();
                } catch (Exception e) {
                    Log.e(TAG, e);

                } finally {
                    mLock.unlock();
                }
            } else {
                Log.i(TAG, "ME: publish fail: topic is " + topic);

            }

            return this;
        }

        @Override
        public IMEBinder subscribe(String topic, int qos, boolean needKeepAlive) throws RemoteException {
            addToSubscribeQueue(METopicEntity.create(topic, qos, needKeepAlive));

            return this;
        }

        public IMEBinder unsubscribe(final String topicName) throws RemoteException {
            if (mKeepAliveMap != null) {
                mKeepAliveMap.remove(topicName);
            }

            if (Assert.notEmpty(mSubscribeQueue)) {
                for (METopicEntity meTopic : mSubscribeQueue) {
                    if (METopicEntity.check(meTopic) && meTopic.topic.equals(topicName)) {
                        mSubscribeQueue.remove(meTopic);

                        break;
                    }
                }
            }

            if (mMEClient != null && Assert.notEmpty(topicName)) {
                try {
                    mMEClient.unsubscribe(topicName);
                } catch (Exception e) {
                    Log.e(TAG, e);

                    throw new RemoteException();
                }
            }

            return this;
        }

        @Override
        public void setIMEListener(IMEListener listener) throws RemoteException {
            if (mMEClient != null) {
                mMEClient.setIMEListener(listener);

            } else {
                Log.d(TAG, "ME: setMEListener: mMEClient is null");
            }
        }

        public boolean isMEConnected() throws RemoteException {

            return isMEClientConnected();
        }

    };

    private boolean addToSubscribeQueue(METopicEntity meTopic) {
        boolean result = false;
        if (!hasMETopic(mSubscribeQueue, meTopic) && mSubscribeQueue != null) {
            mSubscribeQueue.add(meTopic);

            result = true;

            try {
                mLock.lock();

                mSubscribeMETopicHandle.notifyToHandle();
            } catch (Exception e) {
                Log.e(TAG, e);

            } finally {
                mLock.unlock();
            }
        }

        return result;
    }

    private void resubscribeKeepAliveMETopic() {
        if (Assert.notEmpty(mKeepAliveMap)) {
            Set<Map.Entry<String, METopicEntity>> entrySet = mKeepAliveMap.entrySet();
            for (Map.Entry<String, METopicEntity> topic : entrySet) {
                if (topic != null) {
                    addToSubscribeQueue(topic.getValue());
                }
            }
        }
    }

    private synchronized boolean hasMETopic(ArrayList<METopicEntity> queue, METopicEntity meTopic) {
        boolean has = false;
        if (Assert.notEmpty(queue) && METopicEntity.check(meTopic)) {
            for (METopicEntity tempTopic : queue) {
                if (meTopic.equals(tempTopic)) {
                    has = true;

                    break;
                }
            }
        }

        return has;
    }

    private synchronized void removeMETopic(ArrayList<METopicEntity> queue, String topicName) {
        METopicEntity meTpioc = getMETopic(queue, topicName);
        if (meTpioc != null) {
            queue.remove(meTpioc);
        }
    }

    private synchronized METopicEntity getMETopic(ArrayList<METopicEntity> queue, String topicName) {
        METopicEntity meTopic = null;
        if (Assert.notEmpty(queue) && Assert.notEmpty(topicName)) {
            for (METopicEntity tempTopic : queue) {
                if (topicName.equals(tempTopic.topic)) {
                    meTopic = tempTopic;

                    break;
                }
            }
        }

        return meTopic;
    }

    private synchronized void writeMEOptions() {
        mMEThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mMEOptions != null) {
                    try {
                        FileUtils.write(MEOptions.obtainMEOptionsFile(), mMEOptions.toJSON(), charset_encoding);
                    } catch (Exception e) {
                        Log.d(TAG, e);
                    }
                }
            }
        });
    }

    private synchronized void cleanMEOptions() {
        mMEThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                MEOptions.obtainMEOptionsFile().delete();
            }
        });

        mMEOptions = null;
    }

    private synchronized String readMEOptions() {

        return FileUtils.read(MEOptions.obtainMEOptionsFilePath(), charset_encoding);
    }

    private void retryConnect() {
        mMERetryConnectState = MESTATE_CONNECT_RETRY;

        ++mRepeatConnectCount;

        long retryInterval = MEOptions.CONNECTION_SHORT_INTERVAL;
        if (mRepeatConnectCount > REPEAT_MAX_CONNECT_COUNT) {
            mRepeatConnectCount = 0;
            Log.v(TAG, "ME: mqtt connected fail by 4 step, repeated next round");

            sendMEStateBroadcast(MESTATE_CONNECT_LOSTED_LONG_TIME, "MQTT connected losted long time");

        } else if (mRepeatConnectCount > REPEAT_LONG_CONNECT_COUNT) {
            retryInterval = MEOptions.KAT_SHORT_RETRY_INTERVAL_KEEP_ALIVE;

            Log.v(TAG, "ME: mqtt connected fail by 3 step, repeated after " + (retryInterval / 60000) + "m, "
                    + mRepeatConnectCount + ", " + retryInterval);

        } else if (mRepeatConnectCount > REPEAT_CONNECT_COUNT) {
            retryInterval = MEOptions.CONNECTION_LONG_RETRY_INTERVAL * (mRepeatConnectCount - REPEAT_CONNECT_COUNT);

            Log.v(TAG, "ME: mqtt connected fail by 2 step, repeated after " + (retryInterval / 60000) + "m, "
                    + mRepeatConnectCount + ", " + retryInterval);
        } else {
            retryInterval = MEOptions.CONNECTION_SHORT_INTERVAL * mRepeatConnectCount;

            Log.v(TAG, "ME: mqtt connected fail by 1 step, repeated after " + (retryInterval / 1000) + "s, "
                    + mRepeatConnectCount + ", " + retryInterval);
        }

        if (mMEThreadHandler != null) {
            mMEThreadHandler.removeCallbacksAndMessages(TOKEN_RETRY_CONNECT);

            mMEThreadHandler.postAtTime(new Runnable() {

                @Override
                public void run() {
                    reconnect(false);
                }

            }, TOKEN_RETRY_CONNECT, SystemClock.uptimeMillis() + retryInterval);
        }
    }

    private class HandleMETopicThread extends Thread {

        /**
         * Condition for waiting takes
         */
        private final Condition mLockCondition;
        private final HandleRunnable mHandleRunnable;

        public HandleMETopicThread(String threadName, HandleRunnable handleRunnable) {
            super(threadName);

            mLockCondition = mLock.newCondition();
            mHandleRunnable = handleRunnable;
        }

        public HandleMETopicThread startHandle() {
            start();

            return this;
        }

        public void notifyToHandle() {
            mLockCondition.signalAll();
        }

        @Override
        public void run() {
            for (; ; ) {
                if (mHandleRunnable != null && mHandleRunnable.judge()) {
                    try {
                        mHandleRunnable.handle();
                    } catch (MqttException e) {
                        Log.e(TAG, e);

                        reconnect(false);

                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }

                } else {
                    try {
                        mLock.lock();

                        mLockCondition.await();
                    } catch (Exception e) {
                        // do nothing
                    } finally {
                        mLock.unlock();
                    }
                }
            }
        }
    }

    private interface HandleRunnable {

        void handle() throws Exception;

        /**
         * 判断条件
         *
         * @return
         */
        boolean judge();
    }

    /**
     * 消息发送线程
     */
    private final HandleRunnable mPublishRunnable = new HandleRunnable() {

        /**
         * 发送消息,如果消息发送发生异常,则不作任务处理,交由连接保证机制处理
         *
         */
        @Override
        public void handle() throws Exception {
            METopicEntity meMessage = mPublishQueue.get(0);
            if (meMessage != null) {
                try {
                    mMEClient.publish(meMessage.topic, meMessage.payload, meMessage.qos);

                    mPublishQueue.remove(0);
                } catch (Exception e) {
                    Log.e(TAG, e);

                    throw new MqttException(MESTATE_PUBLISH_FAIL);
                }
            } else {
                mPublishQueue.remove(0);
            }
        }

        @Override
        public boolean judge() {

            return isMEClientConnected() && Assert.notEmpty(mPublishQueue);
        }
    };

    /**
     * 订阅线程
     */
    private final HandleRunnable mSubscribeRunnable = new HandleRunnable() {

        @Override
        public void handle() throws Exception {
            METopicEntity meTopic = mSubscribeQueue.get(0);
            if (METopicEntity.check(meTopic)) {
                try {
                    mMEClient.subscribe(meTopic.topic, meTopic.qos);

                    if (meTopic.needToKeepAlive) {
                        mKeepAliveMap.put(meTopic.topic, meTopic);

                        if (!Assert.containsKey(mKeepAliveTokenMap, meTopic.topic)) {
                            mKeepAliveTokenMap.put(meTopic.topic, new byte[]{});
                        }
                    }

                    mSubscribeQueue.remove(meTopic);
                } catch (Exception e) {
                    Log.e(TAG, e);

                    throw new MqttException(MESTATE_PUBLISH_FAIL);
                }
            } else {
                mSubscribeQueue.remove(0);
            }
        }

        @Override
        public boolean judge() {

            return isMEClientConnected() && Assert.notEmpty(mSubscribeQueue);
        }
    };
}
