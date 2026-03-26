package SISA.example.SISA.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

@Service
public class FileService {
    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename != null && (filename.endsWith(".log") || filename.endsWith(".txt"))) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        return tika.parseToString(file.getInputStream());
    }
}
