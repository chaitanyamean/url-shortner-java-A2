package com.url_shortner.project.controller;

import com.url_shortner.project.dto.BatchUrlRequestDto;
import com.url_shortner.project.dto.BatchUrlResponseDto;
import com.url_shortner.project.dto.UrlRequestDto;
import com.url_shortner.project.dto.UrlResponseDto;
import com.url_shortner.project.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponseDto> shortenUrl(@Valid @RequestBody UrlRequestDto request,
            java.security.Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        UrlResponseDto response = urlService.shortenUrl(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/shorten/batch")
    public ResponseEntity<java.util.List<BatchUrlResponseDto>> batchShorten(
            @Valid @RequestBody BatchUrlRequestDto request,
            java.security.Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        java.util.List<BatchUrlResponseDto> response = urlService.shortenBatch(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/redirect")
    public RedirectView redirect(@RequestParam String shortCode, @RequestParam String password) {
        System.out.println("shortCode: " + shortCode);
        String originalUrl = urlService.getOriginalUrl(shortCode, password);
        return new RedirectView(originalUrl);
    }

    @DeleteMapping("/shorten/{shortCode}")
    public ResponseEntity<java.util.Map<String, Object>> deleteUrl(@PathVariable String shortCode,
            java.security.Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        urlService.deleteUrl(shortCode, userId);
        return ResponseEntity.ok(java.util.Map.of(
                "status", "success",
                "message", "URL deleted successfully",
                "statusCode", 200));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<UrlResponseDto>> getUrlsByUserId(@PathVariable Long userId) {
        List<UrlResponseDto> urls = urlService.getUrlsByUserId(userId);
        return ResponseEntity.ok(urls);
    }

    @PutMapping("/edit/{shortCode}")
    public ResponseEntity<UrlResponseDto> editUrl(@PathVariable String shortCode,
            @Valid @RequestBody UrlRequestDto request,
            java.security.Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        UrlResponseDto response = urlService.editUrl(shortCode, request, userId);
        return ResponseEntity.ok(response);
    }

}
