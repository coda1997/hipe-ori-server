package com.dadachen

import com.github.jasync.sql.db.Connection
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder

data class Point(
    val id: Int,
    val type: Int,
    val time: String,
    val building_name: String,
    val floor: String,
    val level: Int = -1,
    val latitude: String,
    val longitude: String
)

enum class PointType(val type: Int) {
    WIFI(1),
    BLE(2),
    MAG(3),
    IMG(4)
}

val config = Dconfig("127.0.0.1", 5432, "postgres", "123456")
val connection: Connection = PostgreSQLConnectionBuilder.createConnectionPool(
    "jdbc:postgresql://${config.ip}:${config.port}/postgres?user=${config.userName}&password=${config.password}"
)

const val INSERT_SQL = "insert into point (type, time, building_name, floor, level, latitude, longitude) values "

fun addPoint(point: Point): Boolean {
    val res =
        connection.sendQuery(INSERT_SQL + "(${point.type}, '${point.time}', '${point.building_name}', '${point.floor}', ${point.level}, '${point.latitude}', '${point.longitude}')")
            .join()

    return res.rowsAffected == 1L
}

fun addManyPoint(points: Array<Point>): Boolean {
    points.forEach { addPoint(it) }
    return true
}

const val SELECT_SQL = "select * from point where id >= "
fun findPoint(id: Int = 0): List<Point> {
    val res = connection.sendQuery(SELECT_SQL + id).join()
    return res.rows.map {
        Point(
            it[0] as Int,
            it[1] as Int,
            it[2] as String,
            it[3] as String,
            it[4] as String,
            it[5] as Int,
            it[6] as String,
            it[7] as String
        )
    }
}

