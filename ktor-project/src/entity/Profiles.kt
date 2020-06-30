package com.example.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import java.util.*

object Profiles: Table() {
  val id = char("id", 32)
  val filename = varchar("filename", 100)
  val height = integer("height")
  val width = integer("width")
  val photoDate = datetime("photo_date").nullable()
  val rMin = integer("r_min")
  val rMax = integer("r_max")
  val rAvg = integer("r_avg")
  val gMin = integer("g_min")
  val gMax = integer("g_max")
  val gAvg = integer("g_avg")
  val bMin = integer("b_min")
  val bMax = integer("b_max")
  val bAvg = integer("b_avg")
  val histogram = text("histogram")

  override val primaryKey = PrimaryKey(id)
}

data class Profile(
  val id: String = "",
  val filename: String = "",
  val height: Int = 0,
  val width: Int = 0,
  val photoDate: Date? = Date(),
  val rMin: Int = 0,
  val rMax: Int = 0,
  val rAvg: Int = 0,
  val gMin: Int = 0,
  val gMax: Int = 0,
  val gAvg: Int = 0,
  val bMin: Int = 0,
  val bMax: Int = 0,
  val bAvg: Int = 0,
  val histogram: String = ""
) /*{
  var id: String = ""
  constructor(id: String, filename: String, height: Int, width: Int, photoDate: Date?): this(filename, height, width, photoDate) {
    this.id = id
  }
}*/

data class UploadInfoDTO(
  val filename: String,
  val uploadPath: String
)

data class PixelStatisticsDTO(
  val rMin: Int,
  val rMax: Int,
  val rAvg: Int,
  val gMin: Int,
  val gMax: Int,
  val gAvg: Int,
  val bMin: Int,
  val bMax: Int,
  val bAvg: Int
)

data class ImageMetadataDTO(
  val filename: String,
  val height: Int,
  val width: Int,
  val photoDate: Date?
)