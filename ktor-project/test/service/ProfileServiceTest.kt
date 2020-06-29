package service

import com.example.entity.Profile
import com.example.entity.UploadInfoDTO
import com.example.service.ProfileService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import java.util.*
import kotlin.test.Test

internal class ProfileServiceTest {
  private val profileService = ProfileService()

  @Test
  fun findAll() {
    val profile1 = profileService.create(
      Profile(profileService.generateId(), "test1.jpg", 22323, 4214124, Date()))
    val profile2 = profileService.create(
      Profile(profileService.generateId(), "test2.jpg", 2444, 1111, Date()))

    val profiles = profileService.findAll()
    Assertions.assertThat(profiles).extracting("id")
      .containsExactlyInAnyOrder(profile1?.id, profile2?.id)
    Assertions.assertThat(profiles).extracting("filename")
      .containsExactlyInAnyOrder(profile1?.filename, profile2?.filename)
  }
  @Test
  fun findById() {
    val profile = profileService.create(
      Profile(id = profileService. generateId(), height = 11, width = 22, filename = "test1.jpg", photoDate = Date()))
    assertNotNull(profileService.findById(profile!!.id))
  }
  @Test
  fun create() {
    val profile = profileService.create(
      Profile(id = profileService. generateId(), height = 11, width = 22, filename = "test1.jpg", photoDate = Date()))
    assertNotNull(profileService.findById(profile!!.id))
  }
  @Test
  fun getImageMetadata() {
    val profile = profileService.getImageMetadata(
      UploadInfoDTO("20170418_212607.jpg", "upload"))
    assertNotNull(profile)
  }
}