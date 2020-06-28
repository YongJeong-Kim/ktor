package routes

import com.example.DatabaseFactory
import com.example.entity.Profile
import com.example.entity.Profiles
import com.example.service.ProfileService
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import java.awt.PageAttributes
import java.io.File
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull


internal class ProfileRouteKtTest {
  private val profileService = ProfileService()

  @Before
  fun be() {
    DatabaseFactory.init()
    transaction { Profiles.deleteAll() }
  }

  @Test
  fun `get profile test`() {
    val profile1 = Profile(profileService.generateId(), "test1.jpg", 111, 222, Date())
    val profile2 = Profile(profileService.generateId(), "test2.jpg", 333, 444, Date())

    createProfile(profile1)
    createProfile(profile2)

    val profiles = get("/profile")
      .then()
      .statusCode(200)
      .extract()
      .`as`(Any::class.java) as List<Profile>

    assertThat(profiles).extracting("id").containsExactlyInAnyOrder(profile1.id, profile2.id)
  }

  @Test
  fun `get profile id 테스트`() {
    val profile = createProfile(
      Profile(profileService.generateId(), "test1.jpg", 111, 222, Date()))
    val newProfile = get("/profile/{id}", profile.id)
      .then()
      .statusCode(200)
      .extract()
      .`as`(Any::class.java) as List<Profile>
    assertThat(newProfile).extracting("id").containsExactlyInAnyOrder(profile.id)
  }

  @Test
  fun `파일 업로드`() {
    val profile = given()
      .contentType("multipart/form-data")
      .multiPart(File("C:\\Users\\yongyong\\Desktop\\20170418_212607.jpg"))
      .`when`()
      .post("/profile")
      .then()
      .extract()
      .`as`(Profile::class.java)

    assertNotNull(profile)
  /*  val response: Response = given()
      .multiPart(
        MultiPartSpecBuilder("upload/20170418_212607.jpg")
          .fileName("20170418_212607.jpg") // controlName is the name of the
          .controlName("file")
          .mimeType("image/jpg")
          .header("Content-Type", "multipart/form-data")
          .build()
      ).param("documentType", "MyCat") // You can omit this if U want
      .`when`()
      .post("/profile")
      .then()
      .extract()
      .response()*/
  }

  private fun createProfile(profile: Profile): Profile {
    return given()
      .contentType(ContentType.JSON)
      .body(profile)
      .`when`()
      .post("/profile/create")
      .then()
      .statusCode(201)
      .extract()
      .`as`(Profile::class.java)
  }
}