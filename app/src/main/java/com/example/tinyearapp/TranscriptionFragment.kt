package com.example.tinyearapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class TranscriptionFragment : Fragment() {

    private lateinit var transcriptionTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transcription, container, false)
        transcriptionTextView = view.findViewById(R.id.transcriptionTextView)
        return view
    }

    fun updateTranscription(text: String) {
        transcriptionTextView.append("$text\n")
    }
}
