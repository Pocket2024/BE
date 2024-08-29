package Project.Pocket.ClovaOCR.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Value("${clova.ocr.api.url}")
    private String apiUrl;
    @Value("${clova.ocr.api.key}")
    private String apiKey;
    @Value("${gpt.api.key}")
    private String gptApiKey;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String ocrResponse;
        try {
            // 1. 클로바 OCR API 호출
            File fileToUpload = convertMultipartFile(file);
            ocrResponse = callOcr(fileToUpload, createMetadata(file));
            if (fileToUpload.exists()) {
                fileToUpload.delete();  // 임시 파일 삭제
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing OCR: " + e.getMessage()));
        }

        // 2. ChatGPT API 호출하여 공연 정보 추출
        Map<String, String> extractedData = extractPerformanceInfo(ocrResponse);

        // 3. 결과 반환
        return ResponseEntity.ok(extractedData);
    }

    private File convertMultipartFile(MultipartFile multipartFile) throws IOException {
        // 임시 파일을 생성
        File tempFile = File.createTempFile("temp-file", ".tmp");
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    private String callOcr(File file, String metadataJson) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-OCR-SECRET", apiKey);

        // 파일 및 메타데이터 추가
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", metadataJson);
        body.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return response.getBody();
    }

    private String createMetadata(MultipartFile file) {
        // 파일의 형식과 이름 추출
        String fileName = file.getOriginalFilename();
        String fileFormat = fileName != null ? fileName.substring(fileName.lastIndexOf('.') + 1) : "Unknown";

        // 메타데이터 생성
        String requestId = UUID.randomUUID().toString(); // UUID로 고유 요청 ID 생성
        long timestamp = System.currentTimeMillis(); // 현재 시간(밀리초 단위)

        // 메타데이터 JSON 구조 생성
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "V2");
        metadata.put("requestId", requestId);
        metadata.put("timestamp", timestamp);

        // 이미지 정보 추가
        Map<String, String> imageInfo = new HashMap<>();
        imageInfo.put("format", fileFormat);
        imageInfo.put("name", fileName);

        metadata.put("images", Collections.singletonList(imageInfo)); // `images` 배열로 추가

        // JSON 문자열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
        } catch (IOException e) {
            throw new RuntimeException("Error creating metadata JSON", e);
        }

        return metadataJson;
    }

    // ChatGPT API를 호출하여 공연 정보 추출
    private Map<String, String> extractPerformanceInfo(String ocrText) {
        RestTemplate restTemplate = new RestTemplate();
        String gptApiUrl = "https://api.openai.com/v1/chat/completions";

        // GPT API 요청 본문 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant."),
                Map.of("role", "user", "content", "Extract the Ticket title, date (format it as 'yyyy.mm.dd'), location, and seat from the following text:\n\n" + ocrText)
        ));
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.5);


        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        // 제네릭 타입을 제거한 HttpEntity 선언
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // ChatGPT API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                gptApiUrl,
                HttpMethod.POST,
                request,
                Map.class
        );

        // 응답에서 공연 정보 추출 (예: content에 따라 parsing 필요)
        Map<String, Object> choices = (Map<String, Object>) ((List<Object>) response.getBody().get("choices")).get(0);
        String chatGptResponse = (String) ((Map<String, Object>) choices.get("message")).get("content");

        // 공연 정보 추출 (간단한 파싱)
        Map<String, String> extractedData = new HashMap<>();
        extractedData.put("title", extractValue(chatGptResponse, "Ticket title"));
        extractedData.put("date", extractValue(chatGptResponse, "Date"));
        extractedData.put("location", extractValue(chatGptResponse, "Location"));
        extractedData.put("seat", extractValue(chatGptResponse, "Seat"));

        return extractedData;
    }

    private String extractValue(String response, String label) {
        Pattern pattern = Pattern.compile(label + ": (.+)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Not Found";
    }
}
