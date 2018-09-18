#include <jni.h>
#include <string>
#include <android/log.h>

#include <unistd.h>
#include <dlfcn.h>
#include "substrate.h"

#define TAG_NAME        "test2:test_cplusplus"
#define log_error(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG_NAME, (const char *) fmt, ##args)
#define log_info(fmt, args...) __android_log_print(ANDROID_LOG_INFO, TAG_NAME, (const char *) fmt, ##args)

extern "C" {
extern void *fake_dlopen(const char *filename, int flags);
extern void *fake_dlsym(void *handle, const char *symbol);
extern void fake_dlclose(void *handle);
extern void MSHookFunction(void *symbol, void *replace, void **result);
}

extern "C" {
int hookForP();
int hookForPMethod();
int hookForPField();

void makeHiddenApiAccessable(JNIEnv *env);
}


extern "C" JNIEXPORT jstring
JNICALL
Java_com_example_support_1p_MainActivity_stringFromJNI(
    JNIEnv *env,
    jobject /* this */) {
  std::string hello = "Hello from C++";
//  makeHiddenApiAccessable(env);
  return env->NewStringUTF(hello.c_str());
}

extern "C" int hookForP() {
  hookForPMethod();
  hookForPField();
  return 0;
}

class ObjPtr {
 public:
  uintptr_t reference_;
};

ObjPtr
(*sys_GetDeclaredMethodInternal)(void *self, jobject kclass, jstring name, jobjectArray args);

void *(*executableGetArtMethod)(void *ex);

ObjPtr myGetDeclaredMethodInternal(void *self, jobject kclass, jstring name, jobjectArray args) {
  ObjPtr res = sys_GetDeclaredMethodInternal(self, kclass, name, args);
  if (res.reference_ != 0) {
    void *pMethod = executableGetArtMethod((void *) (res.reference_));
    reinterpret_cast<uint32_t *>(pMethod)[1] &= 0xcfffffff;
  }
  return res;
}


extern "C" int hookForPMethod() {
  void *libc = fake_dlopen("/system/lib/libart.so", RTLD_NOW);
  if (libc != NULL) {
    void *p = fake_dlsym(libc, "_ZN3art6mirror5Class25GetDeclaredMethodInternalILNS_11Poin"
        "terSizeE4ELb0EEENS_6ObjPtrINS0_6MethodEEEPNS_6ThreadENS4_IS1_EENS4_INS0_6StringEEEN"
        "S4_INS0_11ObjectArrayIS1_EEEE");
    if (p != NULL) {
      MSHookFunction(p,
                     reinterpret_cast<void *>(myGetDeclaredMethodInternal),
                     reinterpret_cast<void **>(&sys_GetDeclaredMethodInternal));
    }
    *(void **) (&executableGetArtMethod) =
        fake_dlsym(libc, "_ZN3art6mirror10Executable12GetArtMethodEv");
    fake_dlclose(libc);

  } //if

  return 1;
}


void *(*sys_CreateFromArtField)(void *, void *, bool);

void *myCreateFromArtField(void *self, void *field, bool force_resolve) {
  log_error("=================== %s", "myCreateFromArtField");
  void *result = sys_CreateFromArtField(self, field, force_resolve);
  return result;
}

// hidden的，方案不靠谱
extern "C" int hookForPField() {
  void *libart = fake_dlopen("/system/lib/libart.so", RTLD_NOW);
  if (libart != NULL) {
    // CreateFromArtMethod这个方法是hidden的，找不到，方案三field的切入点找不到
    void *CreateFromArtField = fake_dlsym(libart,
                                          "_ZN3art6mirror5Field18CreateFromArtFieldILNS_11PointerSizeE8ELb1EEEPS1_PNS_6ThreadEPNS_8ArtFieldEb");
    if (CreateFromArtField != NULL) {
      MSHookFunction(CreateFromArtField,
                     reinterpret_cast<void *>(myCreateFromArtField),
                     reinterpret_cast<void **>(&sys_CreateFromArtField));
      log_error("hook field %p,%p", myCreateFromArtField, sys_CreateFromArtField);
    } else {
      log_error("p is null");
    }
    fake_dlclose(libart);

  } //if

  return 1;
}

void (*setClassLoader)(void *pClass, void *new_cl);
ObjPtr (*toClass)(jclass global_jclss);

extern "C" void makeHiddenApiAccessable(JNIEnv *env) {
  void *libart = fake_dlopen("/system/lib/libart.so", RTLD_NOW);
  if (libart != NULL) {
    *(void **) (&toClass) = fake_dlsym(libart, "_ZN3art16WellKnownClasses7ToClassEP7_jclass");
    *(void **) (&setClassLoader) =
        fake_dlsym(libart, "_ZN3art6mirror5Class14SetClassLoaderENS_6ObjPtrINS0_11ClassLoaderEEE");
    jclass cls = env->FindClass("com/example/support_p/ReflectionHelper");
    ObjPtr op = toClass(cls);
    setClassLoader((void *) op.reference_, NULL);
  }
}

