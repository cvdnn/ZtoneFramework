package android.framework.push;

import android.assist.Assert;
import android.assist.Etcetera;
import android.assist.TextLinker;
import android.bus.EventBusUtils;
import android.bus.ThreadMode;
import android.bus.annotation.EventSubscribe;
import android.concurrent.ThreadPool;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.Loople;
import android.framework.context.ServiceManager;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.context.lifecycle.OnLifeCycleListener;
import android.framework.push.annotation.MECode;
import android.framework.push.annotation.MESubscribe;
import android.framework.push.entity.MessageMEEntity;
import android.framework.push.entity.ResultMEEntity;
import android.json.JSONUtils;
import android.log.Log;
import android.os.IBinder;
import android.os.RemoteException;
import android.reflect.ClazzLoader;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static android.framework.push.MEOptions.FORMAT_TOPIC_NAME;
import static android.framework.push.MEOptions.TAG_CODE;
import static android.framework.push.annotation.MECode.MECODE_NONE;

public class MEBridge {
    private static final String TAG = "MEBridge";

    /* ************************************
     * static
     *
     * ************************************
     */
    public static final String ME_CORE_TREAD_POOL = "__me_tread_pool";

    public static final String TAG_ME_EVENT_BUS = "__tag_me_event_bus";

    private static final ReentrantLock mMEBridgeLock = new ReentrantLock(false);
    private static final Condition mLockCondition = mMEBridgeLock.newCondition();

    private static final ArrayMap<String, MEEventMethod> mArrivedMethodMap = new ArrayMap<String, MEEventMethod>();

    private static final HashSet<String> mSubscribedTopicSet = new HashSet<String>();

    private static MEBridge mAppMEBridge;
    private static volatile IMEBinder mAppMEBinder;

    static {
        LifeCycleUtils.adhere().register(new OnLifeCycleListener.Impl() {

            @Override
            public <Cx extends Context> void onStart(@NonNull Cx context) {

            }
        });
    }

    /**
     * priject+cpusn+...
     *
     * @param topics
     * @return
     */
    public static String formatCUPTopicName(String... topics) {
        int size = Assert.notEmpty(topics) ? topics.length + 1 : 1; // 1 --> clientId

        String[] tempTopics = new String[size];
        tempTopics[0] = Etcetera.cpuSerialNumber();

        if (Assert.notEmpty(topics)) {
            System.arraycopy(topics, 0, tempTopics, 1, topics.length);
        }

        return formatProjectTopicName(tempTopics);
    }

    /**
     * priject+cleintid+...
     *
     * @param topics
     * @return
     */
    public static String formatClientTopicName(String... topics) {
        int size = Assert.notEmpty(topics) ? topics.length + 1 : 1; // 1 --> clientId

        String[] tempTopics = new String[size];
        tempTopics[0] = IRuntime.vriables().getClientId();

        if (Assert.notEmpty(topics)) {
            System.arraycopy(topics, 0, tempTopics, 1, topics.length);
        }

        return formatProjectTopicName(tempTopics);
    }

    /**
     * priject+...
     *
     * @param topics
     * @return
     */
    public static String formatProjectTopicName(String... topics) {
        int size = Assert.notEmpty(topics) ? topics.length + 1 : 1; // 1 --> projectName

        String[] tempTopics = new String[size];
        tempTopics[0] = MEOptions.obtain().projectName;

        if (Assert.notEmpty(topics)) {
            System.arraycopy(topics, 0, tempTopics, 1, topics.length);
        }

        return linkTopicName(tempTopics);
    }

    /**
     * ...
     */
    public static String linkTopicName(String... topics) {
        TextLinker textLinker = TextLinker.create(MEOptions.SEPARATOR_TOPIC_NAME);

        if (Assert.notEmpty(topics)) {
            for (String topic : topics) {
                textLinker.append(topic);
            }
        }

        return textLinker.toString();
    }

