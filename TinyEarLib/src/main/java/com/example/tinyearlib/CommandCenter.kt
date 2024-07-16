package com.example.tinyearlib

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.fixedRateTimer

class CommandCenter(
    modelPath: String,
    isMultilingual: Boolean,
    private val wakeWord: String? = null,
    private val wakeWordTimeout: Int = 10,
    initialCommands: Map<String, () -> Unit> = emptyMap(),
    private val timeUpdateCallback: ((Int) -> Unit)? = null,
    private val hypothesisUpdateCallback: ((String) -> Unit)? = null,
    private val isSpeakingUpdateCallback: ((Boolean) -> Unit)? = null
) {
    private val recognizer: TinySpeechRecognizer
    private val commands: ConcurrentHashMap<String, () -> Unit> = ConcurrentHashMap()
    private var wakeWordDetected = false
    private var remainingWakeWordTime = wakeWordTimeout
    private var wakeWordTimer: Timer? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        recognizer = TinySpeechRecognizer(modelPath, isMultilingual, object : HypothesisListener {
            override fun onNewHypothesis(hypothesis: String) {
                hypothesisUpdateCallback?.let { it(hypothesis) }
                val cleanedHypothesis = hypothesis.replace("[^a-zA-Z]+".toRegex(), " ").trim()
                handleHypothesis(cleanedHypothesis)
            }

            override fun onIsSpeaking(isSpeaking: Boolean) {
                isSpeakingUpdateCallback?.let { it(isSpeaking) }
            }
        })
        commands.putAll(initialCommands)
    }

    fun startListening(timeout: Int = -1): Boolean {
        return recognizer.startListening(timeout)
    }

    fun stopListening() {
        recognizer.stopListening()
        stopWakeWordTimer()
    }

    fun registerCommand(command: String, action: () -> Unit) {
        commands[command] = action
    }

    fun registerCommands(newCommands: Map<String, () -> Unit>) {
        commands.putAll(newCommands)
    }

    private fun handleHypothesis(hypothesis: String) {
        Log.d("CommandCenter", "Hypothesis received: $hypothesis")

        if (wakeWord == null) {
            executeCommand(hypothesis)
        } else if (hypothesis.contains(wakeWord, ignoreCase = true)) {
            wakeWordDetected = true
            remainingWakeWordTime = wakeWordTimeout
            startWakeWordTimer()
            executeCommand(hypothesis)
            Log.d("CommandCenter", "Wake word detected: $wakeWord")
        } else if (wakeWordDetected) {
            executeCommand(hypothesis)
        }
    }

    private fun startWakeWordTimer() {
        stopWakeWordTimer()
        wakeWordTimer = fixedRateTimer(name = "WakeWordTimer", initialDelay = 1000L, period = 1000L) {
            remainingWakeWordTime--
            handler.post {
                timeUpdateCallback?.invoke(remainingWakeWordTime)
            }
            if (remainingWakeWordTime <= 0) {
                wakeWordDetected = false
                stopWakeWordTimer()
                handler.post {
                    timeUpdateCallback?.invoke(remainingWakeWordTime)
                }
            }
        }
    }

    private fun stopWakeWordTimer() {
        wakeWordTimer?.cancel()
        wakeWordTimer = null
    }

    private fun executeCommand(hypothesis: String) {
        val command = commands.keys.firstOrNull { hypothesis.contains(it, ignoreCase = true) }

        command?.let {
            Log.d("CommandCenter", "Executing command: $it")
            commands[it]?.invoke()
        }
    }
}
