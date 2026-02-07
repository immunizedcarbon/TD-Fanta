package com.tdfanta.game.view.game

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.view.BaseGameFragment

class TowerBuildFragment : BaseGameFragment(), TowerSelector.TowerBuildView {
    private val mTowerSelector: TowerSelector

    private lateinit var mHandler: Handler

    private var mVisible = true
    private lateinit var mTowerViewControl: TowerViewControl

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mTowerSelector = factory.getTowerSelector()
    }

    override fun toggleTowerBuildView() {
        mHandler.post {
            if (mVisible) {
                hide()
            } else {
                show()
            }
        }
    }

    override fun hideTowerBuildView() {
        mHandler.post { hide() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mHandler = Handler(Looper.getMainLooper())

        val v = inflater.inflate(R.layout.fragment_tower_build, container, false)

        val towerViews = ArrayList<TowerView>()
        towerViews.add(v.findViewById(R.id.view_tower_1))
        towerViews.add(v.findViewById(R.id.view_tower_2))
        towerViews.add(v.findViewById(R.id.view_tower_3))
        towerViews.add(v.findViewById(R.id.view_tower_4))
        mTowerViewControl = TowerViewControl(towerViews)

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mTowerSelector.setTowerBuildView(this)
        hide()
    }

    override fun onDetach() {
        super.onDetach()

        mTowerViewControl.close()

        mTowerSelector.setTowerBuildView(null)
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun show() {
        if (!mVisible) {
            updateMenuTransparency()

            parentFragmentManager.beginTransaction()
                .show(this)
                .commitAllowingStateLoss()

            mVisible = true
        }
    }

    private fun hide() {
        if (mVisible) {
            parentFragmentManager.beginTransaction()
                .hide(this)
                .commitAllowingStateLoss()

            mVisible = false
        }
    }
}
