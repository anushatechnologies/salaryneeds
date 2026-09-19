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
public class UploadUrlRequest {

    @NotNull(message = "Document type is required")
    @JsonProperty("doc_type")
    @JsonAlias({"docType", "doc_type"})
    private DocType docType;

    @NotBlank(message = "Filename is required")
    private String filename;

    @JsonProperty("content_type")
    @JsonAlias({"contentType", "content_type"})
    private String contentType;
}
