package android.framework.module;

import android.assist.Assert;
import android.concurrent.ThreadPool;
import android.concurrent.ThreadUtils;
import android.framework.C;
import android.framework.ResultMessage;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class Wizard extends Handler implements Callable<ResultMessage> {
    private static final String TAG = "Wizard";

    private static final int STEP_EMPTY = -1;

    private static final int STEP_FINISH = -9100;
    private static final int STEP_ERROR = -9200;
    private static final int STEP_PROGRESS = -9300;

    private OnWizawrdListener mWizawrdListener;
    private OnWizardProgressListener mWizardProgressListener;

    private final ArrayList<WizardTask> mWizardTasks;

    /**
     * 最小启动实现，默认是wizard注册的WizardTask的执行时间
     */
    private int mWizardMinTime;

    private int mStepCount;
    private int mSkipStep = STEP_EMPTY;
    private int mToken;

    private long mStartTime;

    public Wizard() {
        super();

        mWizardTasks = new ArrayList<WizardTask>();
    }

    private void onFinish() {
        if (mWizawrdListener != null) {
            mWizawrdListener.onFinish();
        }
    }

    private void onError(ResultMessage result) {
        if (mWizawrdListener != null) {
            mWizawrdListener.onInterrupt(result);
        }
    }

    private void onProgressUpdate(int progress) {
        if (mWizardProgressListener != null) {
            mWizardProgressListener.onProgressUpdate(progress > 100 ? 100 : (progress < 0 ? 0 : progress));
        }
    }

    public void setOnWizawrdListener(OnWizawrdListener wizawrdListener) {
        mWizawrdListener = wizawrdListener;
    }

    public void setOnWizardProgressListener(OnWizardProgressListener wizardProgressListener) {
        mWizardProgressListener = wizardProgressListener;
    }

    /**
     * 设置整个过程最短时间
     */
    public void setWizardMinTime(int wmTime) {
        mWizardMinTime = wmTime;
    }

    public final void registerWizardTask(WizardTask... tasks) {
        if (mWizardTasks != null && Assert.notEmpty(tasks)) {
            mStepCount = tasks.length;

            mWizardTasks.addAll(Arrays.asList(tasks));
        }
    }

    public final void start() {
        publishProgress(0);

        mToken = 0;
        ThreadPool.Impl.submit(this);
    }

    public final void end() {
        mToken = mStepCount;

        publishProgress(100);
    }

    public final void next() {
        mToken++;
    }

    public final void follow(boolean toNextStep) {
        if (toNextStep) {
            mToken++;
        }

        ThreadPool.Impl.submit(this);
    }

    /**
     * 跳转，跳转的步骤是在当前步骤执行完之后才有效，此前都可以修改
     *
     * @param token
     */
    public final void skipTo(int token) {

        mSkipStep = token;
    }

    /**
     * 判断是否还有其他步骤，判断之前会先执行skipTo的跳转步骤
     *
     * @return
     */
    public final boolean hasNext() {
        if (mSkipStep != STEP_EMPTY) {
            mToken = mSkipStep;
            mSkipStep = STEP_EMPTY;
        }

        return mToken < mStepCount;
    }

    public final boolean isRunning() {

        return mToken > STEP_EMPTY && mToken < mStepCount;
    }

    protected final boolean isFinish(ResultMessage result) {
        return mToken == mStepCount || result != null && result.result == C.flag.result_finish;
    }

    public final int getToken() {

        return mToken;
    }

    @Override
    public final ResultMessage call() {
        mStartTime = SystemClock.uptimeMillis();

        ResultMessage result = null;

        if (Assert.notEmpty(mWizardTasks)) {
            while (hasNext() && checkResult(result)) {
                WizardTask wTask = mWizardTasks.get(mToken);
                if (wTask != null) {
                    try {
                        result = wTask.onWizard(mToken);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }

                // 如果没有返回值没问题执行下一步, 有中断，需要保留当前步骤
                if (checkResult(result)) {
                    next();
                }
            }
        }

        if (isFinish(result)) {
            end();

            checkWizardTime();
            Message.obtain(this, STEP_FINISH).sendToTarget();
        } else {
            Message.obtain(this, STEP_ERROR, result).sendToTarget();
        }

        return null;
    }

    @Override
    public final void handleMessage(Message msg) {
        switch (msg.what) {
            case STEP_FINISH: {
                onFinish();
                break;
            }
            case STEP_ERROR: {
                ResultMessage result = null;
                if (msg.obj != null && msg.obj instanceof ResultMessage) {
                    result = (ResultMessage) msg.obj;
                }

                onError(result);
            }
            case STEP_PROGRESS: {
                onProgressUpdate(msg.arg1);
            }
        }
    }

    public final void publishProgress(int progress) {
        Message.obtain(this, STEP_PROGRESS, progress, 0).sendToTarget();
    }

    private boolean checkResult(ResultMessage result) {
        return result == null || result.result == C.flag.result_none;
    }

    private void checkWizardTime() {
        long stepTime = SystemClock.uptimeMillis() - mStartTime;
        if (stepTime < mWizardMinTime) {
            ThreadUtils.sleepThread(mWizardMinTime - stepTime);
        }
    }

    public interface WizardTask {

        ResultMessage onWizard(int step) throws Exception;
    }

    public interface OnWizawrdListener {
        void onFinish();

        void onInterrupt(ResultMessage result);
    }

    /**
     * 进度, 0~100;
     *
     * @author Handy
     */
    public interface OnWizardProgressListener {
        void onProgressUpdate(int progress);
    }
}
