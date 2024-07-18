package com.jovision.basekit;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by zhp on 2020/11/26
 */
public class DrawableUtil {

    /**
     * 将资源图片转换为Drawable对象
     *
     * @param ResId
     * @return
     */
    public static Drawable loadDrawable(Context context, int ResId) {
        Drawable drawable = context.getResources().getDrawable(ResId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }

    /**
     * 根据名字获取资源
     *
     * @param name
     * @return
     */
    public static Drawable getNameDrawable(Context context, String name) {
        Drawable image = null;
        try {
            int resID = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            image = context.getResources().getDrawable(resID);
        } catch (Exception ignored) {

        }
        return image;
    }

    /**
     * 根据名字获取资源
     *
     * @param name
     * @return
     */
    public static int getNameRes(Context context, String name) {
        int resID = 0;
        try {
            resID = context.getResources().getIdentifier(name.toLowerCase(), "drawable", context.getPackageName());
        } catch (Exception ignored) {

        }
        return resID;
    }
}
