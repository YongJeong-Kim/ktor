package com.example

import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.tiff.TiffMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifImageDirectory
import com.drew.metadata.exif.ExifSubIFDDirectory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.junit.Before
import java.awt.Color
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
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
    val ffile = File("""C:\Users\yongyong\Desktop\file_example_TIFF_10MB.tiff""")
    val jpgMeta = TiffMetadataReader.readMetadata(ffile)
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
        println("""${it.tagTypeHex}, ${it.tagName}, ${it.description}, ${it.tagType}, ${it.directoryName}""")
      }
    }
  }

  @Test
  fun jpeg() {
    val ffile = File("""C:\Users\yongyong\Desktop\file_example_TIFF_10MB.tiff""")
    val metaData = ImageMetadataReader.readMetadata(ffile)
    val jpg = metaData.getFirstDirectoryOfType(ExifImageDirectory::class.java)
    val date = jpg.getString(ExifIFD0Directory.TAG_DATETIME)
//    val height = jpg.imageHeight
//    val width = jpg.imageWidth
  }

  @Test
  fun rgb() {
    val file = File("""C:\Users\yongyong\Desktop\KakaoTalk_20200630_152357401.jpg""")
    val image = ImageIO.read(file)

    var minRed = 255
    var maxRed = 0
    var minGreen = 255
    var maxGreen = 0
    var minBlue = 255
    var maxBlue = 0
    var sumRed = 0
    var sumGreen = 0
    var sumBlue = 0
    (0 until image.height).forEach { height ->
      (0 until image.width).forEach { width ->
        val clr = image.getRGB(width, height)
        val red = (clr and 0x00ff0000) shr 16
        val green = (clr and 0x0000ff00) shr 8;
        val blue = clr and 0x000000ff

        sumRed += red
        sumGreen += green
        sumBlue += blue

        minRed =
          if (minRed >= red) red
          else minRed
        maxRed =
          if (maxRed <= red) red
          else maxRed
        minGreen =
          if (minGreen >= green) green
          else minGreen
        maxGreen =
          if (maxGreen <= green) green
          else maxGreen
        minBlue =
          if (minBlue >= blue) blue
          else minBlue
        maxBlue =
          if (maxBlue <= blue) blue
          else maxBlue
      }
    }
    println(image.height)
    println(image.width)
    println("min red : $minRed, max red : $maxRed")
    println("min green : $minGreen, max green : $maxGreen")
    println("min blue : $minBlue, max blue : $maxBlue")
    println("sum red : $sumRed, average : ${sumRed / (image.height * image.width)}")
    println("sum green: $sumGreen, average : ${sumGreen / (image.width * image.width)}")
    println("sum blue: $sumBlue, average : ${sumBlue / (image.width * image.width)}")
  }

  @Test
  fun histogram() {
    val image = ImageIO.read(File("""C:\Users\yongyong\Desktop\KakaoTalk_20200630_152357401.jpg"""))
    val iW = image.width
    val iH = image.height
    val a = Array(
      256
    ) { Array(256) { IntArray(256) } }
    for (i in 0 until iH) {
      for (j in 0 until iW) {
        val c = Color(image.getRGB(j, i))
        a[c.red][c.green][c.blue]++
      }
    }
    for (i in 0..254) {
      for (j in 0..254) {
        for (k in 0..254) {
          if (a[i][j][k] > 0) {
            println(
              """
                colour[$i][$j][$k] repeated ${a[i][j][k]} times 
                """.trimIndent()
     /*         """
                [$i][$j][$k] = ${a[i][j][k]}
                """.trimIndent()*/
            )
            //i->Red j-Green k->Blue
          }
        }
      }
    }
  }

  @Test
  fun histogram2() {
    val ch =
      Array(4) { Array(4) { IntArray(4) } }
    val image = ImageIO.read(File("""C:\Users\yongyong\Desktop\Odd-eyed_cat_by_ihasb33r-2.gif"""))
    for (x in 0 until image.width)
      for (y in 0 until image.height) {
        val color = image.getRGB(x, y)
        val alpha = (color and -0x1000000) shr 24
        val red = (color and 0x00ff0000) shr 16
        val green = (color and 0x0000ff00) shr 8
        val blue = color and 0x000000ff
        ch[red / 64][green / 64][blue / 64]++
//        ch[red][green][blue]++
      }
    for (i in ch.indices)
      for (j in ch[i].indices)
        for (p in ch[i][j].indices)
          if (ch[i][j][p] > 0)
            println("""
              t[$i][$j][$p] = ${ch[i][j][p]}
            """.trimIndent())
//            println("t[" + i + "][" + j + "][" + p + "] = " + ch[i][j][p])
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
      val a = async {
        delay(3000)
        println("launch 1")
        "a"
      }
      val b = async {
        delay(3000)
        println("launch 2")
        "b"
      }
      withContext(Dispatchers.Default) {
        delay(3000)
        println("with context")
      }
      val qq = async {
        delay(3000)
        11
      }
      println("end")
      println(a.await())
      println(b.await())
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

