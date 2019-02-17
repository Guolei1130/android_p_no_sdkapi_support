package com.guolei.lib.pcompat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Android Studio.
 * User: guolei
 * Email: 1120832563@qq.com
 * Date: 2019/2/17
 * Time: 10:39 AM
 * Desc:
 */
public class ReflectHelper {

    public static Class<?> forName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    public static Class<?> forName(String className, boolean initialize, ClassLoader classLoader)
            throws ClassNotFoundException {
        return Class.forName(className, initialize, classLoader);
    }

    public static Field[] getFields(Class<?> clz) {
        return clz.getFields();
    }

    public static Field getField(Class<?> clz, String name) throws NoSuchFieldException {
        return clz.getField(name);
    }

    public static Field[] getDeclaredFields(Class<?> clz) {
        return clz.getDeclaredFields();
    }

    public static Field getDeclaredField(Class<?> clz, String name) throws NoSuchFieldException {
        return clz.getDeclaredField(name);
    }

    public static Method[] getMethods(Class<?> clz) {
        return clz.getMethods();
    }

    public static Method getMethod(Class<?> clz, String name, Class<?>... parameterType)
            throws NoSuchMethodException {
        return clz.getMethod(name, parameterType);
    }

    public static Method[] getDeclaredMethods(Class<?> clz) {
        return clz.getDeclaredMethods();
    }

    public static Method getDeclaredMethod(Class<?> clz, String name, Class<?>... parameterType)
            throws NoSuchMethodException {
        return clz.getDeclaredMethod(name, parameterType);
    }

    public static Object invokeStaticMethod(Class<?> clz, Object instance,
                                            String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        if (params == null) {
            method = clz.getMethod(methodName);
        } else {
            Class<?> types[] = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                types[i] = params[i].getClass();
            }
            method = clz.getMethod(methodName, types);
        }
        return method.invoke(instance, params);
    }

    public static Object invokeMethod(Class<?> clz, Object instance,
                                      String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        if (params == null) {
            method = clz.getDeclaredMethod(methodName);
        } else {
            Class<?> types[] = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                types[i] = params[i].getClass();
            }
            method = clz.getDeclaredMethod(methodName, types);
        }
        return method.invoke(instance, params);
    }

    public static Object getStaticFieldValue(Class<?> clz, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clz.getField(fieldName);
        return field.get(null);
    }

    public static Object getFieldValue(Class<?> clz, Object instance, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clz.getDeclaredField(fieldName);
        return field.get(instance);
    }


}
