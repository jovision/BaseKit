package com.jovision.basekit;

import android.content.Context;
import android.os.Build;

import java.util.Locale;


/**
 * @ProjectName: HoloSens
 * @Package: com.huawei.holosens.utils
 * @ClassName: AppLanUtil
 * @Description: java类作用描述
 * @Author: 作者名
 * @CreateDate: 2020-01-07 11:28
 * @Version: 1.0
 */
public class AppLanUtil {

    public static boolean isChinese(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        //获取语言的正确姿势：
        String lang = locale.getLanguage();
        if (lang.contains("zh")){
            return true;
        }
        return false;
    }

}
