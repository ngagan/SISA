package SISA.example.SISA.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponse {
    private String summary;
    private String content_type;
    private List<Finding> findings;
    private int risk_score;
    private String risk_level;
    private String action;
    private List<String> insights;
    // ADD THIS LINE BELOW TO FIX THE ERROR
    private String masked_content;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Finding {
        private String type;
        private String risk;
        private Integer line;
        private String value;
    }
}