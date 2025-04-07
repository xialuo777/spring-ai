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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/nexis-ai")
public class ClientController {

    @Autowired
    private ChatClient chatClient;

    private final ChatMemory chatMemory = new InMemoryChatMemory();

    @GetMapping("/chat")
    public SseEmitter chat( @RequestParam(defaultValue = "今天天气如何？") String message) {
        // 创建 SSE 发射器，设置超时时间（例如 1 分钟）
        SseEmitter emitter = new SseEmitter(60_000L);
//        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory, documentId, 10);
        String response = chatClient.prompt(message)
//                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
        // 发送 SSE 事件
        try {
            emitter.send(SseEmitter.event()
                    .data(response)
                    .id(String.valueOf(System.currentTimeMillis()))
                    .build());
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        emitter.complete();
        System.out.println("响应结果: " + response);
        return emitter;
    }

//    /**
//     * 对话API
//     */
//    @GetMapping("/chat")
//    public Flux<ChatResponse> generateStream(@RequestParam(value = "id") String documentId,
//                                             @RequestParam(value = "prompt") String prompt) {
//
//        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory, documentId, 10);
//        return this.chatClient.prompt(prompt)
//                .advisors(messageChatMemoryAdvisor)
//                .stream()
//                .chatResponse();
//    }

}
