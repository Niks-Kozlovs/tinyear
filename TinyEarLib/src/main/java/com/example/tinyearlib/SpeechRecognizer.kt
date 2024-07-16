package com.example.tinyearlib

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.util.Log
import com.konovalov.vad.webrtc.VadWebRTC
import com.konovalov.vad.webrtc.config.FrameSize
import com.konovalov.vad.webrtc.config.Mode
import com.konovalov.vad.webrtc.config.SampleRate
import java.io.IOException
import kotlin.math.roundToInt

private const val WHISPER_SAMPLE_RATE = 16000
private const val BUFFER_SIZE_SECONDS = 0.25f
private const val NO_TIMEOUT = -1

class TinySpeechRecognizer(
    modelPath: String,
    isMultilingual: Boolean,
    private val listener: HypothesisListener? = null
) {
    private val bufferSize = (WHISPER_SAMPLE_RATE * BUFFER_SIZE_SECONDS).roundToInt()
    private var recognizerThread: RecognizerThread? = null

    @SuppressLint("MissingPermission")
    private val recorder: AudioRecord = AudioRecord(
        AudioSource.VOICE_RECOGNITION,
        WHISPER_SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )

    private val whisperEngine: WhisperEngine = WhisperEngine()

    init {
        if (recorder.state == AudioRecord.STATE_UNINITIALIZED) {
            recorder.release()
            throw IOException("Failed to initialize recorder")
        }

        whisperEngine.initialize(modelPath, isMultilingual)
    }

    fun startListening(timeout: Int = NO_TIMEOUT): Boolean {
        if (recognizerThread != null) {
            return false
        }

        val thread = RecognizerThread(recorder, whisperEngine, bufferSize, timeout, listener)
        recognizerThread = thread
        thread.start()
        return true
    }

    fun stopListening() {
        recognizerThread?.interrupt()
        recognizerThread = null
    }

    fun transcribeAudio(audioFileLocation: String): String {
        return whisperEngine.transcribeFile(audioFileLocation)
    }

    private class RecognizerThread(
        private val recorder: AudioRecord,
        private val whisperEngine: WhisperEngine,
        private val bufferSize: Int,
        timeout: Int = NO_TIMEOUT,
        private val listener: HypothesisListener? // Add listener parameter
    ) : Thread() {
        private var remainingSamples: Int = 0
        private var timeoutSamples: Int = if (timeout != NO_TIMEOUT) timeout * WHISPER_SAMPLE_RATE / 1000 else NO_TIMEOUT
        private var vad = VadWebRTC(sampleRate = SampleRate.SAMPLE_RATE_16K, frameSize = FrameSize.FRAME_SIZE_160, mode = Mode.AGGRESSIVE)

        override fun run() {
            if (!whisperEngine.isInitialized()) {
                throw RuntimeException("Model not initialized")
            }

            recorder.startRecording()
            if (recorder.recordingState == AudioRecord.RECORDSTATE_STOPPED) {
                recorder.stop()
                return
            }

            var voiceBuffer = ShortArray(bufferSize)

            val hasTimedOut = timeoutSamples != NO_TIMEOUT && remainingSamples <= 0
            while (!interrupted() && !hasTimedOut) {
                val recorderBuffer = ShortArray(bufferSize)
                val nread = recorder.read(recorderBuffer, 0, recorderBuffer.size)

                if (-1 == nread) {
                    throw RuntimeException("error reading audio buffer")
                } else if (nread > 0) {
                    val isSpeaking = vad.isSpeech(recorderBuffer)

                    listener?.onIsSpeaking(isSpeaking)

                    if (isSpeaking && timeoutSamples != NO_TIMEOUT) {
                        remainingSamples = timeoutSamples
                    }

                    if (isSpeaking) {
                        voiceBuffer = shortArrayOf(*voiceBuffer, *recorderBuffer)
                        continue
                    }

                    if (voiceBuffer.size == bufferSize) {
                        voiceBuffer = recorderBuffer
                        continue
                    }

                    val floatBuffer = floatMe(voiceBuffer)

                    val hypothesis = whisperEngine.transcribeBuffer(floatBuffer)
                    voiceBuffer = ShortArray(bufferSize)

                    listener?.onNewHypothesis(hypothesis)

                    Log.d("SpeechRecognizer", hypothesis)
                }

                if (timeoutSamples != NO_TIMEOUT) {
                    remainingSamples -= nread
                }
            }

            recorder.stop()
        }

        private fun floatMe(array: ShortArray): FloatArray {
            val floats = FloatArray(array.size)
            array.forEachIndexed { index, sh ->
                // The input must be normalized to floats between -1 and +1.
                // To normalize it, we just need to divide all the values by 2**16 or in our code, MAX_ABS_INT16 = 32768
                floats[index] = sh.toFloat() / 32768.0f

            }
            return floats
        }

    }
}

interface HypothesisListener {
    fun onNewHypothesis(hypothesis: String)
    fun onIsSpeaking(isSpeaking: Boolean)
}