    public static void subscribeMEEvent(OnMEArrivedSubscriber o) {
        EventBusUtils.register(TAG_ME_EVENT_BUS, o);
    }

    public static <O> void unsubscribeMEEvent(OnMEArrivedSubscriber o) {
        EventBusUtils.unregister(TAG_ME_EVENT_BUS, o);
    }

    public static <O extends ResultMEEntity> void postMEEvent(O o) {
        EventBusUtils.post(TAG_ME_EVENT_BUS, o);
    }

    public static MEBridge createAppMEBridge() {
        if (mAppMEBridge == null) {
            synchronized (MEBridge.class) {
                mAppMEBridge = new MEBridge(LifeCycleUtils.component().app());

                bindAPPMEService();
            }
        }

        return mAppMEBridge;
    }

    public static void destroyAppMEBridge() {
        mAppMEBinder = null;

        if (mAppMEBridge != null) {
            mAppMEBridge.unbindMEService();
            mAppMEBridge = null;
        }
    }

    public static boolean isMEClientConnected() {

        return mAppMEBridge != null && mAppMEBridge.isMEConnected();
    }

    /**
     * 同步等待获取推送消息
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <M extends ResultMEEntity> void pullTopicMessage(final String publishTopic, String publishMessage,
                                                                   final String subscribeTopic, final Class<M> meClass, int timeout) {
        if (Assert.notEmpty(publishTopic) && Assert.notEmpty(subscribeTopic) && Assert.notEmpty(publishMessage)
                && meClass != null && timeout > 0) {

            // 超時定時器
            final Timer timeoutTimer = new Timer(true);

            final boolean isSubscribedTopic = MEBridge.isSubscribedTopic(subscribeTopic);

            final OnMEArrivedSubscriber meArrivedSubscriber = new OnMEArrivedSubscriber() {

                @EventSubscribe(tmode = ThreadMode.Async)
                public void onPullArrived(M entity) {
                    if (!isSubscribedTopic) {
                        unsubscribe(subscribeTopic);
                    }

                    unsubscribeMEEvent(this);

                    if (timeoutTimer != null) {
                        timeoutTimer.cancel();
                    }
                }
            };

            subscribe(subscribeTopic, MEOptions.QOS_0, false, meArrivedSubscriber, true, meClass);

            publish(publishTopic, publishMessage, MEOptions.QOS_0);

            timeoutTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (!isSubscribedTopic) {
                        unsubscribe(subscribeTopic);
                    }

                    unsubscribeMEEvent(meArrivedSubscriber);

                    try {
                        M temp = ClazzLoader.newInstance(meClass);
                        temp.code = ResultMEEntity.RESULT_TIMEOUT;
                    } catch (Exception e) {
                        Log.v(TAG, e);
                    }

                }

            }, timeout);
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends ResultMEEntity> M waittingForTopicMessage(final String publishTopic,
                                                                       String publishMessage, final String subscribeTopic, final Class<M> meClass, int timeout) {
        M m = null;

        if (Assert.notEmpty(publishTopic) && Assert.notEmpty(subscribeTopic) && Assert.notEmpty(publishMessage) && meClass != null && timeout > 0) {
            // 線程執行令牌
            final ArrayBlockingQueue<M> token = new ArrayBlockingQueue<M>(1);

            // 超時定時器
            final Timer timeoutTimer = new Timer(true);

            final boolean isSubscribedTopic = MEBridge.isSubscribedTopic(subscribeTopic);

            final OnMEArrivedSubscriber meArrivedSubscriber = new OnMEArrivedSubscriber() {

                @EventSubscribe(tmode = ThreadMode.Async)
                public void onWaittingArrived(M entity) {
                    if (!isSubscribedTopic) {
                        unsubscribe(subscribeTopic);
                    }

                    unsubscribeMEEvent(this);

                    if (timeoutTimer != null) {
                        timeoutTimer.cancel();
                    }

                    if (Assert.isInstanceOf(meClass, entity)) {
                        if (token != null) {
                            try {
                                token.put(entity);
                            } catch (Exception e) {
                                Log.v(TAG, e);
                            }
                        }
                    }
                }
            };

            subscribe(subscribeTopic, MEOptions.QOS_0, false, meArrivedSubscriber, true, meClass);

            publish(publishTopic, publishMessage, MEOptions.QOS_0);

            timeoutTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (!isSubscribedTopic) {
                        unsubscribe(subscribeTopic);
                    }

                    unsubscribeMEEvent(meArrivedSubscriber);

                    if (token != null) {
                        try {
                            M temp = ClazzLoader.newInstance(meClass);
                            temp.code = ResultMEEntity.RESULT_TIMEOUT;
                            token.put(temp);
                        } catch (Exception e) {
                            Log.v(TAG, e);
                        }
                    }
                }

            }, timeout);

            // 阻塞等待回复或超时
            try {
                m = token.take();
            } catch (Exception e) {
                Log.v(TAG, e);
            }
        }

        return m;
    }

    public static void subscribe(final String topic, final int qos, final OnMEArrivedSubscriber subscriber,
                                 Class<? extends ResultMEEntity>... meEntitys) {
        subscribe(topic, qos, false, subscriber, false, meEntitys);
    }

    public static void subscribe(final String topic, final int qos, final boolean keepalive,
                                 final OnMEArrivedSubscriber subscriber, Class<? extends ResultMEEntity>... meEntitys) {
        subscribe(topic, qos, keepalive, subscriber, false, meEntitys);
    }

    public static void subscribe(final String topic, final int qos, final boolean keepalive,
                                 final OnMEArrivedSubscriber subscriber, boolean subscribeEvent,
                                 Class<? extends ResultMEEntity>... meEntitys) {
        if (Assert.notEmpty(topic)) {
            // 处理可接收的MECode,优先级高于MEEntity
            if (Assert.notEmpty(meEntitys)) {
                for (Class<? extends ResultMEEntity> meEntityClazz : meEntitys) {
                    if (meEntityClazz != null) {
                        String formatTopicName = getMEEntityFormatTopicName(meEntityClazz, topic);
                        putArrivedMethodMap(formatTopicName,
                                MEEventMethod.create(topic, qos, subscriber, null, meEntityClazz, formatTopicName));
                    }
                }
            }

            subscribeToMEBinder(topic, qos, keepalive);

            if (subscribeEvent) {
                subscribeMEEvent(subscriber);
            }
        }
    }

    public static <O> void subscribe(final O o) {
        if (o != null) {
            Class<?> clazz = o.getClass();
            Method[] methods = clazz.getDeclaredMethods();
            if (Assert.notEmpty(methods)) {
                for (Method meArrivedMethod : methods) {
                    if (meArrivedMethod != null) {
                        MESubscribe meSubscribe = meArrivedMethod.getAnnotation(MESubscribe.class);
                        if (meSubscribe != null) {
                            Class<?>[] paras = meArrivedMethod.getParameterTypes();
                            if (paras != null && paras.length == 1) {
                                final String topic = meSubscribe.topic();
                                final int qos = meSubscribe.qos();

                                Class<?> paraClazz = paras[0];

                                String formatTopicName = getMEEntityFormatTopicName(paraClazz, topic);
                                putArrivedMethodMap(formatTopicName, MEEventMethod.create(topic, qos, o,
                                        meArrivedMethod, paraClazz, formatTopicName));

                                subscribeToMEBinder(topic, qos, meSubscribe.keepalive());
                            }
                        }
                    }
                }
            }
        }
    }

    public synchronized static <O> void unsubscribe(String topic, O o) {
        if (Assert.notEmpty(topic) && o != null && Assert.notEmpty(mArrivedMethodMap)) {
            if (Assert.notEmpty(topic) && Assert.notEmpty(mArrivedMethodMap)) {
                int subscribCount = 0;
                HashSet<String> removedTopicArray = new HashSet<String>();

                Set<Map.Entry<String, MEEventMethod>> entrySet = mArrivedMethodMap.entrySet();
                for (Map.Entry<String, MEEventMethod> entry : entrySet) {
                    if (entry != null) {
                        MEEventMethod meEventMethod = entry.getValue();
                        if (meEventMethod != null && topic.equals(meEventMethod.topic)) {
                            // 先移除ME事件订阅
                            if (meEventMethod.registerObject == o) {
                                removedTopicArray.add(meEventMethod.formatTopicName);
                            } else {
                                // 统计不属于object的订阅,以便判断是否要取消topic订阅
                                subscribCount++;
                            }
                        }
                    }
                }

                if (Assert.notEmpty(removedTopicArray)) {
                    mArrivedMethodMap.removeAll(removedTopicArray);
                }

                // ==0时说明没有其他地方订阅可以取消订阅该Topic, 否则只要移除ME事件订阅
                if (subscribCount == 0) {
                    unsubscribeToMEBinder(topic);
                }
            }
        }
    }

    public synchronized static <O> void unsubscribe(O o) {
        if (o != null && Assert.notEmpty(mArrivedMethodMap)) {
            HashSet<String> removedTopicArray = new HashSet<String>();

            Set<Map.Entry<String, MEEventMethod>> entrySet = mArrivedMethodMap.entrySet();
            for (Map.Entry<String, MEEventMethod> entry : entrySet) {
                if (entry != null) {
                    String topic = entry.getKey();
                    if (Assert.notEmpty(topic)) {
                        MEEventMethod meTopicMethod = entry.getValue();
                        if (meTopicMethod != null && meTopicMethod.registerObject == o) {
                            removedTopicArray.add(meTopicMethod.formatTopicName);

                            unsubscribeToMEBinder(topic);
                        }
                    }
                }
            }

            if (Assert.notEmpty(removedTopicArray)) {
                mArrivedMethodMap.removeAll(removedTopicArray);
            }
        }
    }

    public synchronized static void unsubscribe(String topic) {
        if (Assert.notEmpty(topic) && Assert.notEmpty(mArrivedMethodMap)) {
            unsubscribeToMEBinder(topic);

            HashSet<String> removedTopicArray = new HashSet<String>();

            Set<Map.Entry<String, MEEventMethod>> entrySet = mArrivedMethodMap.entrySet();
            for (Map.Entry<String, MEEventMethod> entry : entrySet) {
                if (entry != null) {
                    MEEventMethod meEventMethod = entry.getValue();
                    if (meEventMethod != null) {
                        if (Assert.notEmpty(meEventMethod.formatTopicName)
                                && meEventMethod.formatTopicName.equals(getMEEntityFormatTopicName(
                                meEventMethod.meEntityClazz, topic))) {
                            removedTopicArray.add(meEventMethod.formatTopicName);
                        }
                    }
                }
            }

            if (Assert.notEmpty(removedTopicArray)) {
                mArrivedMethodMap.removeAll(removedTopicArray);
            }
        }
    }

    public static void publish(final String topic, final String text, final int qos) {
        ThreadPool.Impl.execute(ME_CORE_TREAD_POOL, new Runnable() {

            @Override
            public void run() {
                try {
                    awaitLockCondition();

                    if (mAppMEBinder != null) {
                        mAppMEBinder.publish(topic, text, qos);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        });
    }

    public static boolean isSubscribedTopic(String topicName) {

        return Assert.notEmpty(mSubscribedTopicSet) && Assert.notEmpty(topicName)
                && mSubscribedTopicSet.contains(topicName);
    }

    public static boolean isMEServiceStart() {

        return ServiceManager.getInstance().isServiceStarted(MEService.class);
    }

    /**
     * @param context to start the service with
     * @return void
     */
    public static void startMEService(final Context context) {
        startMEService(context, null, false);
    }

