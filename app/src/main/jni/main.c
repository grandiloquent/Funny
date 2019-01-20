#include <jni.h>

JNIEXPORT void JNICALL
Java_euphoria_psycho_funny_natives_NativeUtils_renameMp3File(JNIEnv *env, jclass type,
                                                             jstring fileName_) {
    const char *fileName = (*env)->GetStringUTFChars(env, fileName_, 0);

    // TODO

    (*env)->ReleaseStringUTFChars(env, fileName_, fileName);
}