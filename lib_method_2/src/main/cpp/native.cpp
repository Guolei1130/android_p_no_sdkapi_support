//
// Created by guolei on 2018/10/6.
//
#include <jni.h>
#include <dlfcn.h>
#include <cstring>
#include "v_hook.h"

extern "C" {

extern void *v_dlopen(const char *filename, int flags);
extern void *v_dlsym(void *handle, const char *symbol);
extern int v_dlclose(void *handle);
extern void v_hook_function(void *symbol, void *replace, void **result);
}

class ObjPtr {
 public:
  uintptr_t reference_;
};

void (*setClassLoader)(void *pClass, void *new_cl);
ObjPtr (*toClass)(jclass global_jclss);

char* jstringToChar(JNIEnv* env, jstring jstr) {
  char* rtn = NULL;
  jclass clsstring = env->FindClass("java/lang/String");
  jstring strencode = env->NewStringUTF("GB2312");
  jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
  jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
  jsize alen = env->GetArrayLength(barr);
  jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
  if (alen > 0) {
    rtn = (char*) malloc(alen + 1);
    memcpy(rtn, ba, alen);
    rtn[alen] = 0;
  }
  env->ReleaseByteArrayElements(barr, ba, 0);
  return rtn;
}

extern "C" void makeHiddenApiAccessable(JNIEnv *env,jstring classPath) {
  void *libart = v_dlopen("/system/lib/libart.so", RTLD_NOW);
  if (libart != NULL) {
    *(void **) (&toClass) = v_dlsym(libart, "_ZN3art16WellKnownClasses7ToClassEP7_jclass");
    *(void **) (&setClassLoader) =
        v_dlsym(libart, "_ZN3art6mirror5Class14SetClassLoaderENS_6ObjPtrINS0_11ClassLoaderEEE");
    jclass cls = env->FindClass(jstringToChar(env,classPath));
    ObjPtr op = toClass(cls);
    setClassLoader((void *) op.reference_, NULL);
    v_dlclose(libart);
  }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_guolei_support_1p_1native_PCompat_nativeHook(JNIEnv *env,
                                                      jobject instance,
                                                      jstring classPath_) {
  const char *classPath = env->GetStringUTFChars(classPath_, 0);

  makeHiddenApiAccessable(env,classPath_);

  env->ReleaseStringUTFChars(classPath_, classPath);
}