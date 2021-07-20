package com.dadachen

data class BuildingInfo(
    var bid: String,
    var fids: Array<Int>,
    var features:Array<FeatureInfo>
)

data class FeatureInfo(
    var type:String,
    var fid:Int,
    var path:String
)

private fun BuildingInfo.toSqlString():String{
    val sqls = fids.map {
        "('$bid', $it)"
    }
    return sqls.joinToString { "," }
}


//DAO

fun getInfos(): Array<BuildingInfo> {
    val sql = "select * from building_info"
    val res = connection.sendQuery(sql).join()
    if (res.rowsAffected == 0L) {
        return emptyArray()
    }
    return res.rows.groupBy { it[1].toString() }.map { entry->
        val key = entry.key
        val u = entry.value
        BuildingInfo(
            bid = key,
            fids = u.map { it[2].toString().toInt() }.toTypedArray(),
            features = getFeatureInfos(key)
        )
    }.toTypedArray()
}

fun getFeatureInfos(bid: String):Array<FeatureInfo>{
    val sql = "select * from static_feature_lib where bid = '$bid'"
    val res = connection.sendQuery(sql).join()
    val infos = mutableListOf<FeatureInfo>()
    res.rows.forEach {
        infos.add(
            FeatureInfo(
                type = it[4].toString().toInt().convertToType(),
                fid = it[5].toString().toInt(),
                path = it[6].toString()
            )
        )
    }
    return infos.toTypedArray()
}
private fun Int.convertToType():String = when(this){
    1-> "wifi"
    2->"ble"
    3->"pic"
    4->"mag"
    else->""
}

fun insertInfos(infos:Array<BuildingInfo>):Boolean{
    if (infos.isEmpty()) {
        return true
    }
    val sql = "insert into building_info (bid, fid) values "
    val insertSql = sql+infos.map {
        it.toSqlString()
    }.joinToString { "," }
    connection.sendQuery(insertSql).join()
    return true
}

fun deleteInfoBid(bid: String):Boolean{
    val sql = "delete from building_info where bid=$bid"
    connection.sendQuery(sql).join()
    return true
}
fun deleteInfoByBidAndFid(bid: String, fid:Int):Boolean{
    val sql = "delete from building_info where bid='$bid' and fid=$fid"
    connection.sendQuery(sql).join()
    return true
}
