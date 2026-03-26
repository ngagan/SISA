package SISA.example.SISA.service;

import SISA.example.SISA.model.AnalysisRequest;
import org.springframework.stereotype.Service;
@Service
public class PolicyService {
    public String applyPolicy(String content, AnalysisRequest.Options options, String riskLevel) {
        // REMOVE THE BLOCKING LOGIC HERE
        // We want the user to see the results, even if they are high risk.

        if (options.isMask()) {
            return content.replaceAll("(?i)(password|pass|pwd)[\\s:=]+[^\\s]+", "$1=********")
                    .replaceAll("(?i)(api_key|sk-)[\\s:=]*[a-zA-Z0-9\\-]+", "api_key=********")
                    .replaceAll("(?i)(bearer|auth|token)[\\s:=]+[^\\s]+", "auth_token=********")
                    .replaceAll("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}", "********");
        }
        return content;
    }
}