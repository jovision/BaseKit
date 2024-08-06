package com.jovision.basekit.deviceid;

import android.content.Context;
import android.util.Log;

import com.jovision.basekit.LogUtil;


/**
 * Copyright©中维世纪
 *
 * @author deli
 * 创建日期：2020/12/3
 * 描述：
 */
public class DeviceFlagUtil {
/*此方法作为设备的唯一标识符*/
    public static String getUniqueDeviceId(Context context){
        String uuid = "";
        DeviceUuidFactory  uuidFactory = new DeviceUuidFactory(context);
        uuid = uuidFactory.getUuid().toString();

        LogUtil.v("UUID","uuid="+uuid);
        return uuid;
    }
}
