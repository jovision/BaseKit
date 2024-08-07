package com.jovision.basekit;

import android.content.Context;

/**
 * @ProjectName: BaseKit
 * @Package: com.jovision.basekit
 * @ClassName: ApplicationKit
 * @Description: java类作用描述
 * @CreateDate: 2024/8/7 09:47
 * @Version: 1.0
 */
public class ApplicationKit{
    private static volatile ApplicationKit instance;
    private static final Object lockObject = new Object();

    public static ApplicationKit getInstance() {
        if (instance == null) {
            synchronized (lockObject) {
                if (instance == null) {
                    instance = new ApplicationKit();
                }
            }
        }
        return instance;
    }

    public Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
