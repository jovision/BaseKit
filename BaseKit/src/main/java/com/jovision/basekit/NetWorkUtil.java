package com.jovision.basekit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.jovision.basekit.constant.BaseConstant;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

@SuppressLint("MissingPermission")
public class NetWorkUtil {

    private static final String TAG = "NetWorkUtil";

    /**
     * 获取本机ip地址
     *
     * @return
     */
    public static String getLocalHostIp() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                if ("wlan0".equalsIgnoreCase(nif.getName()) || "eth0"
                        .equalsIgnoreCase(nif.getName())) {
                    // 遍历每一个接口绑定的所有ip
                    while (inet.hasMoreElements()) {
                        InetAddress inetAddress = inet.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     * 获取当前连接的wifi名称
     *
     * @return
     */
    public static String getWIFIName(Context context) {
        //获取SSID
        String ssid = "";
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            ssid = info.getSSID().replace("\"", "");
        }

        return ssid;
    }

    public static boolean isWifi5G(Context context, String ssid) {
        int freq = 0;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
//            freq = wifiInfo.getFrequency();
//        } else {
//            String ssid = wifiInfo.getSSID();
        if (ssid != null && ssid.length() > 2) {
            String ssidTemp = ssid.substring(1, ssid.length() - 1);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {
                if (scanResult.SSID.equals(ssidTemp)) {
                    freq = scanResult.frequency;
                    break;
                }
            }
        }
//        }
        return freq > 4900 && freq < 5900;
    }



    /**
     * 检查当前连接的Wifi是否为AP信号
     *
     * @return
     */
    public static boolean isApSignal(Context mContext) {
        boolean isApSignal = false;
        WifiConfigManager wcm = new WifiConfigManager(mContext);
        String ssid = wcm.getSSID().replace("\"", "");
        if (ssid.startsWith(BaseConstant.AP_HEAD_STR)){
            isApSignal = true;
        }

        LogUtil.v(TAG,"isApSignal:ssid="+ssid+";isApSignal="+isApSignal);
        return isApSignal;
    }



    /**
     * 获取当前连接的ApWifi名字
     *
     * @return
     */
    public static String getApDeviceSn(Context mContext) {
        String deviceSn = "";
        boolean isApSignal = false;
        WifiConfigManager wcm = new WifiConfigManager(mContext);
        String ssid = wcm.getSSID().replace("\"", "");
        if (ssid.startsWith(BaseConstant.AP_HEAD_STR)){
            isApSignal = true;
        }

        if(isApSignal){
            deviceSn = ssid.replace(BaseConstant.AP_HEAD_STR,"");
        }
        return deviceSn;
    }



    /**
     * 获取当前连接的ApWifi名字
     *
     * @return
     */
    public static boolean isThisApWifi(Context mContext,String deviceSn) {
        boolean isApSignal = false;
        WifiConfigManager wcm = new WifiConfigManager(mContext);
        String ssid = wcm.getSSID().replace("\"", "");

        if(ssid.equalsIgnoreCase(BaseConstant.AP_HEAD_STR+deviceSn)){
            isApSignal = true;
        } else {
            isApSignal = false;
        }
        LogUtil.v(TAG,"isThisApWifi:ssid="+ssid+";Constant.AP_HEAD_STR="+BaseConstant.AP_HEAD_STR+";deviceSn="+deviceSn+";isApSignal="+isApSignal);

        return isApSignal;
    }
}
