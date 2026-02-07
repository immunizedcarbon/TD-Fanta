package com.tdfanta.game.business.game

import com.tdfanta.game.R
import java.util.Collections

class MapRepository {
    private val mMapInfos = ArrayList<MapInfo>()

    init {
        mMapInfos.add(MapInfo("original", R.string.map_original_name, R.raw.map_original))
        mMapInfos.add(MapInfo("waiting_line", R.string.map_waiting_line_name, R.raw.map_waiting_line))
        mMapInfos.add(MapInfo("turn_round", R.string.map_turn_round_name, R.raw.map_turn_round))
        mMapInfos.add(MapInfo("hurry", R.string.map_hurry_name, R.raw.map_hurry))
        mMapInfos.add(MapInfo("civyshk_yard", R.string.map_civyshk_yard_name, R.raw.map_civyshk_yard))
        mMapInfos.add(MapInfo("civyshk_2y", R.string.map_civyshk_2y_name, R.raw.map_civyshk_2y))
        mMapInfos.add(MapInfo("civyshk_line5", R.string.map_civyshk_line5_name, R.raw.map_civyshk_line5))
        mMapInfos.add(MapInfo("civyshk_labyrinth", R.string.map_civyshk_labyrinth_name, R.raw.map_civyshk_labyrinth))
        mMapInfos.add(MapInfo("higgledy_piggledy", R.string.map_higgledy_piggledy_name, R.raw.map_higgledy_piggledy))
        mMapInfos.add(MapInfo("big_u", R.string.map_big_u_name, R.raw.map_big_u))
        mMapInfos.add(MapInfo("cloverleaf", R.string.map_cloverleaf_name, R.raw.map_cloverleaf))
        mMapInfos.add(MapInfo("roundabout", R.string.map_roundabout_name, R.raw.map_roundabout))
        mMapInfos.add(MapInfo("runway", R.string.map_runway_name, R.raw.map_runway))
        mMapInfos.add(MapInfo("wtf", R.string.map_wtf_name, R.raw.map_wtf))
        mMapInfos.add(MapInfo("turn_left", R.string.map_turn_left_name, R.raw.map_turn_left))
        mMapInfos.add(MapInfo("turn_right", R.string.map_turn_right_name, R.raw.map_turn_right))
        mMapInfos.add(MapInfo("oddball", R.string.map_oddball_name, R.raw.map_oddball))
        mMapInfos.add(MapInfo("spiral1", R.string.map_spiral1_name, R.raw.map_spiral1))
        mMapInfos.add(MapInfo("chaos", R.string.map_chaos_name, R.raw.map_chaos))
        mMapInfos.add(MapInfo("moar_chaos", R.string.map_moar_chaos_name, R.raw.map_moar_chaos))
        mMapInfos.add(MapInfo("spiral2", R.string.map_spiral2_name, R.raw.map_spiral2))
        mMapInfos.add(MapInfo("nou", R.string.map_nou_name, R.raw.map_nou))
        mMapInfos.add(MapInfo("highscore", R.string.map_highscore_name, R.raw.map_highscore))
    }

    fun getMapInfos(): List<MapInfo> = Collections.unmodifiableList(mMapInfos)

    fun getMapById(mapId: String): MapInfo {
        for (mapInfo in mMapInfos) {
            if (mapInfo.getMapId() == mapId) {
                return mapInfo
            }
        }

        throw RuntimeException("Map not found!")
    }

    fun getDefaultMapId(): String = "original"
}
