package com.example.eventattendance.util

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFeedbackUtil @Inject constructor() {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            // Audio hardware might be unavailable on some devices/emulators
        }
    }

    fun playBeepSuccess() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            // Ignore tone failure
        }
    }

    fun playBeepError() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
        } catch (e: Exception) {
            // Ignore tone failure
        }
    }
}
