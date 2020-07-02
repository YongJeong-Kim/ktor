package com.example.routes

import com.example.entity.UploadInfoDTO
import com.example.service.ProfileService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Routing.profile(profileService: ProfileService, uploadPath: String) {
  route("/profile") {
    get {
      call.respond(profileService.findAll())
    }

    post {
      transaction {
        runBlocking {
          val uploadInfoDTO = upload(call.receiveMultipart(), uploadPath)

          if (isFilenameEquals(uploadInfoDTO)) {
            coroutineScope {
              val imageMetadata = async { profileService.getImageMetadata(uploadInfoDTO) }
              val pixelStatistics = async { profileService.getPixelStatistics(uploadInfoDTO) }
              val histogram = async { profileService.getHistogram(uploadInfoDTO) }
              val mergedProfile =
                profileService.mergeProfile(imageMetadata.await(), pixelStatistics.await(), histogram.await())
              val profile = profileService.create(mergedProfile)
              call.respond(HttpStatusCode.Created, profile)
            }
          } else
            call.respond(HttpStatusCode.BadRequest)
        }
      }
    }

    get("/{id}") {
      val id = call.parameters["id"]!!.toString()
      profileService.findById(id)?.let { call.respond(HttpStatusCode.OK, it) }
        ?: call.respond(HttpStatusCode.NotFound)
    }
  }
}

private suspend fun upload(multipart: MultiPartData, uploadPath: String): UploadInfoDTO {
  val uploadInfoDTO = UploadInfoDTO()

  multipart.forEachPart { part ->
    if (part is PartData.FormItem) {
      if (part.name == "filename")
        uploadInfoDTO.apply {
          filenameOfFormItem = part.value
        }
    } else if (part is PartData.FileItem) {
      val name = part.originalFileName!!
      val file = File(uploadPath, name)

      uploadInfoDTO.apply {
        filename = name
        this.uploadPath = uploadPath
      }
      part.streamProvider().use { its ->
        file.outputStream().buffered().use {
          its.copyTo(it)
        }
      }
    }
    part.dispose()
  }
  return uploadInfoDTO
}

private fun isFilenameEquals(uploadInfoDTO: UploadInfoDTO) =
  uploadInfoDTO.filename == uploadInfoDTO.filenameOfFormItem