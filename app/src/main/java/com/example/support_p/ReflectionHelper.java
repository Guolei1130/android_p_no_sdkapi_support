package com.example.support_p;
//       _       _    _     _
//__   _(_)_ __ | | _(_) __| |
//\ \ / / | '_ \| |/ / |/ _` |
// \ V /| | |_) |   <| | (_| |
//  \_/ |_| .__/|_|\_\_|\__,_|
//        |_|

/* guolei2@vipkid.com.cn
 *
 *2018/9/17
 */

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {

  private static final String TAG = ReflectionHelper.class.getSimpleName();

  public static void testMethod() {
    try {
      Class clz = Class.forName("android.app.ActivityThread");
      Method method = clz.getDeclaredMethod("getIntentBeingBroadcast");
      method.setAccessible(true);
      method.invoke(null);
      Log.e(TAG, "testMethod: 反射成功");
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "testMethod: 反射失败");
    }
  }

  public static void testField() {
    try {
      Class clz = Class.forName("android.app.ActivityThread");
      Field field = clz.getDeclaredField("TAG");
      field.setAccessible(true);
      field.get(null);
      Log.e(TAG, "testField: 反射成功");
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "testField: 反射失败");
    }

  }
}
