package routes

import com.example.DatabaseFactory
import com.example.entity.Profile
import com.example.entity.Profiles
import com.example.entity.UploadInfoDTO
import com.example.service.ProfileService
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import java.io.File
import kotlin.test.Test


internal class ProfileRouteKtTest {
  private val profileService = ProfileService()
  private val uploadPath = "C:\\Users\\yongyong\\Desktop"

  @Before
  fun be() {
    DatabaseFactory.init()
    transaction { Profiles.deleteAll() }
  }

  @Test
  fun getProfileRoot() {
    val profile1 = createProfile(UploadInfoDTO("20170418_212607.jpg", uploadPath))
    val profile2 = createProfile(UploadInfoDTO("20170418_212608.jpg", uploadPath))

    val profiles = get("/profile")
      .then()
      .statusCode(200)
      .extract()
      .`as`(Any::class.java) as List<Profile>

    assertThat(profiles).extracting("id").containsExactlyInAnyOrder(profile1.id, profile2.id)
  }

  @Test
  fun getProfileId() {
    val profile = createProfile(UploadInfoDTO("20170418_212607.jpg", uploadPath))
    val newProfile = get("/profile/{id}", profile.id)
      .then()
      .statusCode(200)
      .extract()
      .`as`(Any::class.java) as List<Profile>
    assertThat(newProfile).extracting("id").containsExactlyInAnyOrder(profile.id)
  }

  @Test
  fun postProfileRoot() {
    val profile = createProfile(UploadInfoDTO("20170418_212607.jpg", uploadPath))
    val newProfile = get("/profile/{id}", profile.id)
      .then()
      .statusCode(200)
      .extract()
      .`as`(Any::class.java) as List<Profile>
    assertThat(newProfile).extracting("id").containsExactlyInAnyOrder(profile.id)
  }

  private fun createProfile(uploadInfoDTO: UploadInfoDTO): Profile {
    /*return given()
      .contentType(ContentType.JSON)
      .body(profile)
      .`when`()
      .post("/profile/create")
      .then()
      .statusCode(201)
      .extract()
      .`as`(Profile::class.java)*/
    return given()
      .contentType("multipart/form-data")
      .multiPart(File("${uploadInfoDTO.uploadPath}/${uploadInfoDTO.filename}"))
      .formParam("filename", uploadInfoDTO.filename)
      .`when`()
      .post("/profile")
      .then()
      .extract()
      .`as`(Profile::class.java)
  }
}