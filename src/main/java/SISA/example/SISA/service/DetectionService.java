package SISA.example.SISA.service;

import SISA.example.SISA.model.AnalysisResponse.Finding;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
public class DetectionService {

    // Optimized Patterns to catch exactly what's in your screenshot
    private static final Pattern EMAIL_P = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE_P = Pattern.compile("\\b\\d{10}\\b");
    private static final Pattern PWD_P = Pattern.compile("(?i)(password|pass|pwd)[\\s:=]+[^\\s]+");
    private static final Pattern KEY_P = Pattern.compile("(?i)(api_key|sk-)[\\s:=]*[a-zA-Z0-9\\-]+");
    private static final Pattern TOKEN_P = Pattern.compile("(?i)(bearer|auth|token)[\\s:=]+[^\\s]+");

    public List<Finding> scan(String content) {
        List<Finding> findings = new ArrayList<>();
        if (content == null) return findings;

        String[] lines = content.split("\\r?\\n");
        int failedLogins = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;
            String lower = line.toLowerCase();

            // 1. Pattern Matching
            if (EMAIL_P.matcher(line).find()) findings.add(new Finding("email", "low", lineNum, "masked"));
            if (PHONE_P.matcher(line).find()) findings.add(new Finding("phone", "medium", lineNum, "masked"));
            if (PWD_P.matcher(line).find()) findings.add(new Finding("password", "critical", lineNum, "hidden"));
            if (KEY_P.matcher(line).find()) findings.add(new Finding("api_key", "high", lineNum, "hidden"));
            if (TOKEN_P.matcher(line).find()) findings.add(new Finding("token", "critical", lineNum, "hidden"));

            // 2. Log Analysis (Brute Force & Debug)
            if (lower.contains("failed login")) failedLogins++;
            if (lower.contains("stack trace") || lower.contains("nullpointer")) {
                findings.add(new Finding("stack_trace", "medium", lineNum, null));
            }
            if (lower.contains("debug") && lower.contains("pass")) {
                findings.add(new Finding("debug_leak", "high", lineNum, null));
            }
        }

        // 3. Brute Force Logic
        if (failedLogins >= 3) {
            findings.add(new Finding("brute_force_attack", "high", 0, "Repeated login failures"));
        }

        return findings;
    }
}