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
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.tower.TowerControl
import com.tdfanta.game.business.tower.TowerInfo
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.entity.tower.TowerInfoValue
import com.tdfanta.game.entity.tower.TowerStrategy
import com.tdfanta.game.util.StringUtils
import com.tdfanta.game.view.BaseGameFragment
import java.text.DecimalFormat

class TowerInfoFragment : BaseGameFragment(), View.OnClickListener, TowerSelector.TowerInfoView {
    private val mTowerSelector: TowerSelector
    private val mTowerControl: TowerControl

    private lateinit var mHandler: Handler

    private lateinit var txtLevel: TextView
    private val txtProperty = arrayOfNulls<TextView>(6)
    private val txtPropertyText = arrayOfNulls<TextView>(6)

    private lateinit var btnStrategy: Button
    private lateinit var btnLockTarget: Button
    private lateinit var btnEnhance: Button
    private lateinit var btnUpgrade: Button
    private lateinit var btnSell: Button

    private var mVisible = true

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mTowerSelector = factory.getTowerSelector()
        mTowerControl = factory.getTowerControl()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_tower_info, container, false)

        txtLevel = v.findViewById(R.id.txt_level)
        txtProperty[0] = v.findViewById(R.id.txt_property1)
        txtProperty[1] = v.findViewById(R.id.txt_property2)
        txtProperty[2] = v.findViewById(R.id.txt_property3)
        txtProperty[3] = v.findViewById(R.id.txt_property4)
        txtProperty[4] = v.findViewById(R.id.txt_property5)
        txtProperty[5] = v.findViewById(R.id.txt_property6)

        val txtLevelText: TextView = v.findViewById(R.id.txt_level_text)
        txtLevelText.text = resources.getString(R.string.level) + ":"

        txtPropertyText[0] = v.findViewById(R.id.txt_property_text1)
        txtPropertyText[1] = v.findViewById(R.id.txt_property_text2)
        txtPropertyText[2] = v.findViewById(R.id.txt_property_text3)
        txtPropertyText[3] = v.findViewById(R.id.txt_property_text4)
        txtPropertyText[4] = v.findViewById(R.id.txt_property_text5)
        txtPropertyText[5] = v.findViewById(R.id.txt_property_text6)

        btnStrategy = v.findViewById(R.id.btn_strategy)
        btnLockTarget = v.findViewById(R.id.btn_lock_target)
        btnUpgrade = v.findViewById(R.id.btn_upgrade)
        btnEnhance = v.findViewById(R.id.btn_enhance)
        btnSell = v.findViewById(R.id.btn_sell)

        btnStrategy.setOnClickListener(this)
        btnLockTarget.setOnClickListener(this)
        btnEnhance.setOnClickListener(this)
        btnUpgrade.setOnClickListener(this)
        btnSell.setOnClickListener(this)

        mHandler = Handler(Looper.getMainLooper())

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val towerInfo = mTowerSelector.getTowerInfo()

        if (towerInfo != null) {
            refresh(towerInfo)
            show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mTowerSelector.setTowerInfoView(this)
        hide()
    }

    override fun onDetach() {
        super.onDetach()

        mTowerSelector.setTowerInfoView(null)
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onClick(v: View) {
        if (v === btnStrategy) {
            mTowerControl.cycleTowerStrategy()
            return
        }

        if (v === btnLockTarget) {
            mTowerControl.toggleLockTarget()
            return
        }

        if (v === btnEnhance) {
            mTowerControl.enhanceTower()
            return
        }

        if (v === btnUpgrade) {
            mTowerControl.upgradeTower()
            return
        }

        if (v === btnSell) {
            mTowerControl.sellTower()
            return
        }
    }

    override fun showTowerInfo(towerInfo: TowerInfo) {
        mHandler.post {
            show()
            refresh(towerInfo)
        }
    }

    override fun hideTowerInfo() {
        mHandler.post { hide() }
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

    private fun refresh(towerInfo: TowerInfo) {
        val fmt = DecimalFormat()
        val level = fmt.format(towerInfo.getLevel()) + " / " + fmt.format(towerInfo.getLevelMax())
        txtLevel.text = level

        val properties: List<TowerInfoValue> = towerInfo.getProperties()
        for (i in properties.indices) {
            val property = properties[i]
            txtPropertyText[i]!!.text = getString(property.getTextId()) + ":"
            txtProperty[i]!!.text = StringUtils.formatSuffix(property.getValue())
        }
        for (i in properties.size until txtProperty.size) {
            txtPropertyText[i]!!.text = ""
            txtProperty[i]!!.text = ""
        }

        if (towerInfo.getEnhanceCost() > 0) {
            btnEnhance.text = StringUtils.formatSwitchButton(
                getString(R.string.enhance),
                StringUtils.formatSuffix(towerInfo.getEnhanceCost()),
            )
        } else {
            btnEnhance.text = getString(R.string.enhance)
        }

        if (towerInfo.getUpgradeCost() > 0) {
            btnUpgrade.text = StringUtils.formatSwitchButton(
                getString(R.string.upgrade),
                StringUtils.formatSuffix(towerInfo.getUpgradeCost()),
            )
        } else {
            btnUpgrade.text = getString(R.string.upgrade)
        }

        btnSell.text = StringUtils.formatSwitchButton(
            getString(R.string.sell),
            StringUtils.formatSuffix(towerInfo.getValue()),
        )

        btnUpgrade.isEnabled = towerInfo.isUpgradeable()
        btnEnhance.isEnabled = towerInfo.isEnhanceable()
        btnSell.isEnabled = towerInfo.isSellable()

        if (towerInfo.canLockTarget()) {
            btnLockTarget.text = StringUtils.formatSwitchButton(
                getString(R.string.lock_target),
                StringUtils.formatBoolean(towerInfo.doesLockTarget(), resources),
            )
            btnLockTarget.isEnabled = true
        } else {
            btnLockTarget.text = getString(R.string.lock_target)
            btnLockTarget.isEnabled = false
        }

        if (towerInfo.hasStrategy()) {
            btnStrategy.text = StringUtils.formatSwitchButton(
                getString(R.string.strategy),
                getStrategyString(towerInfo.getStrategy()!!),
            )
            btnStrategy.isEnabled = true
        } else {
            btnStrategy.text = getString(R.string.strategy)
            btnStrategy.isEnabled = false
        }
    }

    private fun getStrategyString(strategy: TowerStrategy): String {
        return when (strategy) {
            TowerStrategy.Closest -> getString(R.string.strategy_closest)
            TowerStrategy.Weakest -> getString(R.string.strategy_weakest)
            TowerStrategy.Strongest -> getString(R.string.strategy_strongest)
            TowerStrategy.First -> getString(R.string.strategy_first)
            TowerStrategy.Last -> getString(R.string.strategy_last)
        }
    }
}
