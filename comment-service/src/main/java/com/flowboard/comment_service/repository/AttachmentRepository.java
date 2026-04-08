package com.flowboard.comment_service.repository;

import com.flowboard.comment_service.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByCardId(Long cardId);

    Optional<Attachment> findByAttachmentId(Long attachmentId);

    void deleteByAttachmentId(Long attachmentId);
}