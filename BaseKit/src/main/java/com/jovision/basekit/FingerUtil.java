package com.jovision.basekit;

/**
 * @ProjectName: HoloSens
 * @Package: com.huawei.holosens.utils.finger
 * @ClassName: FingerUtil
 * @Description: java类作用描述 * @CreateDate: 2020-03-26 15:28
 * @Version: 1.0
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;

import androidx.annotation.RequiresApi;

/**
 * 指纹识别工具类
 */
public class FingerUtil {

    private static final String TAG = "FingerUtil";

    private final FingerprintManager fingerprintManager;
    private final KeyguardManager keyguardManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private FingerUtil(Context context) {
        fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    private static volatile FingerUtil singleton = null;

    public static synchronized FingerUtil getInstance(Context context) {
        if (singleton == null) {
            synchronized (FingerUtil.class) {
                if (singleton == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        singleton = new FingerUtil(context);
                    }
                }
            }
        }
        return singleton;
    }


    /**
     * ②检查手机硬件（有没有指纹感应区）
     */


    @SuppressLint("MissingPermission")
    public boolean isHardFinger() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * ③检查手机是否开启锁屏密码
     */

    public boolean isWindowSafe() {
        if (keyguardManager != null && keyguardManager.isKeyguardSecure()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ④检查手机是否已录入指纹
     */

    public boolean isHaveHandler() {
        boolean isHaveHandler = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints()) {
                isHaveHandler = true;
            } else {
                isHaveHandler = false;
            }
        }
        LogUtil.e(TAG,"isHaveHandler="+isHaveHandler+";Build.VERSION.SDK_INT="+Build.VERSION.SDK_INT);
        return isHaveHandler;
    }

    /**
     * 创建指纹验证
     */

    public void authenticate(FingerprintManager.CryptoObject cryptoObject, CancellationSignal cancellationSignal,
                             int flag,
                             FingerprintManager.AuthenticationCallback authenticationCallback, Handler handler) {
        LogUtil.v(TAG,"startAuth---authenticate");
        if (fingerprintManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LogUtil.v(TAG,"startAuth,startAuth---I");
                fingerprintManager.authenticate(cryptoObject, cancellationSignal, flag, authenticationCallback, handler);
            }
        }
    }

    /**
     * 取消指纹验证  . 应该不会用上
     */
    public void cannelFinger(CancellationSignal cancellationSignal) {
        if(null != cancellationSignal){
            cancellationSignal.cancel();
        }
    }

    @TargetApi(23)
    public boolean judgeFingerprintIsCorrect() {
        //判断硬件是否支持指纹识别
        if (!isHardFinger()) {
            return false;
        }
        //判断是否开启锁屏密码
        if (!isWindowSafe()) {
            return false;
        }
        //判断是否有指纹录入
        if (!isHaveHandler()) {
            return false;
        }
        return true;
    }
}