package com.jovision.basekit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("MissingPermission")
public class WifiConfigManager {

    public static final String TAG = "WifiConfigManager";
    private final String IPC_HEAD = "IPC-";
    // 定义一个WifiLock
    WifiLock mWifiLock;
    Context mContext;
    private ConnectivityManager connectivityManager;
    // 定义WifiManager对象
    private static WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private ArrayList<ScanResult> mWifiList;
    // 配置过的网络连接列表
    private ArrayList<WifiConfiguration> mWifiConfiguration;

    // 构造器
    public WifiConfigManager(Context context) {
        mContext = context;
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    // 获取wifi状态
    public static boolean getWifiState() {
        if (null != mWifiManager) {
            return mWifiManager.isWifiEnabled();
        }
        return false;
    }

    // 打开WIFI
    public static boolean openWifi() {
        boolean flag = false;
        if (!mWifiManager.isWifiEnabled()) {
            flag = mWifiManager.setWifiEnabled(true);
        } else {
            flag = true;
        }
        return flag;
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    // 检查当前WIFI状态
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    public boolean ConnectWifiByConfig(WifiConfiguration wifiConfiguration) {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return false;
        }
        boolean flag = false;
        WifiConfiguration wc = isExsits(wifiConfiguration.SSID);

        if (null == wc) {// 未配置过的网络
            flag = addNetwork(wifiConfiguration);
        } else {// 已配置过的网络,使连接可用
            flag = connNetwork(wc);
        }
        return flag;
    }

    @SuppressLint("MissingPermission")
    public ArrayList<ScanResult> startScanIPC() {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return null;
        }
        mWifiManager.startScan();
        // 得到扫描结果
        mWifiList = (ArrayList<ScanResult>) mWifiManager.getScanResults();
        // // 得到配置好的网络连接
        // mWifiConfiguration = (ArrayList<WifiConfiguration>) mWifiManager
        // .getConfiguredNetworks();

        // 只取出IPC路由
        if (null != mWifiList) {
            int size = mWifiList.size();
            for (int i = 0; i < size; i++) {
                String name = mWifiList.get(i).SSID;
                // .replace("\"", "");

                if (!name.startsWith(IPC_HEAD)) {
                    mWifiList.remove(i);
                    i--;
                    size = mWifiList.size();
                }
            }
        }

        return mWifiList;
    }

    public ArrayList<ScanResult> startScanWifi() {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return null;
        }
        try {
            mWifiManager.startScan();
            // 得到扫描结果
            mWifiList = (ArrayList<ScanResult>) mWifiManager.getScanResults();
            // // 得到配置好的网络连接
            if (null != mWifiList && 0 != mWifiList.size()) {
                int size = mWifiList.size();
                for (int i = 0; i < size; i++) {
                    String name = mWifiList.get(i).SSID;

                    if (name.startsWith(IPC_HEAD)
                            || name.equalsIgnoreCase("")) {
                        mWifiList.remove(i);
                        i--;
                        size = mWifiList.size();
                    }

                    if (name.equalsIgnoreCase(IPC_HEAD)) {
                        ScanResult sr = mWifiList.get(i);
                        mWifiList.remove(i);
                        mWifiList.add(0, sr);
                    }
                }
            }
            mWifiList = removeDup(mWifiList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mWifiList;
    }

    public ArrayList<ScanResult> removeDup(ArrayList<ScanResult> sourceList) {

        for (int i = 0; i < sourceList.size() - 1; i++) {
            for (int j = sourceList.size() - 1; j > i; j--) {
                if (sourceList.get(j).SSID
                        .equalsIgnoreCase(sourceList.get(i).SSID)) {
                    sourceList.remove(j);
                }
            }
        }
        return sourceList; // 返回集合
    }

    // 得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    // 查看扫描结果
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder
                    .append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }



