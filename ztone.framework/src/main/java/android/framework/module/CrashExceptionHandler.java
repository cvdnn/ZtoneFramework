/**
 * 
 */
package android.framework.module;

import android.assist.Assert;
import android.concurrent.ThreadUtils;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.framework.C;
import android.framework.builder.FilePathBuilder;
import android.io.FileUtils;
import android.os.Build;
import android.os.Environment;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static android.framework.C.value.charset_encoding;

/**
 * @author Linzh
 * 
 */
public class CrashExceptionHandler implements UncaughtExceptionHandler {
	public static final String TAG = "CrashHandler";

	private static final long WEEK_TIME = 2 * 24 * 3600 * 1000;

	// 系统默认的UncaughtException处理类
	private UncaughtExceptionHandler mDefaultHandler;

	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private ArrayMap<String, String> infos = new ArrayMap<String, String>();

	// 用于格mNewDateFormatter文件名的一部分
	private final DateFormat mNewDateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
	private final DateFormat mLastDateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;

		new File(FilePath.crash().toFilePath()).mkdirs();

		cleanOldLogFile();

		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (ex != null) {
			Log.e(TAG, ex.getMessage(), ex);
		}

		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			ThreadUtils.sleepThread(3000);

			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return 如果处理了该异常信息返回true;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		// false同时允许系统默认的异常处理器来处理
		boolean result = false;
		if (ex != null) {
			// 收集设备参数信息
			collectDeviceInfo(mContext);
			// 保存日志文件
			saveCrashInfo2File(ex);
		}

		return result;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		Set<Map.Entry<String, String>> entrySet = infos.entrySet();
		for (Map.Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);

		try {
			String fileName = mNewDateFormatter.format(new Date()) + ".log";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				FileUtils.write(
						new File(FilePathBuilder.create().append(C.file.path_log).append(C.file.path_crash)
								.append(fileName).toFilePath()), sb.toString(), charset_encoding);
			}

			return fileName;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return null;
	}

	private void cleanOldLogFile() {
		ThreadUtils.start(new Runnable() {
			private final FilePathBuilder mFilePathBuilder = FilePathBuilder.create().append(C.file.path_log);
			private final String mLogPath = mFilePathBuilder.toFilePath();
			private final String mCarshLogPath = mFilePathBuilder.append(C.file.path_crash).toFilePath();

			@Override
			public void run() {
				moveOldCrashLog();

				cleanTimeoutLog();
			}

			private void moveOldCrashLog() {
				File[] fileList = new File(mLogPath).listFiles(mMoveOldLogFileFilter);
				if (Assert.notEmpty(fileList)) {
					for (File file : fileList) {
						if (Assert.exists(file)) {
							FileUtils.copy(file, //
									new File(FilePathBuilder.create(mCarshLogPath, true).append(file.getName())
											.toFilePath()));

							file.delete();
						}
					}
				}
			}

			private void cleanTimeoutLog() {
				File[] fileList = new File(mCarshLogPath).listFiles(mCleanTimeoutLogFileFilter);
				if (Assert.notEmpty(fileList)) {
					for (File file : fileList) {
						if (Assert.exists(file)) {
							file.delete();
						}
					}
				}
			}

			private FileFilter mMoveOldLogFileFilter = new FileFilter() {

				@Override
				public boolean accept(File file) {

					return Assert.exists(file) && file.isFile() && file.getName().startsWith(C.file.path_crash);
				}
			};

			private FileFilter mCleanTimeoutLogFileFilter = new FileFilter() {
				private final long mNowTime = System.currentTimeMillis();

				@Override
				public boolean accept(File file) {
					boolean result = false;
					if (Assert.exists(file) && file.isFile()) {
						String fileName = file.getName();
						int index = fileName.lastIndexOf('.');
						if (index >= 0) {
							fileName = fileName.substring(0, index);
							if (Assert.notEmpty(fileName)) {
								long time = file.lastModified();

								DateFormat desDateFormat = null;
								if (fileName.startsWith(C.file.path_crash)) {
									desDateFormat = mLastDateFormatter;

									int lastIndex = fileName.lastIndexOf('-');
									fileName = fileName.substring(C.file.path_crash.length() + 1, //
											lastIndex >= 0 ? lastIndex : fileName.length());
								} else {
									desDateFormat = mNewDateFormatter;
								}

								if (desDateFormat != null && Assert.notEmpty(fileName)) {
									try {
										Date date = desDateFormat.parse(fileName);
										if (date != null) {
											time = date.getTime();
										}
									} catch (Exception e) {
										Log.e(TAG, "log file name: " + fileName);
										Log.v(TAG, e.getMessage(), e);
									}
								}

								result = (mNowTime - time >= WEEK_TIME);
							}
						}
					}

					return result;
				}
			};

		}, "CRASH_EXCEPTION_THREAD");
	}
}
