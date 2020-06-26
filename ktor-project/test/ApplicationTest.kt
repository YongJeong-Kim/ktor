package com.example

import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifImageDirectory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.example.entity.Profile
import com.example.entity.Profiles
import com.example.service.ProfileService
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.joda.time.DateTime
import org.junit.Assert.assertThat
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class ApplicationTest {
  @Test
  fun findById() {
    Database.connect(
      url = "jdbc:mysql://localhost:3306/test?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul",
      driver = "com.mysql.cj.jdbc.Driver",
      user = "root",
      password = "1234")
    val profileService = ProfileService()
    profileService.create(Profile(height = 11, width = 22, filename = "8F82D81D79H", photoDate = null))
    assertNotNull(profileService.findByFilename("8F82D81D79H"))
    profileService.deleteByFilename("8F82D81D79H")
  }
  @Test
  fun testRoot() {
    withTestApplication({ module(testing = true) }) {
      handleRequest(HttpMethod.Get, "/").apply {
        assertEquals(HttpStatusCode.OK, response.status())
        assertEquals("HELLO WORLD!", response.content)
      }
    }
  }
  @Test
  fun testProfileRoot() {
    withTestApplication({ module(testing = true) }) {
      handleRequest(HttpMethod.Get, "/profile").apply {
        assertEquals(HttpStatusCode.OK, response.status())
      }
    }
  }
  @Test
  fun testUpload() {
    val call = withTestApplication({ module(testing = true) }) {
      handleRequest(HttpMethod.Post, "/upload") {
        val boundary = "***bbb***"

        addHeader(
          HttpHeaders.ContentType,
          ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
        )
        setBody(boundary, listOf(
          PartData.FormItem(
            "title123", { }, headersOf(
              HttpHeaders.ContentDisposition,
              ContentDisposition.Inline
                .withParameter(ContentDisposition.Parameters.Name, "title")
                .toString()
            )
          ),
          PartData.FileItem({ byteArrayOf(1, 2, 3).inputStream().asInput() }, {}, headersOf(
            HttpHeaders.ContentDisposition,
            ContentDisposition.File
              .withParameter(ContentDisposition.Parameters.Name, "file")
              .withParameter(ContentDisposition.Parameters.FileName, "file.txt")
              .toString()
          )
          )
        )
        )
      }
    }
  }
  @Test
  fun fff() {
    val ffile = File("""C:\Users\yongyong\Desktop\11.jpg""")
    val metaData = ImageMetadataReader.readMetadata(ffile)
    val directory = metaData.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
    val bool = directory.containsTag(ExifSubIFDDirectory.TAG_COLOR_SPACE)
    val bool2 = directory.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)
    val bool3 = directory.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)
    val bool4 = directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
    val qqqw = directory.containsTag(ExifIFD0Directory.TAG_INTEROP_INDEX)
    val color = directory.getInt(ExifSubIFDDirectory.TAG_COLOR_SPACE)
    val width = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)
    val height = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)
    val date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
    metaData.directories.forEach { directory ->
      directory.tags.forEach {
        println("""${it.tagTypeHex}, ${it.tagName}, ${it.description}, ${it.tagTypeHex}, ${it.tagType}, ${it.directoryName}""")
      }
    }
  }

  @Test
  fun jpeg() {
    val ffile = File("""C:\Users\yongyong\Documents\카카오톡 받은 파일\11.jpg""")
    val metaData = ImageMetadataReader.readMetadata(ffile)
    val jpg = metaData.getFirstDirectoryOfType(ExifImageDirectory::class.java)
    val date = jpg.getString(ExifIFD0Directory.TAG_DATETIME)
//    val height = jpg.imageHeight
//    val width = jpg.imageWidth
  }

  @Test
  fun dateToDateTime() {
    val date = Date()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    val dateString = format.format(date)
    println(dateString)

    println(DateTime.parse("2018-01-28T13:42:17.546"))
    println(DateTime.parse(dateString))
  }

  @Test
  fun createIfNotEmpty() {
    Files.createDirectories(Paths.get("upload"))
  }

  @Test
  fun coroutineTest() {
    runBlocking {
      println("start")
      launch {
        delay(3000)
        println("launch 1")
      }
      launch {
        delay(3000)
        println("launch 2")
      }
      val qq = async {
        delay(3000)
        11
      }
      println("end")
      println(qq.await())
    }
  }
}

fun sum(): Int {
  var sum: Int = 0
  runBlocking {
    val jobA = async { funA() }
    val jobB = async { funB() }
//    runBlocking {
    sum = jobA.await() + jobB.await()
//    }
  }
  return sum
}

suspend fun funA(): Int {
  return 1
}

suspend fun funB(): Int {
  return 2
}

