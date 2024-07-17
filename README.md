# TinyEar

TinyEar is an android library that allows for offline speech recognition to be added to any app. The API is simple to use and usage can be seen in the example app.

## Features
This library has a few simple features that can be useful:
1. Transcription from audio files
2. Live transcribe from microphone
3. Voice commands
4. Voice commands with a wakeword
5. Status updates when the user is speaking.

## Requirements
If you want to use a different sized whisper model for better accuracy then you can generate them [here](https://colab.research.google.com/github/nyadla-sys/whisper.tflite/blob/main/models/generate_tflite_from_whisper.ipynb). Change the string: `openai/whisper-tiny.en` string to the one you need before generating the model.

Available options: tiny, base, small, medium, large. For english versions add .en to the end (does not work for large model.

This library requires access to the microphone if you want to use live transcription or voice commands. Make sure to add `<uses-permission android:name="android.permission.RECORD_AUDIO" />` to AndroidManifest.xml and request for permission before starting live transcription.


