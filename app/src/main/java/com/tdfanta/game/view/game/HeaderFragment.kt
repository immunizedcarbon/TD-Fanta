package com.tdfanta.game.view.game

import android.content.Intent
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
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameSpeed
import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.business.wave.WaveManager
import com.tdfanta.game.util.StringUtils
import com.tdfanta.game.view.BaseGameFragment

class HeaderFragment : BaseGameFragment(),
    WaveManager.Listener,
    ScoreBoard.Listener,
    GameSpeed.Listener,
    View.OnClickListener {
    private val mWaveManager: WaveManager
    private val mGameSpeed: GameSpeed
    private val mScoreBoard: ScoreBoard
    private val mTowerSelector: TowerSelector

    private lateinit var mHandler: Handler

    private lateinit var fragmentHeader: View

    private lateinit var txtCredits: TextView
    private lateinit var txtLives: TextView
    private lateinit var txtWave: TextView
    private lateinit var txtBonus: TextView

    private lateinit var btnNextWave: Button
    private lateinit var btnFastForwardSpeed: Button
    private lateinit var btnFastForwardActive: Button
    private lateinit var btnMenu: Button
    private lateinit var btnBuildTower: Button

    private lateinit var mTowerViewControl: TowerViewControl

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mScoreBoard = factory.getScoreBoard()
        mWaveManager = factory.getWaveManager()
        mGameSpeed = factory.getSpeedManager()
        mTowerSelector = factory.getTowerSelector()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mHandler = Handler(Looper.getMainLooper())

        val v = inflater.inflate(R.layout.fragment_header, container, false)

        fragmentHeader = v
        txtCredits = v.findViewById(R.id.txt_credits)
        txtLives = v.findViewById(R.id.txt_lives)
        txtWave = v.findViewById(R.id.txt_wave)
        txtBonus = v.findViewById(R.id.txt_bonus)

        btnNextWave = v.findViewById(R.id.btn_next_wave)
        btnFastForwardSpeed = v.findViewById(R.id.btn_fast_forward_speed)
        btnFastForwardActive = v.findViewById(R.id.btn_fast_forward_active)
        btnMenu = v.findViewById(R.id.btn_menu)
        btnBuildTower = v.findViewById(R.id.btn_build_tower)

        btnNextWave.setOnClickListener(this)
        btnFastForwardSpeed.setOnClickListener(this)
        btnFastForwardActive.setOnClickListener(this)
        btnMenu.setOnClickListener(this)
        btnBuildTower.setOnClickListener(this)
        fragmentHeader.setOnClickListener(this)

        btnNextWave.isEnabled = mWaveManager.isNextWaveReady()
        txtWave.text = getString(R.string.wave) + ": " + mWaveManager.getWaveNumber()
        txtCredits.text = getString(R.string.credits) + ": " + StringUtils.formatSuffix(mScoreBoard.getCredits())
        txtLives.text = getString(R.string.lives) + ": " + mScoreBoard.getLives()
        txtBonus.text =
            getString(R.string.bonus) + ": " +
            StringUtils.formatSuffix(mScoreBoard.getWaveBonus() + mScoreBoard.getEarlyBonus())
        btnFastForwardSpeed.text = getString(R.string.var_speed, mGameSpeed.fastForwardMultiplier())
        updateButtonFastForwardActive()

        val towerViews = ArrayList<TowerView>()
        towerViews.add(v.findViewById(R.id.view_tower_1))
        towerViews.add(v.findViewById(R.id.view_tower_2))
        towerViews.add(v.findViewById(R.id.view_tower_3))
        towerViews.add(v.findViewById(R.id.view_tower_4))
        mTowerViewControl = TowerViewControl(towerViews)

        fragmentHeader.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val lastTowerView = towerViews[towerViews.size - 1]
            val enoughSpace = lastTowerView.x + lastTowerView.width < btnMenu.x

            btnBuildTower.visibility = if (enoughSpace) View.INVISIBLE else View.VISIBLE
            for (towerView in towerViews) {
                towerView.visibility = if (enoughSpace) View.VISIBLE else View.INVISIBLE
            }
        }

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mWaveManager.addListener(this)
        mGameSpeed.addListener(this)
        mScoreBoard.addListener(this)
    }

    override fun onDetach() {
        super.onDetach()

        mTowerViewControl.close()

        mWaveManager.removeListener(this)
        mGameSpeed.removeListener(this)
        mScoreBoard.removeListener(this)

        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onClick(view: View) {
        if (view === fragmentHeader) {
            mTowerSelector.selectTower(null)
            return
        }

        if (view === btnNextWave) {
            mWaveManager.startNextWave()
            return
        }

        if (view === btnFastForwardSpeed) {
            mGameSpeed.cycleFastForward()
            return
        }

        if (view === btnFastForwardActive) {
            mGameSpeed.setFastForwardActive(!mGameSpeed.isFastForwardActive())
            return
        }

        if (view === btnMenu) {
            mTowerSelector.selectTower(null)
            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
            return
        }

        if (view === btnBuildTower) {
            mTowerSelector.toggleTowerBuildView()
            return
        }
    }

    override fun waveStarted() {
    }

    override fun waveNumberChanged() {
        mHandler.post {
            txtWave.text = getString(R.string.wave) + ": " + mWaveManager.getWaveNumber() +
                " (" + mWaveManager.getRemainingEnemiesCount() + ")"
        }
    }

    override fun nextWaveReadyChanged() {
        mHandler.post { btnNextWave.isEnabled = mWaveManager.isNextWaveReady() }
    }

    override fun remainingEnemiesCountChanged() {
        mHandler.post {
            txtWave.text = getString(R.string.wave) + ": " + mWaveManager.getWaveNumber() +
                " (" + mWaveManager.getRemainingEnemiesCount() + ")"
        }
    }

    override fun creditsChanged(credits: Int) {
        mHandler.post {
            txtCredits.text = getString(R.string.credits) + ": " + StringUtils.formatSuffix(credits)
        }
    }

    override fun livesChanged(lives: Int) {
        mHandler.post {
            txtLives.text = getString(R.string.lives) + ": " + lives
        }
    }

    override fun bonusChanged(waveBonus: Int, earlyBonus: Int) {
        mHandler.post {
            txtBonus.text = getString(R.string.bonus) + ": " + StringUtils.formatSuffix(waveBonus + earlyBonus)
        }
    }

    override fun gameSpeedChanged() {
        mHandler.post {
            btnFastForwardSpeed.text = getString(R.string.var_speed, mGameSpeed.fastForwardMultiplier())
            updateButtonFastForwardActive()
        }
    }

    private fun updateButtonFastForwardActive() {
        // Use the same resolved color as the adjacent menu button to avoid any style/state mismatch.
        btnFastForwardActive.setTextColor(btnMenu.currentTextColor)
    }
}
