#include <jni.h>
#include <vector>

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_aihealthassistant_MainActivity_processSymptoms(
        JNIEnv* env,
        jobject /* this */,
        jintArray input) {

    jsize len = env->GetArrayLength(input);
    jint* body = env->GetIntArrayElements(input, nullptr);

    std::vector<jint> processed(len);
    for (jsize i = 0; i < len; i++) {
        // Validate values (only allow 0 or 1), replace invalid with 0
        if (body[i] == 1 || body[i] == 0) {
            processed[i] = body[i];
        } else {
            processed[i] = 0;
        }
    }

    env->ReleaseIntArrayElements(input, body, JNI_ABORT);

    jintArray result = env->NewIntArray(len);
    env->SetIntArrayRegion(result, 0, len, processed.data());

    return result;
}
