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
  val redMin = integer("r_min")
  val redMax = integer("r_max")
  val redAvg = integer("r_avg")
  val greenMin = integer("g_min")
  val greenMax = integer("g_max")
  val greenAvg = integer("g_avg")
  val blueMin = integer("b_min")
  val blueMax = integer("b_max")
  val blueAvg = integer("b_avg")
  val histogram = text("histogram")

  override val primaryKey = PrimaryKey(id)
}

data class Profile(
  val id: String = "",
  val filename: String = "",
  val height: Int = 0,
  val width: Int = 0,
  val photoDate: Date? = null,
  val redMin: Int = 0,
  val redMax: Int = 0,
  val redAvg: Int = 0,
  val greenMin: Int = 0,
  val greenMax: Int = 0,
  val greenAvg: Int = 0,
  val blueMin: Int = 0,
  val blueMax: Int = 0,
  val blueAvg: Int = 0,
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
  val redMin: Int,
  val redMax: Int,
  val redAvg: Int,
  val greenMin: Int,
  val greenMax: Int,
  val greenAvg: Int,
  val blueMin: Int,
  val blueMax: Int,
  val blueAvg: Int
)

data class ImageMetadataDTO(
  val filename: String,
  val height: Int,
  val width: Int,
  val photoDate: Date?
)