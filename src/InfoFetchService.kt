package com.dadachen

import com.google.gson.Gson

data class BuildingInfo(
    var bid: String,
    var fids: Array<Int>
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
            key,
            u.map { it[2].toString().toInt() }.toTypedArray()
        )
    }.toTypedArray()
}
fun insertInfos(infos:Array<BuildingInfo>):Boolean{
    if (infos.isEmpty()) {
        return true
    }
    val sql = "insert into building_info (fid, bid) values"
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
