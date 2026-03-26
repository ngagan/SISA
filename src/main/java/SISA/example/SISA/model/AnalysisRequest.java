package SISA.example.SISA.model;

import lombok.Data;

@Data
public class AnalysisRequest {
    private String input_type;
    private String content;
    private Options options;

    @Data
    public static class Options {
        private boolean mask;
        private boolean block_high_risk;
        private boolean log_analysis;
    }
}