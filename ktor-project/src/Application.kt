package com.example

import com.example.routes.profile
import com.example.service.ProfileService
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>): Unit {
//  io.ktor.server.netty.EngineMain.main(args)
  embeddedServer(Netty, port = 8080) {
    module()
  }.start(wait = true)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
  install(ContentNegotiation) {
    jackson {
      enable(SerializationFeature.INDENT_OUTPUT)
    }
  }

  DatabaseFactory.init()

  val uploadPath = "upload"
  Files.createDirectories(Paths.get(uploadPath))

  routing {
    this.profile(ProfileService(), uploadPath)

    // Static feature. Try to access `/static/ktor_logo.svg`
    static("/static") {
      resources("static")
    }
  }
}
