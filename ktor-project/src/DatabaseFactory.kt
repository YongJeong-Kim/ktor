package com.example

import com.example.entity.Profiles
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
  fun init() {
    Database.connect(
      url = "jdbc:mysql://localhost:3306/test?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul",
      driver = "com.mysql.cj.jdbc.Driver",
      user = "root",
      password = "1234")
    transaction {
      addLogger(StdOutSqlLogger)
      create(Profiles)
    }
  }
}