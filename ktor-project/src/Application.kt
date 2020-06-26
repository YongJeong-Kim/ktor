package com.example

import com.example.entity.Profiles
import com.example.routes.profile
import com.example.service.ProfileService
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
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

  Database.connect(
    url = "jdbc:mysql://localhost:3306/test?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul",
    driver = "com.mysql.cj.jdbc.Driver",
    user = "root",
    password = "1234")
  transaction {
    addLogger(StdOutSqlLogger)
    SchemaUtils.create(Profiles)
  }

  val uploadPath = "upload"
  Files.createDirectories(Paths.get(uploadPath))
  val profileService = ProfileService()

  routing {
    this.profile(profileService, uploadPath)

    // Static feature. Try to access `/static/ktor_logo.svg`
    static("/static") {
      resources("static")
    }

    get("/coroutine") {
      val startTime = System.currentTimeMillis()
      val as1 = async {
        delay(1000)
        num1()
      }
      val as2 = async {
        delay(1000)
        num2()
      }
      val sum = as1.await() + as2.await()
      val endTime = System.currentTimeMillis()
      println("${endTime - startTime}")
      call.respond(sum)
    }
  }
}

fun num1() = 1
fun num2() = 2
