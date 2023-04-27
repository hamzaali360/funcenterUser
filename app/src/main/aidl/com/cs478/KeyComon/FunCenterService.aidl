// FunCenterService.aidl
package com.cs478.KeyComon;

// Declare any non-default types here with import statements

interface FunCenterService {
    Bitmap getPicture(int pictureNumber);
    void playAudio(int audioNumber);
    void pauseAudio();
    void resumeAudio();
    void stopAudio();
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}