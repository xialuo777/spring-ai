package com.ai.mcpclient.controller;//package com.ai.mcpclient.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.AsyncContext;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.memory.InMemoryChatMemory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/nexis-ai")
//public class ClientController {
//
//    @Autowired
//    private ChatClient chatClient;
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    private final ChatMemory chatMemory = new InMemoryChatMemory();
//
//
//    // 流式对话接口
//    @GetMapping(value = "/chat", produces = "text/event-stream")
//    public void streamChat(
//            @RequestParam("id") String id,
//            @RequestParam("prompt") String prompt,
//            HttpServletResponse response,
//            AsyncContext asyncContext) throws IOException {
//
//        response.setContentType("text/event-stream");
//        response.setCharacterEncoding("UTF-8");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setHeader("Connection", "keep-alive");
//        response.setHeader("Access-Control-Allow-Origin", "*");
//
//        PrintWriter writer = response.getWriter();
//
//        this.chatClient.prompt(prompt)
//                .advisors(new MessageChatMemoryAdvisor(chatMemory, id, 10))
//                .stream()
//                .chatResponse()
//                .subscribe(
//                        chatResponse -> writeSSEEvent(writer, "message", chatResponse),
//                        error -> {
//                            writeSSEEvent(writer, "error", Map.of("error", error.getMessage()));
//                            asyncContext.complete();
//                        },
//                        () -> {
//                            writeSSEEvent(writer, "complete", null);
//                            asyncContext.complete();
//                        }
//                );
//    }
//
//    private void writeSSEEvent(PrintWriter writer, String event, Object data) {
//        try {
//            if (data != null) {
//                String json = objectMapper.writeValueAsString(data);
//                writer.write("event: " + event + "\n");
//                writer.write("data: " + json + "\n\n");
//                writer.flush();
//            }
//        } catch (JsonProcessingException e) {
//            writer.write("event: error\ndata: {\"error\":\"序列化失败\"}\n\n");
//            writer.flush();
//        }
//    }
//}
