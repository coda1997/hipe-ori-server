package com.dadachen

import com.google.gson.Gson

data class FeatureVersion(
    var bid: String,
    var model_num: Int,
    var update_num: Int,
    var signal_type: String
)

fun getVersionByBid(bid: String): String {
    return Gson().toJson(getVersionsFromDB(bid))
}

private fun getTypeByNum(num: Int) = when (num) {
    1 -> "wifi"
    2 -> "ble"
    3 -> "pic"
    4 -> "mag"
    else -> ""
}

fun getVersionsFromDB(bid: String): Array<FeatureVersion> {
    val featureVersions = mutableListOf<FeatureVersion>()
    for (i in 1 ..4){
        val f = getVersionsByTypeFromDB(bid, i)
        f?.let {
            featureVersions.add(it)
        }
    }
    return featureVersions.toTypedArray()
}

fun getVersionsByTypeFromDB(bid: String, type:Int):FeatureVersion?{
    val sql = "select * from version_lib where building_id = $bid and signal_type = $type order by model_num, update_num DESC limit 1"
    val res = connection.sendQuery(sql).join()
    if(res.rowsAffected!=1L){
        return null
    }
    val modelNum =  res.rows[0][1].toString().toInt()
    val updateNum = res.rows[0][2].toString().toInt()
    val signalType = getTypeByNum(res.rows[0][4].toString().toInt())
    return FeatureVersion(bid,modelNum,updateNum,signalType)
}