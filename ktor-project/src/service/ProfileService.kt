package com.example.service

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.file.FileSystemDirectory
import com.example.entity.*
import com.zaxxer.hikari.metrics.prometheus.PrometheusHistogramMetricsTrackerFactory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

class ProfileService {
  fun generateId(): String = UUID.randomUUID().toString().replace("-", "")
  fun findById(id: String): List<Profile> = transaction {
    Profiles.select { Profiles.id eq id }
      .map { toProfile(it) }
  }
  fun create(profile: Profile): Profile? = transaction {
    val insertedId = Profiles.insert {
      it[id] = generateId()
      it[filename] = profile.filename
      it[height] = profile.height
      it[width] = profile.width
      it[photoDate] = dateToDateTime(profile.photoDate)
      it[rMin] = profile.rMin
      it[rMax] = profile.rMax
      it[rAvg] = profile.rAvg
      it[gMin] = profile.gMin
      it[gMax] = profile.gMax
      it[gAvg] = profile.gAvg
      it[bMin] = profile.bMin
      it[bMax] = profile.bMax
      it[bAvg] = profile.bAvg
      it[histogram] = profile.histogram
    } get Profiles.id
    Profiles.select { Profiles.id eq insertedId and (Profiles.filename eq profile.filename) }
      .map { toProfile(it) }
      .singleOrNull()
  }
  fun findAll(): List<Profile> = transaction {
    Profiles.selectAll().map { toProfile(it) }
  }
  fun getImageMetadata(uploadInfoDTO: UploadInfoDTO): ImageMetadataDTO {
    val file = File("${uploadInfoDTO.uploadPath}/${uploadInfoDTO.filename}")
    val metaData = ImageMetadataReader.readMetadata(file)
    val exifDirectory: ExifSubIFDDirectory? = metaData.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)

    if (exifDirectory != null) {
      val date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
      val height = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)
      val width = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)

      val fileSystemDirectory = metaData.getFirstDirectoryOfType(FileSystemDirectory::class.java)
      val filename = fileSystemDirectory.getString(FileSystemDirectory.TAG_FILE_NAME)

      return ImageMetadataDTO(filename = filename, height = height, width = width, photoDate = date)
    } else
      throw Exception("exif directory가 null인 이미지 입니다.")
  }
  fun getPixelStatistics(uploadInfoDTO: UploadInfoDTO): PixelStatisticsDTO {
    val avgValue = { sum: Int, value: Int ->
      sum / value
    }
    val image = ImageIO.read(File("${uploadInfoDTO.uploadPath}/${uploadInfoDTO.filename}"))
    val areaValue = image.height * image.width
    var redMin = 255
    var redMax = 0
    var greenMin = 255
    var greenMax = 0
    var blueMin = 255
    var blueMax = 0
    var redSum = 0
    var greenSum = 0
    var blueSum = 0

    (0 until image.height).forEach { height ->
      (0 until image.width).forEach { width ->
        val clr = image.getRGB(width, height)
        val red = (clr and 0x00ff0000) shr 16
        val green = (clr and 0x0000ff00) shr 8;
        val blue = clr and 0x000000ff

        // sum
        redSum += red
        greenSum += green
        blueSum += blue

        redMin =
          if (redMin >= red) red
          else redMin
        redMax =
          if (redMax <= red) red
          else redMax
        greenMin =
          if (greenMin >= green) green
          else greenMin
        greenMax =
          if (greenMax <= green) green
          else greenMax
        blueMin =
          if (blueMin >= blue) blue
          else blueMin
        blueMax =
          if (blueMax <= blue) blue
          else blueMax
      }
    }
    return PixelStatisticsDTO(rMin = redMin, rMax = redMax, rAvg = avgValue(redSum, areaValue),
      gMin = greenMin, gMax = greenMax, gAvg = avgValue(greenSum, areaValue),
      bMin = blueMin, bMax = blueMax, bAvg = avgValue(blueSum, areaValue))
  }

  fun getHistogram(uploadInfoDTO: UploadInfoDTO): String {
    val ch = Array(256) { Array(256) { IntArray(256) } }
    val image = ImageIO.read(File("${uploadInfoDTO.uploadPath}/${uploadInfoDTO.filename}"))
    for (x in 0 until image.width)
      for (y in 0 until image.height) {
        val color = image.getRGB(x, y)
        val alpha = (color and -0x1000000) shr 24
        val red = (color and 0x00ff0000) shr 16
        val green = (color and 0x0000ff00) shr 8
        val blue = color and 0x000000ff
//        ch[red / 64][green / 64][blue / 64]++
        ch[red][green][blue]++
      }

    var histogramString = ""
    for (i in ch.indices)
      for (j in ch[i].indices)
        for (p in ch[i][j].indices)
          if (ch[i][j][p] > 0)
            histogramString += "$i:$j:$p:${ch[i][j][p]},"
    /*        println("""
              t[$i][$j][$p] = ${ch[i][j][p]}
            """.trimIndent())*/
//            println("t[" + i + "][" + j + "][" + p + "] = " + ch[i][j][p])
    return histogramString
  }
  fun mergeProfile(imageMetadataDTO: ImageMetadataDTO,
                   pixelStatisticsDTO: PixelStatisticsDTO,
                   histogram: String): Profile =
    Profile(filename = imageMetadataDTO.filename, height = imageMetadataDTO.height, width = imageMetadataDTO.width,
      rMin = pixelStatisticsDTO.rMin, rMax = pixelStatisticsDTO.rMax, rAvg = pixelStatisticsDTO.rAvg,
      gMin = pixelStatisticsDTO.gMin, gMax = pixelStatisticsDTO.gMax, gAvg = pixelStatisticsDTO.gAvg,
      bMin = pixelStatisticsDTO.bMin, bMax = pixelStatisticsDTO.bMax, bAvg = pixelStatisticsDTO.bAvg,
      histogram = histogram)

  private fun dateToDateTime(date: Date?): DateTime? =
    if (dateIsNotNull(date)) {
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
      val dateString = format.format(date)
      DateTime.parse(dateString)
    } else null

  private fun dateIsNotNull(date: Date?) = date != null

  private fun toProfile(row: ResultRow): Profile =
    Profile(
      id = row[Profiles.id],
      filename = row[Profiles.filename],
      height = row[Profiles.height],
      width = row[Profiles.width],
      photoDate = row[Profiles.photoDate]?.toDate(),
      rMin = row[Profiles.rMin],
      rMax = row[Profiles.rMax],
      rAvg = row[Profiles.rAvg],
      gMin = row[Profiles.gMin],
      gMax = row[Profiles.gMax],
      gAvg = row[Profiles.gAvg],
      bMin = row[Profiles.bMin],
      bMax = row[Profiles.bMax],
      bAvg = row[Profiles.bAvg],
      histogram = row[Profiles.histogram]
    )
}