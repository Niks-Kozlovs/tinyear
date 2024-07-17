package com.example.tinyearapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tinyearlib.TinySpeechRecognizer
import java.io.File
import java.io.IOException

class TranscribeAudioActivity : AppCompatActivity() {
    private lateinit var speechRecognizer: TinySpeechRecognizer
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transcribe_audio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeSpeechRecognizer()

        val waveFileName = arrayOf<String?>(null)
        val spinner = findViewById<Spinner>(R.id.spnrFiles)
        val btnTranscribe = findViewById<Button>(R.id.btnTranscribe)
        val tvTranscription = findViewById<TextView>(R.id.tvTranscription)
        progressBar = findViewById(R.id.progressBar)

        btnTranscribe.setOnClickListener {
            val wavFileName = waveFileName[0] ?: return@setOnClickListener
            tvTranscription.text = ""
            val wavFilePath = getFilePath(wavFileName)

            progressBar.visibility = View.VISIBLE

            // Transcribe the audio on seperate thread.
            Thread {
                val transcription = speechRecognizer.transcribeAudio(wavFilePath)

                runOnUiThread {
                    tvTranscription.text = transcription
                    progressBar.visibility = View.GONE
                    Log.d("TranscribeAudioActivity", "Transcription: $transcription")
                }
            }.start()
        }

        val files = getAssetFiles();
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, files)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent == null) return
                waveFileName[0] = files[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun getAssetFiles(): Array<String> {
        val files = ArrayList<String>()
        try {
            val assetFiles = assets.list("")
            for (file in assetFiles!!) {
                if (file.endsWith(".wav")) files.add(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return files.toTypedArray()
    }

    private fun getFilePath(assetName: String): String {
        val outfile = File(filesDir, assetName)
        return outfile.absolutePath
    }

    private fun initializeSpeechRecognizer() {
        val modelPath = getFilePath("whisper-tiny-en.tflite")
        speechRecognizer = TinySpeechRecognizer(modelPath, false)
    }
}
