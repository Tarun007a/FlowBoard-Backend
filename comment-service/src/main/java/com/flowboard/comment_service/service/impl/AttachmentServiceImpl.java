package com.flowboard.comment_service.service.impl;

import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.exception.FileException;
import com.flowboard.comment_service.mapper.Mapper;
import com.flowboard.comment_service.repository.AttachmentRepository;
import com.flowboard.comment_service.service.AttachmentService;
import com.flowboard.comment_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final Mapper<Attachment, AttachmentResponseDto> attachmentResponseMapper;
    private final Mapper<AttachmentRequestDto, Attachment> attachmentRequestMapper;

    @Override
    public AttachmentResponseDto uploadAttachment(MultipartFile file, AttachmentRequestDto attachmentRequestDto) {
        String fakeUrl = "http://localhost/files/" + file.getOriginalFilename();

        Attachment attachment = attachmentRequestMapper.mapTo(attachmentRequestDto);

        long maxSize = AppConstants.maxFileSize;
        long sizeInKb = file.getSize() / 1024;

        List<String> allowedFileFormat = AppConstants.allowedFileFormat;

        String fileFormat = file.getContentType();

        if(file.isEmpty()) {
            throw new FileException("Empty file cannot be uploaded");
        }

        if(sizeInKb > maxSize) {
            throw new FileException("Maximum file size is " + maxSize + "kb");
        }

        if(!allowedFileFormat.contains(fileFormat)) {
            throw new FileException("Invalid file format, allowed format " + allowedFileFormat.toString());
        }


        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileUrl(fakeUrl);
        attachment.setFileType(fileFormat);
        attachment.setSizeKb(sizeInKb);

        Attachment savedAttachment = attachmentRepository.save(attachment);

        return attachmentResponseMapper.mapTo(savedAttachment);
    }

    @Override
    public List<AttachmentResponseDto> getAttachmentsByCard(Integer cardId) {
        List<Attachment> attachment = attachmentRepository.findByCardId(cardId);
        return attachment.stream()
                .map(attachmentResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        attachmentRepository.deleteByAttachmentId(attachmentId);
    }
}