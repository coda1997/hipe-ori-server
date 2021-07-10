package com.dadachen

import com.github.jasync.sql.db.Connection
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File
import java.io.FileReader
import java.util.*
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

lateinit var connection: Connection
fun Application.module() {
    val file = FileReader(File("config.json")).readText()
    val config = Gson().fromJson(file, Dconfig::class.java)
    println(config.toString())
    connection = PostgreSQLConnectionBuilder.createConnectionPool(
        "jdbc:postgresql://${config.ip}:${config.dataBasePort}/${config.dataBaseName}?user=${config.username}&password=${config.password}"
    )
    connection.connect()
    charset("utf-8")
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Compression) {
        gzip()
    }
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, message = cause.localizedMessage)
        }
    }
    routing {
        route("/point") {
            get {
                call.respond(findPoint())
            }
            patch {
                println("start inserting")
                val points = call.receive<Array<Point>>()
                println(points.size)
                val res = addManyPoint(points)
                call.respondText("insert status: $res")
            }
            get("{id}") {
                val id = call.parameters["id"]?.toInt() ?: 0
                val res = findPoint(id)
                call.respond(res)
            }
        }
        route("/feature") {
            get("/version/{bid}"){
                val bid = call.parameters["bid"] ?: ""
                val data = getVersionByBid(bid)
                call.respondText(data)
            }
            get("/{bid}"){
                val bid = call.parameters["bid"] ?: ""
                val f = downloadFeatureData(arrayOf(bid))
                call.respondFile(f)
            }
            post("/wifi/{bid}/{fid}") {
                //handle adding wifi feature
                var success = true
                val bid = call.parameters["bid"] ?: ""
                val fid = call.parameters["fid"]?.toInt() ?: 0
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val name = part.originalFileName!!
                        val f = File("/uploads/$bid/floor_$fid/${UUID.randomUUID().toString()+"-"+name}")
                        part.streamProvider().use { inputSteam ->
                            f.outputStream().buffered().use {
                                inputSteam.copyTo(it)
                            }
                        }
                        success = updateWifiFeature(f, bid) && success
                    }
                    part.dispose()
                }
                if (success) {
                    call.respond(HttpStatusCode.OK, message = "Update succeed")
                } else {
                    call.respond(HttpStatusCode.BadRequest, message = "Update failed")
                }
            }
            post("/ble/{bid}") {
                //handle adding ble
                val bid = call.parameters["bid"] ?: ""
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val name = part.originalFileName!!
                        val f = File("/uploads/$bid/${UUID.randomUUID().toString()+"-"+name}")
                        part.streamProvider().use { inputSteam ->
                            f.outputStream().buffered().use {
                                inputSteam.copyTo(it)
                            }
                        }
                        updateBleFeature(bid, f)
                    }
                    part.dispose()
                }
            }

            post("/pic/{bid}") {
                val bid = call.parameters["bid"] ?: ""
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val name = part.originalFileName!!
                        val f = File("/uploads/$bid/${UUID.randomUUID().toString()+"-"+name}")
                        part.streamProvider().use { inputSteam ->
                            f.outputStream().buffered().use {
                                inputSteam.copyTo(it)
                            }
                        }
                        updatePicFeature(bid, f)
                    }
                    part.dispose()
                }
            }
            post("mag/{bid}/{fid}") {
                val bid = call.parameters["bid"] ?: ""
                val fid = call.parameters["fid"]?.toInt() ?: 0
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val name = part.originalFileName!!
                        val f = File("/uploads/$bid/floor_$fid/${UUID.randomUUID().toString()+"-"+name}")
                        part.streamProvider().use { inputSteam ->
                            f.outputStream().buffered().use {
                                inputSteam.copyTo(it)
                            }
                        }
                        updateMagFeature(bid, fid, f)
                    }
                    part.dispose()

                }
            }
        }

    }
}


