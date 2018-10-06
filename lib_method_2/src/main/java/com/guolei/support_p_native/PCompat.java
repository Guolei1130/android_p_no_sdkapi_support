package com.guolei.support_p_native;

/**
 * Created by Android Studio.
 * User: guolei
 * Email: 1120832563@qq.com
 * Date: 2018/10/6
 * Time: 2:30 PM
 * Desc:
 */
public class PCompat {

    static {
        System.loadLibrary("native_hook");
    }

    public native void nativeHook(String fullPath);

}
