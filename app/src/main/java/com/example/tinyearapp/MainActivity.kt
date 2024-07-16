package com.example.tinyearapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tinyearapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions(this)

        val extensionsToCopy = arrayOf("pcm", "bin", "wav", "tflite")
        copyAssetsWithExtensionsToDataFolder(this, extensionsToCopy)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonTranscribeAudio.setOnClickListener {
            startActivity(Intent(this@MainActivity, TranscribeAudioActivity::class.java))
        }

        binding.buttonLiveTranscribe.setOnClickListener {
            startActivity(Intent(this@MainActivity, LiveTranscribeActivity::class.java))
        }

        binding.buttonLiveCommands.setOnClickListener {
            startActivity(Intent(this@MainActivity, LiveCommandsActivity::class.java))
        }

        binding.buttonCommandsWakeword.setOnClickListener {
            startActivity(Intent(this@MainActivity, CommandsWithWakewordActivity::class.java))
        }
    }

    private fun copyAssetsWithExtensionsToDataFolder(context: Context, extensions: Array<String>) {
        val assetManager = context.assets
        try {
            val destFolder = context.filesDir.absolutePath

            for (extension in extensions) {
                val assetFiles = assetManager.list("")
                for (assetFileName in assetFiles!!) {
                    if (assetFileName.endsWith(".$extension")) {
                        val outFile = File(destFolder, assetFileName)
                        if (outFile.exists()) continue

                        val inputStream = assetManager.open(assetFileName)
                        val outputStream: OutputStream = FileOutputStream(outFile)

                        val buffer = ByteArray(1024)
                        var read: Int
                        while ((inputStream.read(buffer).also { read = it }) != -1) {
                            outputStream.write(buffer, 0, read)
                        }

                        inputStream.close()
                        outputStream.flush()
                        outputStream.close()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun checkAndRequestPermissions(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), 1)
        }
    }
}
