package com.example

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifImageDirectory
import com.drew.metadata.exif.ExifSubIFDDirectory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.Before
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
  @Before
  fun before() {
    DatabaseFactory.init()
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
  fun fff() {
    val ffile = File("""C:\Users\yongyong\Desktop\다운로드.jpg""")
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

