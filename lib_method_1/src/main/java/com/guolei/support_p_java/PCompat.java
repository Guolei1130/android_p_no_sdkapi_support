package com.guolei.support_p_java;


import android.os.Build;

import java.lang.reflect.Field;

/**
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 2018/10/6
 * Time: 2:09 PM
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

}
