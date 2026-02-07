package com.tdfanta.game.view.game

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.view.BaseGameFragment
import java.text.DecimalFormat

class GameOverFragment : BaseGameFragment(), GameState.Listener {
    private val mGameState: GameState

    private lateinit var mHandler: Handler

    private lateinit var txtScore: TextView

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mGameState = factory.getGameState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val v = inflater.inflate(R.layout.fragment_game_over, container, false)

        txtScore = v.findViewById(R.id.txt_score)

        mHandler = Handler(Looper.getMainLooper())

        updateScore()

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mGameState.addListener(this)

        if (!mGameState.isGameOver()) {
            parentFragmentManager.beginTransaction()
                .hide(this)
                .commit()
        }
    }

    override fun onDetach() {
        super.onDetach()

        mGameState.removeListener(this)
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun gameRestart() {
        mHandler.post {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(this@GameOverFragment)
                .commitAllowingStateLoss()
        }
    }

    override fun gameOver() {
        mHandler.post {
            updateScore()

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .show(this@GameOverFragment)
                .commitAllowingStateLoss()
        }
    }

    private fun updateScore() {
        val fmt = DecimalFormat("###,###,###,###")
        txtScore.text = resources.getString(R.string.score) + ": " + fmt.format(mGameState.getFinalScore())
    }
}
