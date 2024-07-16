package com.example.tinyearlib

class WhisperEngine {
    private val nativePtr: Long
    private var isInitialized = false
    init {
        System.loadLibrary("audioEngine")
        nativePtr = createTFLiteEngine()
    }

    fun isInitialized(): Boolean {
        return isInitialized
    }

    fun initialize(modelPath: String, isMultilingual: Boolean): Boolean {
        loadModel(nativePtr, modelPath, isMultilingual)
        this.isInitialized = true
        return true
    }

    fun transcribeBuffer(samples: FloatArray): String {
        return this.transcribeBuffer(nativePtr, samples)
    }



    fun transcribeFile(waveFile: String): String {
        return this.transcribeFile(nativePtr, waveFile)
    }



    private external fun createTFLiteEngine(): Long

    external fun loadModel(nativePtr: Long, modelPath: String, isMultilingual: Boolean): Int

    external fun freeModel(nativePtr: Long)

    private external fun transcribeBuffer(nativePtr: Long, samples: FloatArray): String

    private external fun transcribeFile(nativePtr: Long, waveFile: String): String
}
