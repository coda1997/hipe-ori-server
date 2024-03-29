package com.dadachen

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.lang.StringBuilder

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
    val content = file.readText()
    //its a json file, need to parse it
    val points = Gson().fromJson(content, Array<PPoint>::class.java)
    val r = getLastVersion(bid, "wifi")
    val modelNum = r[0]
    val updateNum = r[1]+1
    //handle points
    //insert points into db file
    genWifiInsertSql(points, modelNum, updateNum)
    updateVersion(modelNum, updateNum, bid, "wifi")
    return true
//    return result.rowsAffected == points.totalCount()
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

private const val baseSQL =
    "insert into fingerprint_lib (model_num, update_num, building_id, floor, signal_type,coordinate_x,coordinate_y,signal_mac_address,signal_name,signal_strength,signal_time) values"

private fun genWifiInsertSql(points: Array<PPoint>, modelNum: Int, updateNum: Int) {
    val sqlList = mutableListOf<String>()
    for (point in points) {
        if (point.bid==""){
            continue
        }
        for (wifiScan in point.wifiScans) {
            for (wifiScanInfo in wifiScan.wifiScanInfos) {
                if(sqlList.size>1000){
                    val t = sqlList.joinToString(",")
//                    print(baseSQL+t)
                    connection.sendQuery(baseSQL+t)
                    sqlList.clear()
                }
                val sql =
                    "($modelNum,$updateNum,'${point.bid}','${point.fid}',1,${point.posLon},${point.posLat},'${wifiScanInfo.bssid}','${wifiScanInfo.ssid}',${wifiScanInfo.level},'${wifiScanInfo.date}')"
                sqlList.add(sql)
            }
        }
    }
    if (sqlList.size>0){
        val t = sqlList.joinToString(",")
        connection.sendQuery(baseSQL+t)
        sqlList.clear()
    }
}

private fun updateVersion(modelNum: Int, updateNum: Int, bid: String, type: Int): Boolean {
    val sql =
        "insert into version_lib (model_num, update_num, building_id, signal_type) values ($modelNum, $updateNum,'$bid',$type )"
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
    val sql = "select * from version_lib where building_id = '$bid' and signal_type = $type order by model_num DESC, update_num DESC limit 1"
    val result = connection.sendQuery(sql).join()
    return if (!result.rows.isEmpty()){
        val row = result.rows[0]
        val modelNum = row[1].toString().toInt()
        val updateNum = row[2].toString().toInt()
        intArrayOf(modelNum, updateNum)
    }else{
        intArrayOf(1,1)
    }
}

fun updateBleFeature(bid: String, file: File): Boolean {
    val path = "/uploads/$bid/${file.name}"
    val r = getLastVersion(bid, "ble")
    val modelNum = r[0]+1
    val updateNum = r[1]
    updateVersion(modelNum, updateNum, bid, "ble")
    return insertIntoStaticLib(bid, modelNum, updateNum, "ble", "0", path)
}

fun insertIntoStaticLib(bid: String, modelNum: Int, updateNum: Int, typeString: String, floor:String, sourcePath:String) :Boolean{
    val type:Int = when (typeString) {
        "wifi" -> 1
        "ble" -> 2
        "pic" -> 3
        "mag" -> 4
        else -> 0
    }
    val sql = "insert into static_feature_lib (model_num, update_num, building_id, signal_type, floor, source_url) values ($modelNum, $updateNum, '$bid',$type,'$floor','$sourcePath')"
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
    val path = "/uploads/$bid/floor_$fid/${file.name}"
    updateVersion(r[0]+1, r[1], bid, "mag")
    return insertIntoStaticLib(bid, r[0]+1,r[1],"mag",fid.toString(),path)
}
