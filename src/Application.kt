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
    embeddedServer(Netty, port = 16828){
        install(ContentNegotiation){
            gson {
                setPrettyPrinting()
            }
        }
        install(Compression){
            gzip()
        }
        routing {
            get("/point/{id}"){
                val id = call.parameters["id"]?.toInt()?:0
                val res = findPoint(id)
                call.respond(res)
            }

            get("/point"){
                call.respond(findPoint())
            }

            patch("/point"){
                val points = call.receive<List<Point>>()
                addManyPoint(points)
            }
        }
    }.start()
}


