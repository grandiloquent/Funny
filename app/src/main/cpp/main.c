#include <memory.h>
#include "file.h"
#include <iconv.h>
#include <android/log.h>
#include <stdlib.h>
#include <jni.h>

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "hello-libs::", __VA_ARGS__))

int codeConvert(char *from_charset, char *to_charset, char *inbuf, size_t inlen, char *outbuf,
                size_t outlen) {
    iconv_t cd;
    char **pin = &inbuf;
    char **pout = &outbuf;

    cd = iconv_open(to_charset, from_charset);
    if (cd == 0) return -1;
    memset(outbuf, 0, outlen);
    if (iconv(cd, pin, &inlen, pout, &outlen) == -1) return -1;
    iconv_close(cd);
    return 0;
}

int u2g(char *inbuf, size_t inlen, char *outbuf, size_t outlen) {
    return codeConvert("utf-8", "gb2312", inbuf, inlen, outbuf, outlen);
}

int g2u(char *inbuf, size_t inlen, char *outbuf, size_t outlen) {
    return codeConvert("gb2312", "utf-8", inbuf, inlen, outbuf, outlen);
}

void renameMp3File(const char *path) {

    FILE *file = fopen(path, "rb");
    fseek(file, -125, SEEK_END);

    char *wt = malloc(30);
    char *wa = malloc(30);
    char *uwt = malloc(255);
    char *uwa = malloc(255);


    fread(wt, 1, 30, file);
    fread(wa, 1, 30, file);

    g2u(wt, strlen(wt), uwt, 255);
    g2u(wa, strlen(wa), uwa, 255);

//    char b[100];
//    for (int i = 0; i < 30; ++i) {
//        char j[10];
//        sprintf(j, "%d,", wa[i]);
//        strcat(b, j);
//    }

    char dir[PATH_MAX];
    strcpy(dir, path);
    substringBeforeLast(dir, '/', strlen(path));

    char t[256] = {0};
    strcat(t, dir);
    strcat(t, "/");
    strcat(t, uwt);
    strcat(t, " - ");
    strcat(t, uwa);
    strcat(t, ".mp3");
    t[-1] = '\0';


    rename(path, t);
    free(wa);
    free(wt);
    free(uwa);
    free(uwt);
    free(dir);
    fclose(file);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_funny_natives_NativeUtils_renameMp3File(JNIEnv *env, jclass type,
                                                             jstring fileName_) {
    const char *fileName = (*env)->GetStringUTFChars(env, fileName_, 0);

    renameMp3File(fileName);

    (*env)->ReleaseStringUTFChars(env, fileName_, fileName);
}