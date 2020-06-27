package com.example.service

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.file.FileSystemDirectory
import com.example.entity.Profile
import com.example.entity.Profiles
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileService {
  fun generateId(): String = UUID.randomUUID().toString().replace("-", "")
  fun findById(id: String): List<Profile> = transaction {
    Profiles.select { Profiles.id eq id }
      .map {
        Profile(
          filename = it[Profiles.filename],
          height = it[Profiles.height],
          width = it[Profiles.width],
          photoDate = it[Profiles.photoDate]?.toDate()
        )
      }
  }
  fun create(profile: Profile): Profile? = transaction {
    val insertedId = Profiles.insert {
      it[id] = profile.id
      it[filename] = profile.filename
      it[height] = profile.height
      it[width] = profile.width
      it[photoDate] = dateToDateTime(profile.photoDate)
    } get Profiles.id
    Profiles.select { Profiles.id eq insertedId and (Profiles.filename eq profile.filename) }
      .mapNotNull { toProfile(it) }
      .singleOrNull()
  }
  fun findAll(): List<Profile> = transaction {
    Profiles.selectAll().map { toProfile(it) }
  }
  fun getImageMetadata(filename: String, uploadPath: String): Profile {
    val file = File("$uploadPath/$filename")
    val metaData = ImageMetadataReader.readMetadata(file)
    val exifDirectory: ExifSubIFDDirectory? = metaData.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)

    if (exifDirectory != null) {
      val date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
      val height = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)
      val width = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)

      val fileSystemDirectory = metaData.getFirstDirectoryOfType(FileSystemDirectory::class.java)
      val filename = fileSystemDirectory.getString(FileSystemDirectory.TAG_FILE_NAME)

      return Profile(filename = filename, height = height, width = width, photoDate = date)
    } else
      throw Exception("exif directory가 null 입니다.")
  }

  suspend fun uploadImage(multipart: MultiPartData, uploadPath: String): String {
    var filename = ""
    multipart.forEachPart { part ->
      // if part is a file (could be form item)
      if(part is PartData.FileItem) {
        // retrieve file name of upload
        val name = part.originalFileName!!
        val file = File("$uploadPath/$name")
        filename = name

        // use InputStream from part to save file
        part.streamProvider().use { its ->
          // copy the stream to the file with buffering
          file.outputStream().buffered().use {
            // note that this is blocking
            its.copyTo(it)
          }
        }
      }
      // make sure to dispose of the part after use to prevent leaks
      part.dispose()
    }
    return filename
  }

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
      photoDate = row[Profiles.photoDate]?.toDate()
    )
}