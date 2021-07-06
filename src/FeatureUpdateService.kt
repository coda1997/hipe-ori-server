package com.dadachen

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File

data class PPoint(
    @SerializedName("Building ID") val bid: String,
    @SerializedName("Floor ID") val fid: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("PosLon") val posLon: Float,
    @SerializedName("PosLat") val posLat: Float,
    @SerializedName("Point NO") val pno: Int,
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
    @SerializedName("SSID") val ssid: String,
    @SerializedName("AP") val ap: Int
)

fun updateWifiFeature(file: File, bid: String): Boolean {
    val content = file.readBytes()
    //its a json file, need to parse it
    val points = Gson().fromJson(content.toString(), Array<PPoint>::class.java)
    val r = getLastVersion(bid, "wifi")
    val modelNum = r[0]
    val updateNum = r[1]+1
    //handle points
    val sql = genWifiInsertSql(points, modelNum, updateNum)
    updateVersion(modelNum, updateNum, bid, "wifi")
    //insert points into db file
    val result = connection.sendQuery(sql).join()

    return result.rowsAffected == points.totalCount()
}

private fun Array<PPoint>.totalCount(): Long {
    var c = 0L
    this.forEach { p ->
        p.wifiScans.forEach { scan ->
            c += scan.wifiScanInfos.size
        }
    }
    return c
}

private val baseSQL =
    "insert into fingerprint_lib (model_num, update_num, building_id, floor, signal_type,coordinate_x,coordinate_y,signal_mac_address,signal_strength,signal_time) values"

private fun genWifiInsertSql(points: Array<PPoint>, modelNum: Int, updateNum: Int): String {
    val sqlList = mutableListOf<String>()
    for (point in points) {
        for (wifiScan in point.wifiScans) {
            for (wifiScanInfo in wifiScan.wifiScanInfos) {
                val sql =
                    "($modelNum,$updateNum,'${point.bid}','${point.fid}',${point.posLon},${point.posLat},'${wifiScanInfo.bssid}',${wifiScanInfo.level},'${wifiScanInfo.date}')"
                sqlList.add(sql)
            }
        }
    }
    return baseSQL + sqlList.joinToString(",")
}

private fun updateVersion(modelNum: Int, updateNum: Int, bid: String, type: Int): Boolean {
    val sql =
        "insert into version_lib (model_num, update_num, building_id, signal_type) value ($modelNum, $updateNum,'$bid',$type )"
    val result = connection.sendQuery(sql).join()
    return result.rowsAffected == 1L
}

private fun updateVersion(modelNum: Int, updateNum: Int, bid: String, type: String):Boolean {
    val intType: Int = when (type) {
        "wifi" -> 1
        "ble" -> 2
        "pic" -> 3
        "mag" -> 4
        else -> 0
    }
    return updateVersion(modelNum, updateNum, bid, intType)
}

fun getLastVersion(bid:String, type: String):IntArray{
    val intType: Int = when (type) {
        "wifi" -> 1
        "ble" -> 2
        "pic" -> 3
        "mag" -> 4
        else -> 0
    }
    return getLastVersion(bid, intType)
}
private fun getLastVersion(bid: String, type: Int):IntArray{
    val sql = "select * from version_lib where bid = '$bid' and type = $type order by model_num, update_num ascending limet 1"
    val result = connection.sendQuery(sql).join()
    if (!result.rows.isEmpty()){
        val row = result.rows[0]
        val modelNum = row[1].toString().toInt()
        val updateNum = row[2].toString().toInt()
        return intArrayOf(modelNum, updateNum)
    }else{
        return intArrayOf(-1,-1)
    }
}

fun updateBleFeature(bid: String, file: File): Boolean {
    val path = "/uploads/$bid/${file.name}"
    val r = getLastVersion(bid, "ble")
    val modelNum = r[0]+1
    val updateNum = r[1]
    updateVersion(modelNum, updateNum, bid, "ble")
    return insertIntoStaticLib(bid, modelNum, updateNum, "bld", "0", path)
}

fun insertIntoStaticLib(bid: String, modelNum: Int, updateNum: Int, typeString: String, floor:String, sourcePath:String) :Boolean{
    val type:Int = when (typeString) {
        "wifi" -> 1
        "ble" -> 2
        "pic" -> 3
        "mag" -> 4
        else -> 0
    }
    val sql = "insert into static_feature_lib (model_num, update_num, building_id, signal_type, floor, source_url) value ($modelNum, $updateNum, '$bid',$type,'$floor','$sourcePath')"
    val result = connection.sendQuery(sql).join()
    return result.rowsAffected == 1L
}


fun updatePicFeature(bid: String, file: File): Boolean {
    val r = getLastVersion(bid, "pic")
    val modelNum = r[0]+1
    val updateNum = r[1]
    val path = "/uploads/$bid/${file.name}"
    updateVersion(modelNum, updateNum, bid, "pic")
    return insertIntoStaticLib(bid, modelNum, updateNum, "pic", "0", path)
}

fun updateMagFeature(bid: String, fid: Int, file: File): Boolean {
    val r = getLastVersion(bid, "mag")
    val path = "/uploads/$bid/${file.name}"
    updateVersion(r[0]+1, r[1], bid, "mag")
    return insertIntoStaticLib(bid, r[0],r[1],"mag",fid.toString(),path)
}
