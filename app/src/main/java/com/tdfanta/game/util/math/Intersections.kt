package com.tdfanta.game.util.math

object Intersections {
    @JvmStatic
    fun getPathSectionsInRange(
        wayPoints: List<Vector2>,
        position: Vector2,
        range: Float,
    ): Collection<Line> {
        val r2 = MathUtils.square(range)
        val sections = ArrayList<Line>()

        for (i in 1 until wayPoints.size) {
            val p1 = Vector2.to(position, wayPoints[i - 1])
            val p2 = Vector2.to(position, wayPoints[i])

            val p1in = p1.len2() <= r2
            val p2in = p2.len2() <= r2

            val intersections = lineCircle(p1, p2, range)

            val sectionP1: Vector2
            val sectionP2: Vector2

            if (p1in && p2in) {
                sectionP1 = Vector2.add(p1, position)
                sectionP2 = Vector2.add(p2, position)
            } else if (!p1in && !p2in) {
                if (intersections == null) {
                    continue
                }

                val a1 = intersections[0].angleTo(p1)
                val a2 = intersections[0].angleTo(p2)

                if (MathUtils.equals(a1, a2, 10f)) {
                    continue
                }

                sectionP1 = Vector2.add(intersections[0], position)
                sectionP2 = Vector2.add(intersections[1], position)
            } else {
                val angle = p1.angleTo(p2)

                if (p1in) {
                    sectionP2 = if (MathUtils.equals(angle, p1.angleTo(intersections!![0]), 10f)) {
                        Vector2.add(intersections[0], position)
                    } else {
                        Vector2.add(intersections[1], position)
                    }

                    sectionP1 = Vector2.add(p1, position)
                } else {
                    sectionP1 = if (MathUtils.equals(angle, intersections!![0].angleTo(p2), 10f)) {
                        Vector2.add(intersections[0], position)
                    } else {
                        Vector2.add(intersections[1], position)
                    }

                    sectionP2 = Vector2.add(p2, position)
                }
            }

            sections.add(Line(sectionP1, sectionP2))
        }

        return sections
    }

    private fun lineCircle(p1: Vector2, p2: Vector2, r: Float): Array<Vector2>? {
        val d = Vector2.to(p1, p2)
        val dr2 = d.len2()
        val D = p1.x() * p2.y() - p2.x() * p1.y()

        var discriminant = MathUtils.square(r) * dr2 - MathUtils.square(D)
        if (discriminant < 0) {
            return null
        }

        val ret = arrayOf(Vector2(), Vector2())

        discriminant = kotlin.math.sqrt(discriminant.toDouble()).toFloat()

        val y1 = (-D * d.x() + kotlin.math.abs(d.y()) * discriminant) / dr2
        ret[0] = Vector2((D * d.y() + MathUtils.sign(d.y()) * d.x() * discriminant) / dr2, y1)

        val y = (-D * d.x() - kotlin.math.abs(d.y()) * discriminant) / dr2
        ret[1] = Vector2((D * d.y() - MathUtils.sign(d.y()) * d.x() * discriminant) / dr2, y)

        return ret
    }
}
