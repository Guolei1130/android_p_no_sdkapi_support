package com.example.support_p;

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
