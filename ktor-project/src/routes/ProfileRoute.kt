package com.example.routes

import com.example.entity.Profile
import com.example.entity.UploadInfoDTO
import com.example.service.ProfileService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

fun Routing.profile(profileService: ProfileService, uploadPath: String) {
  route("/profile") {
    get {
      call.respond(profileService.findAll())
    }

    post {
      runBlocking {
        val multipart = call.receiveMultipart()
        var uploadInfoDTO: UploadInfoDTO? = null

        multipart.forEachPart { part ->
          if (part is PartData.FormItem) {
            if (part.name == "filename")
              uploadInfoDTO = UploadInfoDTO(part.value, uploadPath)
          } else if (part is PartData.FileItem) {
            // retrieve file name of upload
            val name = part.originalFileName!!
            val file = File("$uploadPath/$name")

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

        val imageMetadata = async { profileService.getImageMetadata(uploadInfoDTO!!) }
        val pixelStatistics = async { profileService.getPixelStatistics(uploadInfoDTO!!) }
//            val histogram = profileService.getHistogram(uploadInfoDTO!!)
        val mergedProfile = profileService.mergeProfile(imageMetadata.await(), pixelStatistics.await(), "")
        val profile = profileService.create(mergedProfile)
        call.respond(profile!!)
        /*withContext(Dispatchers.Default) {
          uploadInfoDTO?.let {
            val imageMetadata = profileService.getImageMetadata(uploadInfoDTO!!)
            val pixelStatistics = profileService.getPixelStatistics(uploadInfoDTO!!)
//            val histogram = profileService.getHistogram(uploadInfoDTO!!)
            val mergedProfile = profileService.mergeProfile(imageMetadata, pixelStatistics, "")
            val profile = profileService.create(mergedProfile)
              ?: Profile("create file failed")
            call.respond(profile)
          } ?: call.respond(HttpStatusCode.BadRequest)
        }*/
      }
    }

    get("/{id}") {
      val id = call.parameters["id"]!!.toString()
      call.respond(HttpStatusCode.OK, profileService.findById(id))
    }
  }
}