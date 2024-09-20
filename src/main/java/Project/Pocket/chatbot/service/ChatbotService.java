package Project.Pocket.chatbot.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Value("${gpt.api.key}")
    private String gptApiKey;

    private static final String gptApiUrl = "https://api.openai.com/v1/chat/completions";

    public String getChatbotResponse(String prompt){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant"),
                Map.of("role", "user","content", prompt)
        ));
        requestBody.put("max_tokens", 1000);
        // 요청 헤더 설정
        org.springframework.http.HttpHeaders headers = new HttpHeaders();
       headers.set("Authorization", "Bearer " + gptApiKey);
       headers.set("Content-Type", "application/json");

       //요청 본문 설정
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
      //gpt api 호출
        ResponseEntity<Map> responseEntity = restTemplate.exchange(gptApiUrl, HttpMethod.POST,requestEntity,Map.class);
     // 응답
     if(responseEntity.getStatusCode() ==  HttpStatus.OK){
         Map<String, Object> responseBody = responseEntity.getBody();
         List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
         if(choices != null && !choices.isEmpty()){
             Map<String,Object> choice = choices.get(0);
             return (String) ((Map<String, Object>) choice.get("message")).get("content");
         }

     }
     return "Sorry , the request failed";

    }

}