    /**
     * Start MQTT Client
     *
     * @param context to start the service with
     * @return void
     */
    public static void startMEService(Context context, MEOptions meOptions) {
        startMEService(context, meOptions, false);
    }

    /**
     * Start MQTT Client
     *
     * @param context to start the service with
     * @return void
     */
    public static void startMEService(final Context context, final MEOptions meOptions, final boolean deamonFlag) {
        Log.printCalledStackTrace("ME");

        if (context != null) {
            Loople.post(new Runnable() {

                @Override
                public void run() {
                    if (context != null) {
                        Intent intent = newMEActionIntent(context, C.flag.me_start_sevice);

                        if (meOptions != null) {
                            intent.putExtra(C.tag.me_options, meOptions);
                        }

                        intent.putExtra(C.tag.deamon_flag, deamonFlag ? C.flag.app_daemon : C.flag.none);

                        context.startService(intent);
                    }
                }
            });
        }
    }

    /**
     * Stop MQTT Client
     *
     * @param context to start the service with
     * @return void
     */
    public static void stopMEService(Context context) {
        startMEAction(context, C.flag.me_stop_foreground);
        startMEAction(context, C.flag.me_stop_sevice);

    }

    /**
     * Start MQTT Client
     *
     * @param context to start the service with
     * @return void
     */
    public static void reconnectMEService(Context context) {
        if (context != null) {
            Intent intent = newMEActionIntent(context, C.flag.me_sevice_reconnection);
            intent.putExtra(C.tag.me_options, MEOptions.obtain());

            context.startService(intent);
        }
    }

