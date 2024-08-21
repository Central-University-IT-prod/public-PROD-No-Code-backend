package xyz.neruxov.nocode.backend.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import xyz.neruxov.nocode.backend.service.LinkService

@RestController
@RequestMapping("/api/link")
class LinkController(
    val linkService: LinkService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.TEMPORARY_REDIRECT)
    fun getLink(
        @RequestParam code: String
    ) = linkService.getLink(code)

}