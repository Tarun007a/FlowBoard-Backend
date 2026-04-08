package com.flowboard.comment_service.controller;

import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    public ResponseEntity<AttachmentResponseDto> upload(@RequestParam("file") MultipartFile file,
                                                        @Valid @RequestBody AttachmentRequestDto attachmentRequestDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(attachmentService.uploadAttachment(file, attachmentRequestDto));
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<AttachmentResponseDto>> getByCard(@PathVariable Integer cardId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(attachmentService.getAttachmentsByCard(cardId));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<String> delete(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.status(HttpStatus.OK).body("Attachment deleted");
    }
}
