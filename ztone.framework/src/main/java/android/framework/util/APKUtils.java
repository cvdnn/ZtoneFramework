package android.framework.util;

import android.assist.Assert;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.framework.IRuntime;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.log.Log;
import android.net.Uri;
import android.reflect.ClazzLoader;
import android.util.DisplayMetrics;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

/**
 * Created by handy on 17-3-14.
 */

public class APKUtils {
    private static final String TAG = "APKUtils";

    /**
     * 代码安装
     */
    public static void installApk(Context context, String apkPath) {
        if (context != null && Assert.notEmpty(apkPath)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    public static boolean checkAppInstall(String packageName) {
        boolean result = false;

        if (Assert.notEmpty(packageName)) {
            try {
                ApplicationInfo appinfo = LifeCycleUtils.component().app().getPackageManager().getApplicationInfo(packageName, GET_UNINSTALLED_PACKAGES);
                result = appinfo != null && packageName.equals(appinfo.packageName);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }

        return result;
    }

    public static byte[] getAppSignatureToByteArray() {
        Signature sign = getAppSignature(LifeCycleUtils.component().app(), IRuntime.getPackageName());

        return sign != null ? sign.toByteArray() : null;
    }

    public static String getAppSignatureToString() {
        Signature sign = getAppSignature(LifeCycleUtils.component().app(), IRuntime.getPackageName());

        return sign != null ? sign.toCharsString() : "";
    }

    public static Signature getAppSignature(Context context, String packageName) {
        Signature sign = null;
        if (context != null && Assert.notEmpty(packageName)) {
            List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES);
            if (Assert.notEmpty(apps)) {
                for (PackageInfo packageInfo : apps) {
                    if (packageInfo != null && Assert.notEmpty(packageInfo.signatures) && packageName.equals(packageInfo.packageName)) {
                        sign = packageInfo.signatures[0];
                        break;
                    }
                }
            }
        }

        return sign;
    }

    public static X509Certificate parseSignature(byte[] signature) {
        X509Certificate cert = null;
        if (Assert.notEmpty(signature)) {
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

                cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        return cert;

    }

    public static String getAPKSignatures(String apkPath) {
        String sign = null;

        if (Assert.notEmpty(apkPath)) {
            // PackageParser packageParser = new PackageParser(apkPath);
            Object pkgParser = ClazzLoader.newInstance("android.content.pm.PackageParser", apkPath);
            if (pkgParser != null) {
                // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.setToDefaults();

                // PackageParser.Package mPkgInfo =  packageParser.parsePackage(new File(apkPath), apkPath, metrics, 0);
                Object pkgParserPkg = ClazzLoader.invoke(pkgParser, "parsePackage", new File(apkPath), apkPath,
                        metrics, PackageManager.GET_SIGNATURES);
                if (pkgParserPkg != null) {
                    ClazzLoader.invoke(pkgParser, "collectCertificates", pkgParserPkg, PackageManager.GET_SIGNATURES);

                    Signature[] info = ClazzLoader.getFieldValue(pkgParserPkg, "mSignatures");
                    if (Assert.notEmpty(info) && info.length >= 1 && info[0] != null) {
                        Log.d(TAG, "size:" + info.length);
                        Log.d(TAG, info[0].toCharsString());

                        sign = info[0].toCharsString();
                    }
                }
            }

        }

        return sign;
    }
}
