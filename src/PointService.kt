package com.dadachen

data class Point(val id:Int, val type:Int, val time:String, val building_name:String, val floor:String, val level:Int = -1, val latitude:String, val longitude:String)

enum class PointType(val type: Int){
    WIFI(1),
    BLE(2),
    MAG(3),
    IMG(4)
}

fun addPoint(point: Point):Boolean{
    return true
}

fun addManyPoint(points:List<Point>):Boolean{
    points.forEach { addPoint(it) }
    return true
}

fun findPoint(id:Int = 0):List<Point>{
    val t = Point(1,1, "aa","aa","1",0,"100.1","200.0")
    return listOf(t)
}

