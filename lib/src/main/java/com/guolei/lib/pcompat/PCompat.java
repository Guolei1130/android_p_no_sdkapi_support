package com.guolei.lib.pcompat;

import android.os.Build;

import java.lang.reflect.Field;

/**
 * Created by Android Studio.
 * User: guolei
 * Email: 1120832563@qq.com
 * Date: 2019/2/17
 * Time: 10:25 AM
 * Desc:
 */
public class PCompat {
    public static void compat(Class reflectionHelperClz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Class classClz = Class.class;
                Field classLoaderField = classClz.getDeclaredField("classLoader");
                classLoaderField.setAccessible(true);
                classLoaderField.set(reflectionHelperClz, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void useDefaultReflectHelperClass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Class classClz = Class.class;
                Field classLoaderField = classClz.getDeclaredField("classLoader");
                classLoaderField.setAccessible(true);
                classLoaderField.set(ReflectHelper.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
