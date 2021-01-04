package com.dadachen

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    connection.connect()
    embeddedServer(Netty, port = 16828){
        charset("utf-8")
        install(ContentNegotiation){
            gson {
                setPrettyPrinting()
            }
        }
        install(Compression){
            gzip()
        }
        routing {
            route("/point"){
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
                get("{id}"){
                    val id = call.parameters["id"]?.toInt()?:0
                    val res = findPoint(id)
                    call.respond(res)
                }
            }
        }
    }.start()
}


