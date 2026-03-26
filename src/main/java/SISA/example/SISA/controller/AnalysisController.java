package SISA.example.SISA.controller;

import SISA.example.SISA.model.AnalysisRequest;
import SISA.example.SISA.model.AnalysisResponse;
import SISA.example.SISA.service.DetectionService;
import SISA.example.SISA.service.FileService;
import SISA.example.SISA.service.AIService;
import SISA.example.SISA.service.PolicyService;
import SISA.example.SISA.repo.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analyze")
@CrossOrigin(origins = "*")
public class AnalysisController {

    @Autowired private FileService fileService;
    @Autowired private DetectionService detectionService;
    @Autowired private AIService aiService;
    @Autowired private PolicyService policyService;
    @Autowired private AnalysisRepository repo;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> analyze(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") AnalysisRequest request) {

        try {
            // Safeguard for options
            if (request.getOptions() == null) {
                request.setOptions(new AnalysisRequest.Options());
            }

            // 1. EXTRACTION & NORMALIZATION (Fixed to handle weird encoding/BOM)
            String rawContent = extractAndNormalizeContent(file, request);

            if (rawContent == null || rawContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No valid content found."));
            }

            // 2. DETECTION ENGINE
            List<AnalysisResponse.Finding> findings = detectionService.scan(rawContent);

            // 3. RISK ENGINE (Weights: 15, 10, 5, 2)
            int score = calculateScore(findings);
            String level = (score >= 20) ? "HIGH" : (score >= 10 ? "MEDIUM" : "LOW");

            // 4. POLICY ENGINE
            String finalContent = policyService.applyPolicy(rawContent, request.getOptions(), level);

            if (finalContent != null && finalContent.startsWith("ACCESS BLOCKED")) {
                return ResponseEntity.status(403).body(Map.of("message", finalContent, "risk_level", level));
            }

            // 5. AI INSIGHTS
            List<String> insights = aiService.getInsights(findings);

            // 6. RESPONSE MAPPING
            AnalysisResponse response = AnalysisResponse.builder()
                    .summary("Analysis completed with " + findings.size() + " findings.")
                    .findings(findings)
                    .risk_score(score)
                    .risk_level(level)
                    .insights(insights)
                    .masked_content(finalContent)
                    .action(request.getOptions().isMask() ? "masked" : "none")
                    .build();

            repo.save(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Analysis failed", "details", e.getMessage()));
        }
    }

    /**
     * Extracts content and sanitizes it from encoding artifacts like BOM and null bytes
     * that cause Regex failures in some text editors.
     */
    private String extractAndNormalizeContent(MultipartFile file, AnalysisRequest request) throws Exception {
        String content = "";

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename().toLowerCase();

            if (fileName.endsWith(".txt") || fileName.endsWith(".log")) {
                // Force UTF-8 and strip out Byte Order Marks (BOM) or null bytes
                content = new String(file.getBytes(), StandardCharsets.UTF_8)
                        .replace("\uFEFF", "") // Remove BOM
                        .replace("\u0000", ""); // Remove Null characters
            } else {
                content = fileService.extractText(file);
            }
        } else {
            content = request.getContent();
        }

        return (content != null) ? content.trim() : "";
    }

    private int calculateScore(List<AnalysisResponse.Finding> findings) {
        return findings.stream().mapToInt(f -> {
            switch (f.getRisk().toLowerCase()) {
                case "critical": return 15;
                case "high": return 10;
                case "medium": return 5;
                case "low": return 2;
                default: return 0;
            }
        }).sum();
    }
}
