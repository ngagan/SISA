package SISA.example.SISA.repo;

import org.springframework.stereotype.Repository;
import SISA.example.SISA.model.AnalysisResponse;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AnalysisRepository {
    private final List<AnalysisResponse> history = new ArrayList<>();

    public void save(AnalysisResponse response) {
        history.add(response);
    }
}