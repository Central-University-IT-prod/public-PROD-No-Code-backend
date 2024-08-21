package xyz.neruxov.nocode.backend.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import xyz.neruxov.nocode.backend.data.post.request.CreatePostRequest
import xyz.neruxov.nocode.backend.data.post.request.EditPostRequest
import xyz.neruxov.nocode.backend.data.post.request.TestPostRequest
import xyz.neruxov.nocode.backend.data.post.request.UploadAttachmentsRequest
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.service.PostService

@RestController
@RequestMapping("/api/post")
class PostController(private val postService: PostService) {

    @GetMapping
    fun getPostByDateRange(
        @RequestParam fromDate: Long,
        @RequestParam toDate: Long,
        @RequestParam organizationId: Long
    ) = postService.getPostsByDateRange(fromDate, toDate, organizationId)

    @GetMapping("/{id}")
    fun getPostById(
        @PathVariable id: Long
    ) = postService.getPostById(id)

    @PostMapping
    fun createPost(
        @RequestBody @Valid request: CreatePostRequest,
        @AuthenticationPrincipal user: User
    ) = postService.createPost(request, user)

    @PostMapping("/attachments")
    fun uploadAttachments(
        @RequestBody request: UploadAttachmentsRequest
    ) = postService.uploadAttachments(request.organizationId, request.attachmentsBody)

    @GetMapping("/attachment/{id}")
    fun getAttachmentById(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ) = postService.getAttachmentById(user, id)

    @PatchMapping("/{id}")
    fun editPost(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: EditPostRequest
    ) = postService.editPost(user, id, request)

    @DeleteMapping("/{id}")
    fun cancelPost(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ) = postService.cancelPost(user, id)

    @GetMapping("/{id}/statistics")
    fun getStatistics(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ) = postService.getStatistics(user, id)

    @PostMapping("/test")
    fun testPost(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid body: TestPostRequest
    ) = postService.testPost(user, body.integrationId, body.organizationId, body.attachments, body.body)

    @PostMapping("/{id}/publish")
    fun publishPost(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ) = postService.publishPost(user, id)

}