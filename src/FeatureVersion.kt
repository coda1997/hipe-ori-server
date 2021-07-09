package com.dadachen

import com.google.gson.Gson

data class FeatureVersion(
    var bid: String,
    var model_num: Int,
    var update_num: Int,
    var signal_type: String
)

fun getVersionByBid(bid: String): String {
    val gson = Gson().toJson(getVersionsFromDB(bid))
    return gson
}

private fun getTypeByNum(num: Int) = when (num) {
    1 -> "wifi"
    2 -> "ble"
    3 -> "pic"
    4 -> "mag"
    else -> ""
}

fun getVersionsFromDB(bid: String): Array<FeatureVersion> {
    val sql = "select * from version_lib where building_id = $bid"
    val result = connection.sendQuery(sql).join()
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
    val model_num =  res.rows[0][1].toString().toInt()
    val update_num = res.rows[0][2].toString().toInt()
    val signal_type = getTypeByNum(res.rows[0][4].toString().toInt())
    return FeatureVersion(bid,model_num,update_num,signal_type)
}