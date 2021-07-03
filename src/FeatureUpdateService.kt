package com.dadachen

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.lang.StringBuilder

data class PPoint(
    @SerializedName("Building ID") val bid: String,
    @SerializedName("Floor ID") val fid:Int,
    @SerializedName("Date") val date:String,
    @SerializedName("PosLon") val posLon:Float,
    @SerializedName("PosLat") val posLat:Float,
    @SerializedName("Point NO") val pno:Int,
    @SerializedName("WIFIscan") val wifiScans: Array<WifiScan>
)
data class WifiScan(
    @SerializedName("Round") val round: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("WifiScanInfo") val wifiScanInfos: Array<WifiScanInfo>
)

data class WifiScanInfo(
    @SerializedName("BSSID") val bssid: String, //it's mac address
    @SerializedName("Level") val level: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("SSID") val ssid:String,
    @SerializedName("AP") val ap:Int
)

fun updateWifiFeature(file: File): Boolean {
    val content = file.readBytes()
    //its a json file, need to parse it
    val points = Gson().fromJson(content.toString(), Array<PPoint>::class.java)
    //handle points
    val sql = genWifiInsertSql(points)

    //insert points into db file
    val result = connection.sendQuery(sql).join()

    return result.rowsAffected==points.totalCount()
}

private fun Array<PPoint>.totalCount():Long{
    var c = 0L
    this.forEach { p->
        p.wifiScans.forEach { scan->
            c+=scan.wifiScanInfos.size
        }
    }
    return c
}

private val baseSQL =
    "insert into fingerprint_lib (model_num, update_num, building_id, floor, signal_type,coordinate_x,coordinate_y,signal_mac_address,signal_strength,signal_time) values"
private var modelNum = 1
private var updateNum = 1
private fun genWifiInsertSql(points:Array<PPoint>):String{
    val sqlList = mutableListOf<String>()
    for (point in points) {
        for (wifiScan in point.wifiScans) {
            for (wifiScanInfo in wifiScan.wifiScanInfos) {
                val sql = "($modelNum,$updateNum,'${point.bid}','${point.fid}',${point.posLon},${point.posLat},'${wifiScanInfo.bssid}',${wifiScanInfo.level},'${wifiScanInfo.date}')"
                sqlList.add(sql)
            }
        }
    }
    return baseSQL+sqlList.joinToString(",")
}

fun updateBleFeature(bid: String, file: File): Boolean {
    return true
}

fun updatePicFeature(bid: String, file: File): Boolean{
    return true
}
fun updateMagFeature(bid: String, fid: Int, file: File):Boolean{
    return true
}
