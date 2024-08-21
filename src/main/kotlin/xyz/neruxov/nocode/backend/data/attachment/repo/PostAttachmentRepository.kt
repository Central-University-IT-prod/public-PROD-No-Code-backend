package xyz.neruxov.nocode.backend.data.attachment.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.attachment.model.PostAttachment

interface PostAttachmentRepository : JpaRepository<PostAttachment, Long>