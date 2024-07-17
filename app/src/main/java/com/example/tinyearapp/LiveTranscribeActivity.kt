package com.example.tinyearapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tinyearlib.HypothesisListener
import com.example.tinyearlib.TinySpeechRecognizer
import java.io.File

class LiveTranscribeActivity : AppCompatActivity() {
    private lateinit var speechRecognizer: TinySpeechRecognizer
    private lateinit var startStopButton: Button
    private var isListening = false
    private lateinit var speechStatusFragment: SpeechStatusFragment
    private lateinit var transcriptionFragment: TranscriptionFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_transcribe)

        startStopButton = findViewById(R.id.startStopButton)

        val wakeword = intent.getStringExtra("wakeword")
        speechStatusFragment = SpeechStatusFragment.newInstance(wakeword)
        transcriptionFragment = TranscriptionFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.speechStatusFragmentContainer, speechStatusFragment)
            .replace(R.id.transcriptionFragmentContainer, transcriptionFragment)
            .commitAllowingStateLoss()

        initializeSpeechRecognizer()

        startStopButton.setOnClickListener {
            if (isListening) {
                speechRecognizer.stopListening()
                startStopButton.text = "Start Recording"
            } else {
                speechRecognizer.startListening()
                startStopButton.text = "Stop Recording"
            }
            isListening = !isListening
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.stopListening()
    }

    private fun initializeSpeechRecognizer() {
        val modelPath = getFilePath("whisper-tiny-en.tflite")
        speechRecognizer = TinySpeechRecognizer(modelPath, false, object : HypothesisListener {
            override fun onNewHypothesis(hypothesis: String) {
                runOnUiThread {
                    transcriptionFragment.updateTranscription(hypothesis)
                }
            }

            override fun onIsSpeaking(isSpeaking: Boolean) {
                runOnUiThread {
                    speechStatusFragment.updateSpeakingStatus(isSpeaking)
                }
            }
        })
    }

    private fun getFilePath(assetName: String): String {
        val outfile = File(filesDir, assetName)
        return outfile.absolutePath
    }
}
