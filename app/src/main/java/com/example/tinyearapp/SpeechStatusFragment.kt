package com.example.tinyearapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class SpeechStatusFragment : Fragment() {

    private lateinit var speakingStatusTextView: TextView
    private lateinit var wakewordTextView: TextView
    private lateinit var timeRemainingTextView: TextView

    companion object {
        private const val ARG_WAKEWORD = "wakeword"

        fun newInstance(wakeword: String?): SpeechStatusFragment {
            val fragment = SpeechStatusFragment()
            val args = Bundle()
            args.putString(ARG_WAKEWORD, wakeword)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_speech_status, container, false)
        speakingStatusTextView = view.findViewById(R.id.speakingStatusTextView)
        wakewordTextView = view.findViewById(R.id.wakewordTextView)
        timeRemainingTextView = view.findViewById(R.id.timeRemainingTextView)

        arguments?.getString(ARG_WAKEWORD)?.let {
            wakewordTextView.text = it
            wakewordTextView.visibility = View.VISIBLE
            timeRemainingTextView.visibility = View.VISIBLE
        }

        return view
    }

    fun updateSpeakingStatus(isSpeaking: Boolean) {
        if (isAdded) {
            speakingStatusTextView.text = if (isSpeaking) "Speaking" else "Not Speaking"
            speakingStatusTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSpeaking) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateTimeRemaining(time: Int) {
        if (isAdded) {
            wakewordTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (time > 0) android.R.color.holo_green_dark else android.R.color.black
                )
            )
            timeRemainingTextView.text = "${time}s"
        }
    }
}
