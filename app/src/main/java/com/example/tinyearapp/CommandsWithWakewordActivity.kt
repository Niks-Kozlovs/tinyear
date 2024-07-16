package com.example.tinyearapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tinyearlib.CommandCenter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandsWithWakewordActivity : AppCompatActivity() {
    private lateinit var commandCenter: CommandCenter
    private lateinit var speechStatusFragment: SpeechStatusFragment
    private lateinit var transcriptionFragment: TranscriptionFragment
    private lateinit var commandOutputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commands_with_wakeword)
        commandOutputTextView = findViewById(R.id.commandOutputTextView)
        initializeFragments()
        initializeCommandCenter()
    }

    private fun initializeFragments() {
        transcriptionFragment = TranscriptionFragment()
        speechStatusFragment = SpeechStatusFragment.newInstance("Okay phone")
        supportFragmentManager.beginTransaction()
            .replace(R.id.speechStatusFragmentContainer, speechStatusFragment)
            .replace(R.id.transcriptionFragmentContainer, transcriptionFragment)
            .commitAllowingStateLoss()
    }

    private fun initializeCommandCenter() {
        val modelPath = getFilePath("whisper-tiny-en.tflite")
        commandCenter = CommandCenter(
            modelPath,
            false,
            "okay phone",
            10,
            mapOf(
                "show time" to { showTime() },
                "say hello" to { sayHello() }
            ),
            timeUpdateCallback = { time ->
                runOnUiThread {
                    speechStatusFragment.updateTimeRemaining(time)
                }
            },
            isSpeakingUpdateCallback = { isSpeaking ->
                runOnUiThread {
                    speechStatusFragment.updateSpeakingStatus(isSpeaking)
                }
            },
            hypothesisUpdateCallback = { transcription ->
                runOnUiThread {
                    if (transcriptionFragment.isAdded) {
                        transcriptionFragment.updateTranscription(transcription)
                    }
                }
            }
        )

        commandCenter.startListening()
    }

    private fun sayHello() {
        Log.d("CommandsWithWakewordActivity", "Saying hello")
        runOnUiThread {
            commandOutputTextView.text = "Hello!"
        }
    }

    private fun showTime() {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        Log.d("CommandsWithWakewordActivity", "Current time: $currentTime")
        runOnUiThread {
            commandOutputTextView.text = "Current time: $currentTime"
        }
    }

    private fun getFilePath(assetName: String): String {
        val outfile = File(filesDir, assetName)
        return outfile.absolutePath
    }
}
