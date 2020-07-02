package service

import com.example.DatabaseFactory
import com.example.entity.*
import com.example.service.ProfileService
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import java.util.*
import kotlin.test.Test

internal class ProfileServiceTest {
  private val uploadPath = """C:\Users\yongyong\Desktop"""
  private val profileService = ProfileService()

  @Before
  fun before() {
    DatabaseFactory.init()
    transaction { Profiles.deleteAll() }
  }

  @Test
  fun findAll() {
    transaction {
      val profile1 = profileService.create(
        Profile("", "test1.jpg", 22323, 4214124, Date())
      )
      val profile2 = profileService.create(
        Profile("", "test2.jpg", 2444, 1111, Date())
      )

      val profiles = profileService.findAll()
      Assertions.assertThat(profiles).extracting("id")
        .containsExactlyInAnyOrder(profile1.id, profile2.id)
      Assertions.assertThat(profiles).extracting("filename")
        .containsExactlyInAnyOrder(profile1.filename, profile2.filename)
    }
  }
  @Test
  fun findById() {
    transaction {
      val profile = profileService.create(
        Profile(id = "", height = 11, width = 22, filename = "test1.jpg", photoDate = Date())
      )
      assertNotNull(profileService.findById(profile.id))
    }
  }
  @Test
  fun create() {
    transaction {
      val profile = profileService.create(
        Profile(id = "", height = 11, width = 22, filename = "test1.jpg", photoDate = Date())
      )
      assertNotNull(profileService.findById(profile.id))
    }
  }
  @Test
  fun getImageMetadata() {
    val profile: ImageMetadataDTO =
      profileService.getImageMetadata(UploadInfoDTO("20170418_212607.jpg", uploadPath))
    assertNotNull(profile)
  }
  @Test
  fun getPixelStatistics() {
    val profile: PixelStatisticsDTO =
      profileService.getPixelStatistics(UploadInfoDTO("20170418_212607.jpg", uploadPath))
    assertNotNull(profile)
  }
  @Test
  fun getHistogram() {
    val profile = profileService.getHistogram(UploadInfoDTO("20170418_212607.jpg", uploadPath))
    assertNotEquals("", profile)
  }
}