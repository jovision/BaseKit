package com.jovision.basekit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LogUtil {

    private static final String V = "V";
    private static final String D = "D";
    private static final String I = "I";
    private static final String W = "W";
    private static final String E = "e";
    private static final String Ex = "E";

    private static final String RW = "rw";
    private static final String TAG = "LogUtil";
    private static final String LINE_SEPARATOR = "\n";
    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss.SSS";

    private static String FOLDER = null;

    private static boolean ENABLE_FILE = false;
    private static boolean ENABLE_LOGCAT = true;

    private static File checkTag(String tag) {
        File result = null;

        if (null != FOLDER && init(FOLDER)) {
            result = new File(FOLDER + File.separator + tag + ".txt");
            if (!result.exists()) {
                try {
                    result.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    @SuppressLint("SimpleDateFormat")
    private static String prepare(String level, String msg) {
        StringBuilder sBuilder = new StringBuilder(msg.length() + 64);
        sBuilder.append(new SimpleDateFormat(FORMATTER).format(new Date()))
                .append(File.pathSeparatorChar);

        if (null != level) {
            sBuilder.append(level).append(File.separatorChar);
        }

        sBuilder.append(msg);
        sBuilder.append(LINE_SEPARATOR);
        return sBuilder.toString();
    }

    private static synchronized boolean append(File file, String string) {
        boolean result = false;

        if (null != file) {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file.getPath(), RW);
                randomAccessFile.seek(randomAccessFile.length());
                randomAccessFile.writeBytes(new String(string.getBytes
                        ("UTF-8"), "iso-8859-1"));
                randomAccessFile.close();
                randomAccessFile = null;
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e2) {
                    } finally {
                        randomAccessFile = null;
                    }
                }

                Log.e(TAG, "append failed when write");
//                ENABLE_FILE = false;
            }
        } else {
            Log.e(TAG, "append without file");
        }

        return result;
    }

    /**
     * 该方法会自动打印类名和方法名，方便查找打印
     *
     * @param log
     */
    public static void v(String log) {
        StackTraceElement invoke = getInvoker();
        String tag = invoke.getClassName();
        log = "【" + invoke.getMethodName() + ":" + invoke.getLineNumber() + "】"
                + log;
        v(tag, log);

    }

    public static void v(String tag, String msg) {
        if (msg == null) {
            msg = "NULL";
        }

        if (ENABLE_LOGCAT) {
            Log.v(tag, msg);
        }

        if (ENABLE_FILE) {
            append(checkTag(tag), prepare(V, msg));
        }
    }

    /**
     * 该方法会自动打印类名和方法名，方便查找打印
     *
     * @param log
     */
    public static void d(String log) {
        StackTraceElement invoke = getInvoker();
        String tag = invoke.getClassName();
        log = "【" + invoke.getMethodName() + ":" + invoke.getLineNumber() + "】"
                + log;
        d(tag, log);

    }

    public static void d(String tag, String msg) {
        if (msg == null) {
            msg = "NULL";
        }

        if (ENABLE_LOGCAT) {
            Log.d(tag, msg);
        }

        if (ENABLE_FILE) {
            append(checkTag(tag), prepare(D, msg));
        }
    }

    /**
     * 该方法会自动打印类名和方法名，方便查找打印
     *
     * @param log
     */
    public static void i(String log) {
        StackTraceElement invoke = getInvoker();
        String tag = invoke.getClassName();
        log = "【" + invoke.getMethodName() + ":" + invoke.getLineNumber() + "】"
                + log;
        i(tag, log);

    }

    public static void i(String tag, String msg) {
        if (msg == null) {
            msg = "NULL";
        }

        if (ENABLE_LOGCAT) {
            Log.i(tag, msg);
        }

        if (ENABLE_FILE) {
            append(checkTag(tag), prepare(I, msg));
        }
    }

    private static StackTraceElement getInvoker() {
        return Thread.currentThread().getStackTrace()[4];
    }

    public static String getPath(String tag) {
        String filePath = null;

        File file = checkTag(tag);
        if (null != file && file.exists()) {
            filePath = file.getPath();
        }

        return filePath;
    }

    public static String getFolder(String tag) {
        String filePath = null;

        File file = checkTag(tag);
        if (null != file && file.exists()) {
            filePath = file.getParent();
        }

        return filePath;
    }

    public static boolean clean(String tag) {
        boolean result = false;

        File file = checkTag(tag);
        if (null != file && file.exists()) {
            result = file.delete();
        }

        return result;
    }

    /**
     * 该方法会自动打印类名和方法名，方便查找打印
     *
     * @param log
     */
    public static void w(String log) {
        StackTraceElement invoke = getInvoker();
        String tag = invoke.getClassName();
        log = "【" + invoke.getMethodName() + ":" + invoke.getLineNumber() + "】"
                + log;
        w(tag, log);

    }

    public static void w(String tag, String msg) {
        if (msg == null) {
            msg = "NULL";
        }

        if (ENABLE_LOGCAT) {
            Log.w(tag, msg);
        }

        if (ENABLE_FILE) {
            append(checkTag(tag), prepare(W, msg));
        }
    }

    /**
     * 该方法会自动打印类名和方法名，方便查找打印
     *
     * @param log
     */
    public static void e(String log) {
        StackTraceElement invoke = getInvoker();
        String tag = invoke.getClassName();
        log = "【" + invoke.getMethodName() + ":" + invoke.getLineNumber() + "】"
                + log;
        e(tag, log);

    }

    public static void e(String tag, String msg) {
        if (msg == null) {
            msg = "NULL";
        }

        if (ENABLE_LOGCAT) {
            Log.e(tag, msg);
        }

        if (ENABLE_FILE) {
            append(checkTag(tag), prepare(E, msg));
        }
    }

    public static void e(String tag, Exception e) {
        StringBuilder sBuilder = new StringBuilder(16 * 1024);
        sBuilder.append(e.getMessage()).append(LINE_SEPARATOR);

        StackTraceElement[] elements = e.getStackTrace();
        int size = elements.length;

        for (int i = 0; i < size; i++) {
            sBuilder.append(elements[i].getClassName()).append(".")
                    .append(elements[i].getMethodName()).append("@")
                    .append(elements[i].getLineNumber()).append(LINE_SEPARATOR);
        }

        String msg = sBuilder.toString();

        if (ENABLE_LOGCAT) {
            Log.e(tag, msg);
        }

        if (ENABLE_FILE) {
            append(checkTag(tag), prepare(Ex, msg));
        }
    }

    public static void fmt(String format, Object... args) {
        String formatted = String.format(format, args);
        StackTraceElement invoke = getInvoker();
        String tag = invoke.getClassName();
        i(tag, formatted);
    }

    /**
     * set log files' folder
     *
     * @param logPath
     * @return
     */
    public static boolean init(String logPath) {
        boolean result = false;
        //TODO: 16/4/11
//        if (!AppConsts.DEBUG_STATE) {//正式版本
//            return true;
//        }

        File folder = new File(logPath);
        if (!folder.exists()) {
            result = folder.mkdirs();
        } else {
            result = true;
        }

        if (result) {
            result = false;
            if (folder.canRead() && folder.canWrite() && folder.isDirectory()) {
                FOLDER = logPath;
                result = true;
            }
        }

        return result;
    }

    public static void enableFile(boolean enable) {
        ENABLE_FILE = enable;
    }

    public static void enableLogcat(boolean enable) {
        ENABLE_LOGCAT = enable;
    }

    public static boolean getEnableLogcat() {
        return ENABLE_LOGCAT;
    }



//    public static boolean isDebuggable(Context context) {
//        boolean debuggable = true;
//        PackageManager pm = context.getPackageManager();
//        try{
//            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
//            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
//        }catch(PackageManager.NameNotFoundException e){
//            /*debuggable variable will remain false*/
//            debuggable=true;
//        }
//        return debuggable;
//    }
//
//
//    public static void v(String tag, String msg) {
//        if (App.LogFlag)
//            android.util.Log.v(tag, msg);
//    }
//
//    public static void v(String tag, String msg, Throwable t) {
//        if (App.LogFlag)
//            android.util.Log.v(tag, msg, t);
//    }
//
//    public static void d(String tag, String msg) {
//        if (App.LogFlag)
//            android.util.Log.d(tag, msg);
//    }
//
//    public static void d(String tag, String msg, Throwable t) {
//        if (App.LogFlag)
//            android.util.Log.d(tag, msg, t);
//    }
//
//    public static void i(String tag, String msg) {
//        if (App.LogFlag)
//            android.util.Log.i(tag, msg);
//    }
//
//    public static void i(String tag, String msg, Throwable t) {
//        if (App.LogFlag)
//            android.util.Log.i(tag, msg, t);
//    }
//
//    public static void w(String tag, String msg) {
//        if (App.LogFlag)
//            android.util.Log.w(tag, msg);
//    }
//
//    public static void w(String tag, String msg, Throwable t) {
//        if (App.LogFlag)
//            android.util.Log.w(tag, msg, t);
//    }
//
//    public static void e(String tag, String msg) {
//        if (App.LogFlag)
//            android.util.Log.e(tag, msg);
//    }
//
//    public static void e(String tag, String msg, Throwable t) {
//        if (App.LogFlag)
//            android.util.Log.e(tag, msg, t);
//    }
}
