package com.ai.mcpclient.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/nexis-ai")
public class ClientController {

    @Autowired
    private ChatClient chatClient;

    private final ChatMemory chatMemory = new InMemoryChatMemory();

//    @GetMapping("/chat")
//    public String chat(@RequestParam(value = "msg",defaultValue = "今天天气如何？") String msg) {
//        String response = chatClient.prompt()
//                .user(msg)
//                .call()
//                .content();
//        System.out.println("响应结果: " + response);
//        return response;
//    }

    /**
     * 对话API
     */
    @GetMapping("/chat")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "id") String documentId,
                                             @RequestParam(value = "prompt") String prompt) {

        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory, documentId, 10);
        return this.chatClient.prompt(prompt)
                .advisors(messageChatMemoryAdvisor)
                .stream()
                .chatResponse();
    }

}
