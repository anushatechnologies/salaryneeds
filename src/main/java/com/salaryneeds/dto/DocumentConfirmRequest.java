package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.salaryneeds.entity.enums.DocType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentConfirmRequest {

    @NotNull(message = "Document type is required")
    @JsonProperty("doc_type")
    @JsonAlias({"docType", "doc_type"})
    private DocType docType;

    @NotBlank(message = "S3 key is required")
    @JsonProperty("s3_key")
    @JsonAlias({"s3Key", "s3_key", "fileKey", "file_key", "key"})
    private String s3Key;

    @JsonProperty("file_size_bytes")
    @JsonAlias({"fileSizeBytes", "file_size_bytes"})
    private Long fileSizeBytes;

    @JsonProperty("original_filename")
    @JsonAlias({"originalFilename", "original_filename"})
    private String originalFilename;
}
