package Project.Pocket.chatbot.service;

import Project.Pocket.Review.dto.ReviewDto;
import Project.Pocket.Review.dto.ReviewTranslateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GPTService {
    private final RestTemplate restTemplate;
    private final String gptApiUrl = "https://api.openai.com/v1/chat/completions";
    @Value("${gpt.api.key}")
    private String gptApiKey;
    @Autowired
    public GPTService(RestTemplate restTemplate){
        this.restTemplate =restTemplate;
    }

    public ReviewTranslateDto translateReview(ReviewTranslateDto reviewDto) {
        // 각 필드를 번역
        String translatedContent = callGptApi(reviewDto.getContent());
        String translatedTitle = callGptApi(reviewDto.getTitle());
        String translatedSeat = callGptApi(reviewDto.getSeat());

        // 번역된 데이터를 ReviewTranslateDto에 설정
        reviewDto.setContent(translatedContent);
        reviewDto.setTitle(translatedTitle);
        reviewDto.setSeat(translatedSeat);

        return reviewDto;
    }

    private String callGptApi(String text){
        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "system","content","Translate the following text to English: " + text)));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(gptApiUrl, request, Map.class);
        Map<String, Object> choices = (Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0);
        Map<String, Object> message = (Map<String, Object>) choices.get("message");
        return message.get("content").toString();
    }
}
