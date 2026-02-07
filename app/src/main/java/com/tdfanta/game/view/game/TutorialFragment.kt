package com.tdfanta.game.view.game

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.R
import com.tdfanta.game.business.game.TutorialControl
import com.tdfanta.game.view.BaseGameFragment

class TutorialFragment : BaseGameFragment(), TutorialControl.TutorialView, View.OnClickListener {
    private val mControl: TutorialControl = TDFantaApplication.getInstance().getGameFactory().getTutorialControl()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var mVisible = true

    private lateinit var txtContent: TextView
    private lateinit var btnGotIt: Button
    private lateinit var btnSkip: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_tutorial, container, false)

        txtContent = view.findViewById(R.id.txt_content)
        btnGotIt = view.findViewById(R.id.btn_got_it)
        btnSkip = view.findViewById(R.id.btn_skip)

        btnGotIt.setOnClickListener(this)
        btnSkip.setOnClickListener(this)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hide()
        mControl.setView(this)
    }

    override fun onDetach() {
        super.onDetach()
        mControl.setView(null)
    }

    override fun onClick(v: View) {
        if (v === btnGotIt) {
            mControl.gotItClicked()
        }

        if (v === btnSkip) {
            mControl.skipClicked()
        }
    }

    override fun showHint(textId: Int, showSkipButton: Boolean) {
        mHandler.post { show(textId, showSkipButton) }
    }

    override fun tutorialFinished() {
        mHandler.post { hide() }
    }

    private fun show(textId: Int, showSkipButton: Boolean) {
        txtContent.setText(textId)
        btnSkip.visibility = if (showSkipButton) View.VISIBLE else View.GONE

        if (!mVisible) {
            parentFragmentManager.beginTransaction()
                .show(this@TutorialFragment)
                .commitAllowingStateLoss()

            mVisible = true
        }
    }

    private fun hide() {
        if (mVisible) {
            parentFragmentManager.beginTransaction()
                .hide(this@TutorialFragment)
                .commitAllowingStateLoss()

            mVisible = false
        }
    }
}
