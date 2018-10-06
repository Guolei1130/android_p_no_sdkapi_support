//
// Created by vipkid on 2018/9/19.
//

#ifndef VK_HOOK_V_HOOK_H
#define VK_HOOK_V_HOOK_H

#include <stdlib.h>

#define _extern extern "C" __attribute__((__visibility__("default")))

#ifdef __cplusplus
extern "C" {
#endif

extern void *v_dlopen(const char *filename, int flags);
extern void *v_dlsym(void *handle, const char *symbol);
extern int v_dlclose(void *handle);
extern void v_hook_function(void *symbol, void *replace, void **result);

#ifdef __cplusplus
}
#endif

#endif