    // 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 得到接入点的SSID
    public String getSSID() {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return "";
        }
        mWifiInfo = mWifiManager.getConnectionInfo();
        LogUtil.e("wifi", "wifiInfo= " + mWifiInfo);
        return (mWifiInfo == null) ? "" : mWifiInfo.getSSID();
    }

    // 得到IP地址
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 添加一个网络并连接
    public boolean addNetwork(WifiConfiguration wcg) {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return false;
        }

        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        return b;
    }

    // 连接一个网络
    public boolean connNetwork(WifiConfiguration wcg) {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return false;
        }
        int wcgID = wcg.networkId;
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        return b;
    }

    // 断开指定ID的网络
    public void disconnectWifi(WifiConfiguration wifiConfiguration,
                               boolean remove) {
        if (!getWifiState()) {// 如果wifi为关闭状态
            return;
        }

        int netId = -1;

        WifiConfiguration wc = isExsits(wifiConfiguration.SSID);
        if (null == wc) {
            return;
        } else {
            netId = wc.networkId;
        }

        // LogUtil.v("断开的网络SSID---", "SSID--" + wifiConfiguration.SSID);
        // LogUtil.v("断开的网络ID---", "netId--" + netId);
        try {
            if (null == mWifiManager) {
                // 取得WifiManager对象
                mWifiManager = (WifiManager) mContext
                        .getSystemService(Context.WIFI_SERVICE);
                // 取得WifiInfo对象
                mWifiInfo = mWifiManager.getConnectionInfo();
            }
            mWifiManager.disableNetwork(netId);
            // mWifiManager.disconnect();
            if (remove) {
                mWifiManager.removeNetwork(netId);// 移除网络
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 分为三种情况：1没有密码2用wep加密3用wpa加密
    public WifiConfiguration CreateWifiInfo(String SSID, String Password,
                                            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        WifiConfiguration tempConfig = isExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) {
            config.wepKeys[0] = "\"" + "" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            config.hiddenSSID = true;

        }
        if (Type == 2) {

            // WifiConfiguration wifiConfig = new WifiConfiguration();
            // wifiConfig.SSID = String.format("\"%s\"", ssid);
            // wifiConfig.preSharedKey = String.format("\"%s\"", key);
            // WifiManager wifiManager =
            // (WifiManager)this.getSystemService(WIFI_SERVICE);
            // int netId = wifiManager.addNetwork(wifiConfig)
            // wifiManager.disconnect();
            // wifiManager.enableNetwork(netId, true);
            // wifiManager.reconnect();

            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.preSharedKey = String.format("\"%s\"", Password);
            // config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            // config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            // config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            // config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        }
        if (Type == 3) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }


    public WifiConfiguration isExsits(String SSID) {
        try {
            if (null == SSID) {
                return null;
            }
            if (!getWifiState()) {// 如果wifi为关闭状态
                return null;
            }

            // LogUtil.v("连接的网络：", SSID + "");

            if (null == mWifiManager) {
                // 取得WifiManager对象
                mWifiManager = (WifiManager) mContext
                        .getSystemService(Context.WIFI_SERVICE);
                // 取得WifiInfo对象
                mWifiInfo = mWifiManager.getConnectionInfo();
            }

            List<WifiConfiguration> existingConfigs = mWifiManager
                    .getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {

                String str1 = existingConfig.SSID.replace("\"", "");
                String str2 = SSID.replace("\"", "");
                if (str1.equals(str2)) {
                    // LogUtil.v("找到了：", existingConfig.SSID + "");
                    return existingConfig;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) {
            mWifiManager.setWifiEnabled(false);
        }

        try {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "NETGEAR49-2G";
            config.preSharedKey = "37DD5A0741";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

            return (Boolean) method.invoke(mWifiManager, config, enabled);

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }

    }

    public int[] getWifiAuthEnc(String targetSSID) {
        int result[] = null;

        if (TextUtils.isEmpty(targetSSID)) {
            WifiInfo info = mWifiManager.getConnectionInfo();

            if (null != info) {
                targetSSID = info.getSSID();
            }
        }

        if (false == TextUtils.isEmpty(targetSSID)) {
            result = new int[2];
            result[0] = 0;
            result[1] = 0;

            // [Neo] Open?
            final String[] auth = {
                    "NONE", "NEO", "NEO", "WPA-PSK",
                    "WPA2-PSK", "NEO"
            };
            final String[] enc = {
                    "NONE", "WEP", "TKIP", "CCMP"
            };

            ArrayList<ScanResult> results = (ArrayList<ScanResult>) mWifiManager
                    .getScanResults();
            for (ScanResult item : results) {
                if (item.SSID.equals(targetSSID)) {
                    boolean isFound = false;
                    String combines[] = item.capabilities.replace("[", "")
                            .split("]");

                    int size = combines.length;
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < 6; j++) {
                            // System.out.println("auth: " + j);
                            if (combines[i].contains(auth[j])) {
                                result[0] = j;
                                isFound = true;
                                break;
                            }
                        }

                        for (int j = 0; j < 4; j++) {
                            // System.out.println("enc: " + j);
                            if (combines[i].contains(enc[j])) {
                                result[1] = j;
                                break;
                            }
                        }

                        // System.out.println("combine: " + combines[i]);

                        if (isFound) {
                            // break;
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 切换到原来网络
     *
     * @param disWifi
     * @param connWifi
     * @return
     */
    public boolean changeWifi(String disWifi, String connWifi,
                              boolean oldWifiState) {
        oldWifiState = true;
        boolean state = getWifiState(connWifi);

        if (state) {
            return state;
        }

        LogUtil.v(TAG, "changeWifi-E:" + disWifi + "-" + connWifi);
        boolean changeRes = false;
        try {
            // 断开跟连接的wifi 一样不做任何处理
            if (null != disWifi && !"".equalsIgnoreCase(disWifi)
                    && null != connWifi && !"".equalsIgnoreCase(connWifi)
                    && disWifi.equalsIgnoreCase(connWifi)) {
                changeRes = true;
                return changeRes;
            } else {
                if (oldWifiState) {// 原wifi开着的，恢复到原来的网络
                    // 断开现在的wifi
                    if (null != disWifi && !"".equalsIgnoreCase(disWifi)) {
                        WifiConfiguration currWifi = isExsits(disWifi);
                        if (null != currWifi) {
                            LogUtil.v("完成配置断开", disWifi);
                            disconnectWifi(currWifi, true);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }

                    // 完成配置连上原来的wifi
                    if (null != connWifi) {
                        WifiConfiguration oldWifi = isExsits(connWifi);
                        if (null != oldWifi) {
                            LogUtil.v("完成配置连接", connWifi);
                            boolean connRes = false;
                            int count = 0;
                            while (!connRes) {// 没连接调用连接方法
                                if (count < 10) {
                                    count++;
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    connRes = connNetwork(oldWifi);
                                    LogUtil.v("完成配置", connWifi + "----" + count
                                            + "-----调用连接-----" + connRes);
                                } else {
                                    connRes = true;
                                    break;
                                }
                            }

                            count = 0;
                            if (connRes) {// 已连接
                                while (!changeRes) {// 没连接调用连接方法
                                    if (count < 20) {
                                        count++;
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                        changeRes = getWifiState(oldWifi.SSID);
                                        LogUtil.v("完成配置", connWifi + "----"
                                                + count + "-----连接结果-----"
                                                + changeRes);
                                    } else {
                                        changeRes = false;
                                        break;
                                    }

                                }
                            } else {
                                changeRes = false;
                            }

                        }
                    } else {
                        changeRes = true;
                    }
                } else {// 原wifi关闭状态，关闭wifi
                    closeWifi();
                    changeRes = true;
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        LogUtil.v(TAG, "changeWifi-X:" + changeRes);
        return changeRes;

    }

    /**
     * 判断网络连接状态，wifiadmin不准确
     *
     * @param wifiName
     * @return
     */
    public boolean getWifiState(String wifiName) {
        boolean flag = false;
        try {
            if (null == connectivityManager) {
                connectivityManager = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            NetworkInfo net = connectivityManager.getActiveNetworkInfo();
            if (net == null) {
            } else {
                if ("CONNECTED".equalsIgnoreCase(net.getState().name())) {
                    if (null != this.getSSID()
                            && !"".equalsIgnoreCase(this.getSSID().trim())) {
                        String str1 = this.getSSID().replace("\"", "");
                        String str2 = wifiName.replace("\"", "");

                        if (str1.equalsIgnoreCase(str2)) {
                            flag = true;
                        } else {
                            WifiConfiguration oldWifi = isExsits(str1);
                            if (null != oldWifi) {
                                disconnectWifi(oldWifi, false);// 关掉，不移除
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 检测网络连接是否真的可用<br/>
     *
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        // 使用连接web的方式,不使用ping
//        return connectionWebUrl();
//        return pingIpAddress("www.baidu.com");


        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            LogUtil.v(TAG,"isAvailable-isNetWorkConnected-no permission，net true");
            return true;
        } else {
            //有权限，但是是Ap信号，认为没有网络
            if (NetWorkUtil.isApSignal(context)) {
                LogUtil.e(TAG,"isAvailable-isNetWorkConnected-ap net false");
                return false;
            }
        }



        LogUtil.v(TAG,"isAvailable-isNetWorkConnected-go on isAvailable");

        return isAvailable();
    }


    /**
     * 网络连接判断方式_使用HttpUrlConnection连接web地址<br/>
     * 此中方式能设置超时
     */
    private static boolean connectionWebUrl() {
        boolean result = false;
//        String webUrl1 = "http://www.baidu.com";
//        String webUrl2 = "https://www.baidu.com";
//        String webUrl3 = "http://appdown.cloudsee.net/";
        String webUrl1 = "www.baidu.com";
        String webUrl2 = "www.google.com";
        String[] urlArray = {webUrl1, webUrl2};

        for (int i = 0; i < 2; i++) {
            boolean pingResult = pingWeb(urlArray[i]);
            if (pingResult) {
                result = true;
                break;
            }
        }
        return result;
    }


    public static boolean isAvailable() {
        LogUtil.v("fdasdfasffasf","isAvailable-E");

        Runtime runtime = Runtime.getRuntime();
        try {
            // 会阻塞线程 ping baidu 1次
            Process exec = runtime.exec("ping -c 1 -w 2 www.baidu.com");
            int i = exec.waitFor();
            //wifi不可用或未连接，返回2；WiFi需要认证，返回1；WiFi可用，返回0；

            LogUtil.e("fdasdfasffasf","isAvailable-X");
            return i == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean pingIpAddress(String ipAddress) {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 2 " + ipAddress);

            int status = process.waitFor();
            if (status == 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean pingWeb(String webUrl) {
        String result = null;
        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + webUrl);// ping1次
            // 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            LogUtil.v(TAG, "result content : " + stringBuffer.toString());
            // PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                return true;
            } else {
                result = "failed~ cannot reach the IP address";
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            LogUtil.v(TAG, "result = " + result);
        }
        return false;
    }


    /**
     * 获取当前连接wifi热点的ScanResult
     */
    public ScanResult getCurrentScanResult() {
        String ssid = getSSID();
        LogUtil.e("wifi", "getCurrentScanResult: " + ssid);
        if (null != ssid && !"".equalsIgnoreCase(ssid)) {
            ssid = ssid.substring(1, ssid.length() - 1);
            ArrayList<ScanResult> list = startScanWifi();
            if (list == null)
                return null;
            for (ScanResult result : list) {
                if (ssid.equals(result.SSID))
                    return result;
            }
        }
        return null;
    }

    /**
     * 获取2.4G或者5G的标签
     *
     * @param frequency
     * @return
     */
    public String getWifiG(int frequency) {
        String wifiG = "";
        if (frequency >= 5000) {
            wifiG = "5G";
        } else if (frequency >= 2400 && frequency < 5000) {
            wifiG = "2.4G";
        } else if (frequency < 2400) {
            wifiG = "2G";
        }
        return wifiG;
    }


    /**
     * 判断是否连接WIFI
     *
     * @param context 上下文
     * @return boolean
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }


//    /**
//     * 移除网络
//     */
//    public void removeNetWork(String netWorkSSID) {
//
//        try {
//
//            LogUtil.v("找到了：", "removeNetWork-------netWorkSSID=" + netWorkSSID);
//            if (null == netWorkSSID) {
//                LogUtil.v("找到了：", "removeNetWork----1---netWorkSSID=" + netWorkSSID);
//                return;
//            }
//            if (!getWifiState()) {// 如果wifi为关闭状态
//                LogUtil.v("找到了：", "removeNetWork----3---netWorkSSID=" + netWorkSSID);
//                return;
//            }
//
//            if (null == mWifiManager) {
//                // 取得WifiManager对象
//                mWifiManager = (WifiManager) mContext
//                        .getSystemService(Context.WIFI_SERVICE);
//                // 取得WifiInfo对象
//                mWifiInfo = mWifiManager.getConnectionInfo();
//            }
//
//
//            LogUtil.v("找到了：", "removeNetWork----4---netWorkSSID=" + netWorkSSID);
//            List<WifiConfiguration> existingConfigs = mWifiManager
//                    .getConfiguredNetworks();
//            LogUtil.v("找到了：", "removeNetWork----5---netWorkSSID=" + netWorkSSID+";existingConfigs="+existingConfigs.size());
//            for (WifiConfiguration existingConfig : existingConfigs) {
//                LogUtil.v("找到了：", "removeNetWork----6---netWorkSSID=" + netWorkSSID);
//                String str1 = existingConfig.SSID.replace("\"", "");
//                String str2 = netWorkSSID.replace("\"", "");
//                if (str1.equals(str2)) {
//                    LogUtil.v("找到了：", existingConfig.SSID + "");
//                    boolean disableNetwork = mWifiManager.disableNetwork(existingConfig.networkId);
//                    LogUtil.v("找到了：", existingConfig.SSID + ",disableNetwork=" + disableNetwork);
//
//                    boolean removeNetwork = mWifiManager.removeNetwork(existingConfig.networkId);// 移除网络
//                    LogUtil.v("找到了：", existingConfig.SSID + ",removeNetwork=" + removeNetwork);
//
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
}
