package com.example

import com.example.entity.Profiles
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
  fun init() {
    Database.connect(hikari())
    transaction {
      addLogger(StdOutSqlLogger)
      create(Profiles)
    }
  }

  private fun hikari(): HikariDataSource {
    val config = HikariConfig()
    // mysql
    /*    config.jdbcUrl = "jdbc:mysql://localhost:3306/test?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul"
    config.driverClassName = "com.mysql.cj.jdbc.Driver"
    config.username = "root"
    config.password = "1234"*/
    config.driverClassName = "org.h2.Driver"
    config.jdbcUrl = "jdbc:h2:mem:test"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    config.validate()
    return HikariDataSource(config)
  }
}