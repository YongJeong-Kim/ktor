package com.example.routes

import com.example.service.ProfileService
import io.ktor.application.call
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.profile(profileService: ProfileService, uploadPath: String) {
  route("/profile") {
    get("/") {
      call.respond(profileService.findAll())
    }
    post("/") {
      val multipart = call.receiveMultipart()
      val filename = profileService.uploadImage(multipart, uploadPath)
      val uploadedMetadata = profileService.getImageMetadata(filename, uploadPath)
      profileService.create(uploadedMetadata)

      call.respond(profileService.findAll())
    }
    get("/{id}") {
      val id = call.parameters["id"]!!.toInt()
      call.respond(profileService.findById(id))
    }
  }
}