


[Android P 调用隐藏API限制原理](https://mp.weixin.qq.com/s/sktB0x5yBexkn4ORQ1YofA)

[突破Android P(Preview 1)对调用隐藏API限制的方法](https://mp.weixin.qq.com/s/4k3DBlxlSO2xNNKqjqUdaQ)

[java_lang_Class.cc](https://android.googlesource.com/platform/art/+/android-9.0.0_r5/runtime/native/java_lang_Class.cc)

### 前言

Android P对非SDK API的使用做了限制，导致在Android P上会出现一些状况。在很早前预览版本刚出来的时候，360团队就出了两篇文章。
[Android P 调用隐藏API限制原理](https://mp.weixin.qq.com/s/sktB0x5yBexkn4ORQ1YofA) 以及
[突破Android P(Preview 1)对调用隐藏API限制的方法](https://mp.weixin.qq.com/s/4k3DBlxlSO2xNNKqjqUdaQ)

限制方式三种：
* 反射
* 直接调用
* jni调用

这一篇文章就是根据上面的文章来的。

### 方法一(不建议)

使用Provided(CompileOnly)的方式去解决调用限制，只能解决反射调用的问题，而无法解决直接调用或者jni调用的方式。不建议使用

### 方法二(不建议)

这个方法二对应的是360文章中的方法三。主要代码如下。

```
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
```

其中，fake_dlopen、fake_dlsym 使用的是[Nougat_dlfunctions](https://github.com/Guolei1130/Nougat_dlfunctions),主要是Android 7.0以上对dlopen、dlsym等函数做了限制。因此用这个库。而MSHookFunction，则是大名鼎鼎的[cydiasubstrate](http://www.cydiasubstrate.com/)。


![](https://user-gold-cdn.xitu.io/2018/9/18/165ecc7a0f8e1e81?w=2676&h=332&f=png&s=339816)

上面的代码只解决了反射方法的问题。我按照这种思路去解决字段问题的时候发现。

![](https://user-gold-cdn.xitu.io/2018/9/18/165ecca42fa2c6b3?w=1572&h=878&f=png&s=266460)

![](https://user-gold-cdn.xitu.io/2018/9/18/165eccb2c819c718?w=1584&h=664&f=png&s=162824)

GetDeclaredField是inline的，无法切入。而CreateFromArtField又是hidden的，也不好切入。


![](https://user-gold-cdn.xitu.io/2018/9/18/165eccc2dd9c9f9b?w=1314&h=244&f=png&s=92329)

因此，放弃了这种方法。

### 方法三(可用，但是有更好的)

这里对应的方法三，对应的是360文章中的方法二，也就是修改classloader的方式。代码如下。

```
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

```

没错，代码就是这么点。这样，我们就可以在ReflectionHelper中调用非公开API了。但是这里会依赖Nougat_dlfunctions这个库。


![](https://user-gold-cdn.xitu.io/2018/9/18/165ecd34c14fdca3?w=2236&h=130&f=png&s=105115)
![](https://user-gold-cdn.xitu.io/2018/9/18/165ecd3611c6099a?w=1482&h=110&f=png&s=90357)

### 方法四(超级好)

既然是修改classloader，那么我们为什么不在java层修改呢。代码如下。

```
  private void testJavaPojie() {
    try {
      Class reflectionHelperClz = Class.forName("com.example.support_p.ReflectionHelper");
      Class classClz = Class.class;
      Field classLoaderField = classClz.getDeclaredField("classLoader");
      classLoaderField.setAccessible(true);
      classLoaderField.set(reflectionHelperClz, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
```

而这里用的相关反射只是light级别的，没有什么影响。反而代码量超小，也不依赖其他。

### 方法五(超级好+1)

这个方案来自 @区长 大神

方法四还是存在一点问题。如果以后把classloader加入到深灰或者黑名单，那就僵硬了。所以，我们不用反射，直接用unsafe去修改。代码这就不贴了。为了得到classloader的偏移量，我们写一个和Class结构一样的类，用这个类得到的classLoader的偏移量和Class是一样的。


### 注意：

如果我们用修改ClassLoader的方式的话，那么ReflectionHelper类中只能反射调用非公开API，注意了。

[代码在这里，觉得好的给个star吧](https://github.com/Guolei1130/android_p_no_sdkapi_support)
