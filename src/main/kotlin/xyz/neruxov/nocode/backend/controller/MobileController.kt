package xyz.neruxov.nocode.backend.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.neruxov.nocode.backend.data.mobile.request.SaveTokenRequest
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.service.MobileService

@RestController
@RequestMapping("/api/mobile")
class MobileController(
    val mobileService: MobileService
) {

    @PostMapping("/save-token")
    fun register(
        @AuthenticationPrincipal user: User,
        @RequestBody request: SaveTokenRequest
    ) = mobileService.saveToken(user, request.token)

}