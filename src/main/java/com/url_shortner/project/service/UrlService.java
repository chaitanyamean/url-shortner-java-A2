package com.url_shortner.project.service;

import com.url_shortner.project.dto.BatchUrlRequestDto;
import com.url_shortner.project.dto.BatchUrlResponseDto;
import com.url_shortner.project.dto.UrlRequestDto;
import com.url_shortner.project.dto.UrlResponseDto;

import java.util.List;

public interface UrlService {
    UrlResponseDto shortenUrl(UrlRequestDto request, Long userId);

    List<BatchUrlResponseDto> shortenBatch(BatchUrlRequestDto request, Long userId);

    String getOriginalUrl(String shortCode, String password);

    List<UrlResponseDto> getUrlsByUserId(Long userId);

    void deleteUrl(String shortCode, Long userId);

    UrlResponseDto editUrl(String shortCode, UrlRequestDto request, Long userId);
}
