package com.ai.mcpclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/nexis-ai")
public class ClientController {

    @Autowired
    private ChatClient chatClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatMemory chatMemory = new InMemoryChatMemory();


    // 流式对话接口
    @GetMapping(value = "/chat", produces = "text/event-stream")
    public ResponseBodyEmitter streamChat(
            @RequestParam("documentId") String documentId,
            @RequestParam("prompt") String prompt) {

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        chatClient.prompt(prompt)
                .advisors(new MessageChatMemoryAdvisor(chatMemory, documentId, 10))
                .stream()
                .chatResponse()
                .subscribe(
                        chatResponse -> sendSSEData(emitter, "message", chatResponse),
                        error -> handleError(emitter, error),
                        () -> completeRequest(emitter)
                );

        return emitter;
    }

    private void sendSSEData(ResponseBodyEmitter emitter, String event, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            String formatted = String.format("event: %s\ndata: %s\n\n", event, json);
            emitter.send(formatted);
        } catch (JsonProcessingException e) {
            handleError(emitter, new Exception("数据序列化失败", e));
        } catch (IOException e) {
            handleError(emitter, e);
        }
    }

    private void handleError(ResponseBodyEmitter emitter, Throwable error) {
        try {
            String errorData = objectMapper.writeValueAsString(
                    Map.of("error", error.getMessage())
            );
            emitter.send("event: error\ndata: " + errorData + "\n\n");
        } catch (Exception e) {
            emitter.completeWithError(e);
        } finally {
            completeRequest(emitter);
        }
    }

    private void completeRequest(ResponseBodyEmitter emitter) {
        try {
            emitter.send("event: complete\ndata: {}\n\n");
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