    public static void startMEForeground(Context context) {
        startMEAction(context, C.flag.me_start_foreground);

    }

    public static void stopMEForeground(Context context) {
        startMEAction(context, C.flag.me_stop_foreground);

    }

    /**
     * Stop MQTT Client
     *
     * @param context to start the service with
     * @return void
     */
    public static void startMEAction(Context context, int meFlag) {
        if (context != null) {
            context.startService(newMEActionIntent(context, meFlag));
        }
    }

    public static Intent newMEActionIntent(Context context, int meFlag) {
        Intent intent = new Intent(MEAction.messageExchange());
        intent.setPackage(IRuntime.getPackageName());

        if (context != null) {
            intent.putExtra(C.tag.me_clazz, context.getClass().getName());
        }

        intent.putExtra(C.tag.me_flag, meFlag);

        return intent;
    }

    private static void bindAPPMEService() {
        Loople.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mAppMEBridge != null) {
                    mAppMEBridge.bindMEService();
                }
            }
        }, 200);
    }

    private static String getMEEntityFormatTopicName(Class<?> meEntityClazz, String topic) {
        String formatTopicName = topic;

        if (meEntityClazz != null) {
            MECode meCode = meEntityClazz.getAnnotation(MECode.class);
            if (meCode != null) {
                formatTopicName = formatTopicNameByMECode(topic, meCode.code());
            }
        }

        return formatTopicName;
    }

    private static String formatTopicNameByMECode(String topicName, int code) {

        return MECODE_NONE != code ? String.format(FORMAT_TOPIC_NAME, topicName, code) : topicName;
    }

    /**
     * @param topic
     * @param meMethod
     */
    private static void putArrivedMethodMap(String topic, MEEventMethod meMethod) {
        if (mArrivedMethodMap != null && Assert.notEmpty(topic) && meMethod != null) {
            mArrivedMethodMap.put(topic, meMethod);
        }
    }

    private static MEEventMethod getArrivedMETopicMethod(String topic) {

        return Assert.notEmpty(mArrivedMethodMap) && Assert.notEmpty(topic) ? mArrivedMethodMap.get(topic) : null;
    }

    private static void subscribeToMEBinder(final String topic, final int qos, final boolean needKeepalive) {
        if (Assert.notEmpty(topic)) {
            if (mSubscribedTopicSet != null) {
                mSubscribedTopicSet.add(topic);
            }

            ThreadPool.Impl.execute(ME_CORE_TREAD_POOL, new Runnable() {

                @Override
                public void run() {
                    awaitLockCondition();

                    if (mAppMEBinder != null && Assert.notEmpty(topic)) {
                        try {
                            mAppMEBinder.subscribe(topic, qos, needKeepalive);
                        } catch (Exception e) {
                            Log.d(TAG, e);
                        }
                    }
                }
            });
        }
    }

    private static void unsubscribeToMEBinder(final String topic) {
        if (Assert.notEmpty(topic)) {
            if (mSubscribedTopicSet != null) {
                mSubscribedTopicSet.remove(topic);
            }

            ThreadPool.Impl.execute(ME_CORE_TREAD_POOL, new Runnable() {

                @Override
                public void run() {
                    awaitLockCondition();

                    if (mAppMEBinder != null && Assert.notEmpty(topic)) {
                        try {
                            mAppMEBinder.unsubscribe(topic);
                        } catch (Exception e) {
                            Log.d(TAG, e);
                        }
                    }
                }
            });
        }
    }

    private static void awaitLockCondition() {
        while ((mAppMEBinder == null || mAppMEBinder == null) && mMEBridgeLock != null && mLockCondition != null) {
            try {
                mMEBridgeLock.lock();

                mLockCondition.await();
            } catch (Exception e) {
                Log.e(TAG, e);
            } finally {
                mMEBridgeLock.unlock();
            }
        }
    }

    public static void signalAllLockCondition() {
        if (mMEBridgeLock != null && mLockCondition != null) {
            try {
                mMEBridgeLock.lock();

                mLockCondition.signalAll();
            } catch (Exception e) {
                Log.e(TAG, e);

            } finally {
                mMEBridgeLock.unlock();
            }
        }
    }

	/* *************************************** */

    private Context mContext;

    private boolean mIsMEBinded;


    private MEBridge(Context context) {
        mContext = context;
    }

    public synchronized final boolean bindMEService() {
        unbindMEService();

        // bind
        if (mContext != null && mMEServiceConnection != null) {
            try {
                Intent serviceIntent = new Intent(MEAction.messageExchange());
                serviceIntent.setPackage(IRuntime.getPackageName());

                mIsMEBinded = mContext.bindService(serviceIntent, mMEServiceConnection, Context.BIND_AUTO_CREATE);

                Log.v(TAG, "ME: [bindMEService]: %s", mContext.getClass().getSimpleName());
            } catch (Throwable t) {
                Log.e(TAG, t);
            }
        }

        return mIsMEBinded;
    }

    public synchronized final void unbindMEService() {
        if (mIsMEBinded && mContext != null && mMEServiceConnection != null) {
            try {
                mContext.unbindService(mMEServiceConnection);

                Log.v(TAG, "ME: [unbindMEService]: %s", mContext.getClass().getSimpleName());
            } catch (Throwable t) {
                Log.v(TAG, t);
            }
        }

        mIsMEBinded = false;
        mAppMEBinder = null;
    }

    public synchronized boolean isMEConnected() {
        boolean result = false;

        try {
            result = mMEServiceConnection != null && mIsMEBinded && mAppMEBinder != null && mAppMEBinder.isMEConnected();
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        return result;
    }

    private final ServiceConnection mMEServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAppMEBinder = IMEBinder.Stub.asInterface(service);
            if (mAppMEBinder != null) {
                try {
                    mAppMEBinder.setIMEListener(mIMEListener);
                } catch (Exception e) {
                    Log.e(TAG, e);
                }

                Log.v(TAG, "ME: bind to MEService success");

                signalAllLockCondition();
            } else {
                Log.e(TAG, "ME: [AppMEBinder]: %s", mAppMEBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "ME: unbind from MEService: mIsMEBinded: %s", mIsMEBinded);

            // ZZZ 这里延迟 start me
//			if (mIsMEBinded) {
//				startMEService(mContext);
//				bindAPPMEService();
//			}
        }
    };

    private final IMEListener mIMEListener = new IMEListener.Stub() {

        @Override
        public void onMessageArrived(String topic, String text) throws RemoteException {
            if (Assert.notEmpty(topic) && Assert.notEmpty(text)) {
                JSONObject jsonText = JSONUtils.from(text);
                if (jsonText != null) {
                    int code = JSONUtils.optInt(jsonText, TAG_CODE, MECODE_NONE);

                    MEEventMethod meeMethod = getArrivedMETopicMethod(formatTopicNameByMECode(topic, code));
                    if (meeMethod != null && meeMethod.meEntityClazz != null) {
                        MessageMEEntity meEntity = createMEEntity(topic, (Class<? extends MessageMEEntity>) meeMethod.meEntityClazz, jsonText);
                        postMessageArrived(meeMethod, meEntity);
                    } else {
                        if (meeMethod == null) {
                            Log.e(TAG, "ME: MEEventMethod: meEventMethod: null");
                        } else {
                            Log.e(TAG, "ME: MEEventMethod: meEventMethod.meEntityClazz: %s", (meeMethod.meEntityClazz != null));
                        }
                    }
                }
            } else {
                Log.e(TAG, "ME: onMessageArrived error");
            }
        }

        private void postMessageArrived(final MEEventMethod meEventMethod, final MessageMEEntity meEntity) {
            if (meEntity != null && MEEventMethod.check(meEventMethod)) {
                Loople.post(new Runnable() {

                    @Override
                    public void run() {
                        if (MEEventMethod.check(meEventMethod)) {
                            if (meEventMethod.invokeMethod == null && meEventMethod.registerObject instanceof OnMEArrivedSubscriber) {
                                // 当OnMEArrivedListener收到ME消息后回调监听,返回false时会触发EventBus事件.
                                if (!((OnMEArrivedSubscriber) meEventMethod.registerObject).onArrived(meEntity)) {
                                    // 过listener后EventBus处理
                                    postMEEvent(meEntity);
                                }
                            } else {
                                try {
                                    meEventMethod.invokeMethod.invoke(meEventMethod.registerObject, meEntity);
                                } catch (Exception e) {
                                    Log.e(TAG, e);
                                }
                            }
                        } else {
                            Log.e(TAG, "ME: MEEventMethod: error");
                        }
                    }
                });
            } else {
                Log.e(TAG, "ME: [postMessageArrived]: error cause of: meEntity: " + (meEntity != null)
                        + ", MEEventMethod: " + MEEventMethod.check(meEventMethod));
            }
        }

        private MessageMEEntity createMEEntity(String topic, Class<? extends MessageMEEntity> clazz, JSONObject jsonText) {
            MessageMEEntity entity = null;

            if (Assert.notEmpty(topic) && clazz != null && jsonText != null) {
                entity = ClazzLoader.newInstance(clazz);
                if (entity != null) {
                    entity.topic = topic;

                    try {
                        entity.parse(jsonText);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                }
            }

            return entity;
        }
    };

    private static class MEEventMethod {
        public String topic;
        public int qos;

        /**
         * 注册的类实例,有可能时OnMEArrivedListener
         */
        public Object registerObject;

        /**
         * 通过MESubscribe订阅的可直接处理的方法
         */
        public Method invokeMethod;

        public Class<?> meEntityClazz;
        public String formatTopicName;

        public static MEEventMethod create(String topic, int qos, Object obj, Method invokeMethod, //
                                           Class<?> clazz, String formatTopic) {
            MEEventMethod meTopicMethod = new MEEventMethod();

            meTopicMethod.topic = topic;
            meTopicMethod.qos = qos;

            meTopicMethod.registerObject = obj;

            meTopicMethod.invokeMethod = invokeMethod;
            meTopicMethod.meEntityClazz = clazz;
            meTopicMethod.formatTopicName = formatTopic;

            return meTopicMethod;
        }

        public static boolean check(MEEventMethod method) {

            return method != null && method.meEntityClazz != null && method.registerObject != null;
        }
    }
}
