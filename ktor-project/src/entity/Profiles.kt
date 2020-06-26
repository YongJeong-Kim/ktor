package com.example.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import java.util.*

object Profiles: Table() {
  val id = integer("id").autoIncrement()
  val filename = varchar("filename", 100)
  val height = integer("height")
  val width = integer("width")
  val photoDate = datetime("photo_date").nullable()

  override val primaryKey = PrimaryKey(id, filename)
}

data class Profile(
  val filename: String,
  val height: Int,
  val width: Int,
  val photoDate: Date?
)