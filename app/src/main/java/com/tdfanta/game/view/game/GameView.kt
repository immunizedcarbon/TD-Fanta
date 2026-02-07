package com.tdfanta.game.view.game

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.business.tower.TowerInserter
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.engine.render.Renderer
import com.tdfanta.game.engine.render.Viewport
import com.tdfanta.game.util.math.Vector2

class GameView(context: Context, attrs: AttributeSet) : View(context, attrs), View.OnDragListener, View.OnTouchListener {
    private val mViewport: Viewport?
    private val mRenderer: Renderer?
    private val mTowerSelector: TowerSelector?
    private val mTowerInserter: TowerInserter?

    init {
        if (!isInEditMode) {
            val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
            mViewport = factory.getViewport()
            mRenderer = factory.getRenderer()
            mTowerSelector = factory.getTowerSelector()
            mTowerInserter = factory.getTowerInserter()

            mRenderer.setView(this)
        } else {
            mViewport = null
            mRenderer = null
            mTowerSelector = null
            mTowerInserter = null
        }

        isFocusable = true
        setOnDragListener(this)
        setOnTouchListener(this)
    }

    fun close() {
        mRenderer!!.setView(null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (!isInEditMode) {
            mViewport!!.setScreenSize(w, h)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isInEditMode) {
            mRenderer!!.draw(canvas)
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val pos = mViewport!!.screenToGame(Vector2(event.x, event.y))
            mTowerSelector!!.selectTowerAt(pos)
            return true
        }

        return false
    }

    override fun onDrag(view: View, event: DragEvent): Boolean {
        return when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> true

            DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION -> {
                val pos = mViewport!!.screenToGame(Vector2(event.x, event.y))
                mTowerInserter!!.setPosition(pos)
                true
            }

            DragEvent.ACTION_DROP -> {
                mTowerInserter!!.buyTower()
                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                mTowerInserter!!.cancel()
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!event.result) {
                    mTowerInserter!!.cancel()
                }
                true
            }

            else -> false
        }
    }
}
