package Project.Pocket.chatbot.controller;

import Project.Pocket.chatbot.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService){
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestBody String question){
        try{
            String response = chatbotService.getChatbotResponse(question);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }
}
