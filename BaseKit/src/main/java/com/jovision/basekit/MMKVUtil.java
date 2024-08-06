package com.jovision.basekit;


import com.tencent.mmkv.MMKV;

//替代sp解决java.lang.CloneNotSupportedException的问题
public class MMKVUtil {

    private static String TAG = "MMKVUtil";
    private static MMKVUtil instance;


    public MMKV mmkv;


    private MMKVUtil() {
        mmkv = MMKV.defaultMMKV();
    }


    /**
     * 单例，MMKVUtil 实例
     */
    public static MMKVUtil getInstance() {
        if (null == instance) {
            instance = new MMKVUtil();
//            instance.moveData();
        }

        return instance;
    }

    public void clearMMKVCache(boolean keepAccount) {
        mmkv.clearAll();
        LogUtil.e(TAG, "clearMMKVCache");
    }

    /**
     * @param token 短Token
     */
    public void saveToken(String token, double tokenValidPeriod) {
        mmkv.encode("token", token);
        LogUtil.e(TAG, "saveToken,token=" + token);
        mmkv.encode("tokenValidPeriod", (long) tokenValidPeriod);
        LogUtil.e(TAG, "saveToken,tokenValidPeriod=" + tokenValidPeriod);

    }


//    //启动之后迁移一次数据
//    public void moveData() {
//        //从sp里获取老的token不为空就认为有数据，把数据迁移过来
//        String spToken = SharePreferencesUtils.getToken();
//        LogUtil.e(TAG, "getToken,spToken=" + spToken);
//
//        //不为空，把token迁移过来
//        if (null != spToken && !spToken.isEmpty()) {
//            LogUtil.e(TAG, "getToken,spToken not null,迁移数据");
//            long tokenValidPeriod = SPUtils.getInstance(Constant.userParts).getLong("tokenValidPeriod");
//            saveToken(spToken, tokenValidPeriod);
//            SharePreferencesUtils.clearSpToken();
//
//            String spTiken = SharePreferencesUtils.getTiken();
//            LogUtil.e(TAG, "getToken,spTiken=" + spTiken);
//            long spTikenValidPeriod = SPUtils.getInstance(Constant.userParts).getLong("tikenValidPeriod");
//            LogUtil.e(TAG, "getToken,spTikenValidPeriod=" + spTikenValidPeriod);
//            saveTiken(spToken, spTikenValidPeriod);
//            SharePreferencesUtils.clearSpTiken();
//        } else {
//            LogUtil.e(TAG, "getToken,spToken=" + spToken + ";无需迁移数据");
//        }
//
//    }

    public String getToken() {
        String token = "";
        if (null != mmkv) {
            token = mmkv.decodeString("token");
        }
//        LogUtil.e(TAG, "getToken,token=" + token);
        return token;
    }

    /**
     * @param tiken 短Tiken
     */
    public void saveTiken(String tiken, double tikenValidPeriod) {
        mmkv.encode("tiken", tiken);
        LogUtil.e(TAG, "saveTiken,tiken=" + tiken);
        mmkv.encode("tikenValidPeriod", (long) tikenValidPeriod);
        LogUtil.e(TAG, "saveTiken,tikenValidPeriod=" + tikenValidPeriod);
    }

    public String getTiken() {
        String tiken = mmkv.decodeString("tiken");
        LogUtil.e(TAG, "getTiken,tiken=" + tiken);
        return tiken;
    }

    public long getTikenValidPeriod() {
        long tikenValidPeriod = mmkv.decodeLong("tikenValidPeriod", 0);
        LogUtil.e(TAG, "getTikenValidPeriod,tikenValidPeriod=" + tikenValidPeriod);
        return tikenValidPeriod;
    }


}
