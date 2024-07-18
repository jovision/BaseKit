package com.jovision.basekit;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by ZHP on 2019/1/3/0003.
 */

public class RomUtils {

    private static String TAG = "RomUtils";

    class AvailableRomType {
        public static final int MIUI = 1;
        public static final int FLYME = 2;
        public static final int ANDROID_NATIVE = 3;
        public static final int NA = 4;
    }

    public static int getLightStatusBarAvailableRomType() {
        //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错
        if (isMiUIV7OrAbove()) {
            return AvailableRomType.ANDROID_NATIVE;
        }

        if (isMiUIV6OrAbove()) {
            return AvailableRomType.MIUI;
        }

        if (isFlymeV4OrAbove()) {
            return AvailableRomType.FLYME;
        }

        if (isAndroidMOrAbove()) {
            return AvailableRomType.ANDROID_NATIVE;
        }

        return AvailableRomType.NA;
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    private static boolean isFlymeV4OrAbove() {
        String displayId = Build.DISPLAY;
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            String[] displayIdArray = displayId.split(" ");
            for (String temp : displayIdArray) {
                //版本号4以上，形如4.x.
                if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*")) {
                    return true;
                }
            }
        }
        return false;
    }

    //Android Api 23以上
    private static boolean isAndroidMOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    private static boolean isMiUIV6OrAbove() {
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            String uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null);
            if (uiCode != null) {
                int code = Integer.parseInt(uiCode);
                return code >= 4;
            } else {
                return false;
            }

        } catch (final Exception e) {
            return false;
        }

    }

    static boolean isMiUIV7OrAbove() {
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            String uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null);
            if (uiCode != null) {
                int code = Integer.parseInt(uiCode);
                return code >= 5;
            } else {
                return false;
            }

        } catch (final Exception e) {
            return false;
        }

    }


    /**************************Android判断手机ROM: 其他方案. start**************************/
//    https://it.cha138.com/android/show-77242.html
    public static final String ROM_MIUI = "MIUI";//小米
    public static final String ROM_EMUI = "EMUI";//华为
    public static final String ROM_OPPO = "OPPO";//oppo
    public static final String ROM_VIVO = "VIVO";//vivo


    public static final String ROM_REALME = "REALME";//真我也属于oppo
    public static final String ROM_FLYME = "FLYME";//魅族
    public static final String ROM_SMARTISAN = "SMARTISAN";//
    public static final String ROM_QIKU = "QIKU";// 360

    // 三星
    public static final String ROM_SAMSUNG = "SAMSUNG";

    public static final String ROM_HONOR = "HONOR";// 荣耀
    public static final String ROM_NOVA = "nova";// 华为 NOVA
    public static final String ROM_OnePlus = "OnePlus";// 一加


    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";//小米
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";//华为
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";//oppo
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";//vivo
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";//

    private static final String KEY_VERSION_SUMSUNG = "samsung";//samsung


    //MIUI标识
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    //EMUI标识
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    //Flyme标识
    private static final String KEY_FLYME_ID_FALG_KEY = "ro.build.display.id";
    private static final String KEY_FLYME_ID_FALG_VALUE_KEYWORD = "Flyme";
    private static final String KEY_FLYME_ICON_FALG = "persist.sys.use.flyme.icon";
    private static final String KEY_FLYME_SETUP_FALG = "ro.meizu.setupwizard.flyme";
    private static final String KEY_FLYME_PUBLISH_FALG = "ro.flyme.published";


    private static String sName;
    private static String sVersion;

    //华为
    public static boolean isEmui() {
        return check(ROM_EMUI);
    }

    //小米
    public static boolean isMiui() {
        return check(ROM_MIUI);
    }

    //vivo
    public static boolean isVivo() {
        return check(ROM_VIVO);
    }


    public static boolean isOnePlus() {
        return check(ROM_OnePlus);
    }

    //oppo
    public static boolean isOppo() {
        return check(ROM_OPPO);
    }

    //三星
    public static boolean isSamsung() {
        boolean isSamsung = check(ROM_SAMSUNG);
        LogUtil.e(TAG, "RomUtils:ROM_SAMSUNG=" + ROM_SAMSUNG + ";isSamsung=" + isSamsung);
        return isSamsung;
    }

    //魅族
    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }

    //360手机
    public static boolean is360() {
        return check(ROM_QIKU) || check("360");
    }

    public static boolean isSmartisan() {
        return check(ROM_SMARTISAN);
    }

    public static String getName() {
        if (sName == null) {
            check("");
        }

        return sName;
    }

    public static String getVersion() {
        if (sVersion == null) {
            check("");
        }

        return sVersion;
    }

    public static boolean check(String rom) {
        if (sName != null) {
            LogUtil.e(TAG, "check:rom=" + rom + ";sName=" + sName);

            return sName.equals(rom);
        }


        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_SMARTISAN))) {
            sName = ROM_SMARTISAN;
        } else {
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }


        LogUtil.v(TAG, "check:rom=" + rom + ";sVersion=" + sVersion + ";sName=" + sName);

        LogUtil.e("RomName", "RomName=" + sName);

        //特殊系统兼容,realme算真我
        if (sName.equalsIgnoreCase(ROM_REALME)) {
            sName = ROM_OPPO;
        }

        return sName.equals(rom);
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {


            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**************************Android判断手机ROM: 其他方案. end**************************/


    public static String getCallMobileType() {
        String mobileType = "";
        if (RomUtils.isEmui()) {
            mobileType = "huawei";
        } else if (RomUtils.isMiui()) {
            mobileType = "xiaomi";
        } else if (RomUtils.isOppo()) {
            mobileType = "oppo";
        } else if (RomUtils.isVivo()) {
            mobileType = "vivo";
        } else {
            mobileType = "other";
        }

        return mobileType;
    }
}
