package com.tdfanta.game.view.map

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tdfanta.game.engine.logic.map.GameMap
import com.tdfanta.game.engine.logic.map.MapPath
import com.tdfanta.game.engine.logic.map.PlateauInfo
import com.tdfanta.game.engine.render.Viewport
import com.tdfanta.game.util.container.KeyValueStore
import com.tdfanta.game.util.math.Vector2

internal class MapThumbGenerator {
    fun generateThumb(resources: Resources, mapResId: Int): Bitmap =
        generateThumb(GameMap(KeyValueStore.fromResources(resources, mapResId)))

    private fun generateThumb(map: GameMap): Bitmap {
        val bitmap = Bitmap.createBitmap(
            map.getWidth() * PIXELS_PER_SQUARE,
            map.getHeight() * PIXELS_PER_SQUARE,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)

        val viewport = Viewport()
        viewport.setGameSize(map.getWidth(), map.getHeight())
        viewport.setScreenSize(bitmap.width, bitmap.height)
        canvas.concat(viewport.getScreenMatrix())

        drawPaths(canvas, map)
        drawPlateaus(canvas, map)

        return bitmap
    }

    private fun drawPaths(canvas: Canvas, map: GameMap) {
        val pathPaint = Paint()
        pathPaint.style = Paint.Style.FILL
        pathPaint.color = PATH_COLOR

        for (path: MapPath in map.getPaths()) {
            var lastWayPoint: Vector2? = null
            for (wayPoint: Vector2 in path.getWayPoints()) {
                if (lastWayPoint != null) {
                    canvas.drawRect(
                        kotlin.math.min(lastWayPoint.x(), wayPoint.x()) - 0.5f,
                        kotlin.math.min(lastWayPoint.y(), wayPoint.y()) - 0.5f,
                        kotlin.math.max(lastWayPoint.x(), wayPoint.x()) + 0.5f,
                        kotlin.math.max(lastWayPoint.y(), wayPoint.y()) + 0.5f,
                        pathPaint,
                    )
                }
                lastWayPoint = wayPoint
            }
        }
    }

    private fun drawPlateaus(canvas: Canvas, map: GameMap) {
        val plateauPaint = Paint()
        plateauPaint.style = Paint.Style.STROKE
        plateauPaint.strokeWidth = 1f
        plateauPaint.color = PLATEAU_COLOR

        for (plateau: PlateauInfo in map.getPlateaus()) {
            val position = plateau.getPosition()
            canvas.drawPoint(position.x(), position.y(), plateauPaint)
        }
    }

    companion object {
        private const val PIXELS_PER_SQUARE = 10

        private val PLATEAU_COLOR = Color.parseColor("#bbbbbb")
        private val PATH_COLOR = Color.parseColor("#000000")
    }
}
