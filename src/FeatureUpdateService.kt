package com.dadachen

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File

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

fun updateWifiFeature(bid: String, fid: Int, file: File): Boolean {
    val content = file.readBytes()
    //its a json file, need to parse it
    val points = Gson().fromJson(content.toString(), Array<PPoint>::class.java)
    //handle points
    //insert points into db file

    return true
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
