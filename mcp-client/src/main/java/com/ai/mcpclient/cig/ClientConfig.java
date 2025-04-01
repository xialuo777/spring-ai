package com.ai.mcpclient.cig;

import groovy.util.logging.Slf4j;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class ClientConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ToolCallbackProvider tools) {
        return ChatClient
                .builder(chatModel)
                .defaultTools(tools.getToolCallbacks())
                .build();
    }


//    @Bean
//    public List<ToolCallback> functionCallbacks(
//             McpSyncClient fileSysClient) {
//        return fileSysClient.listTools(null).tools().stream()
//                .map(tool -> new McpFunctionCallback(fileSysClient, tool))
//                .collect(Collectors.toList());
//
//    }

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpFileSysClient() {
        // 配置mcp客户端连接本地文件系统
        ServerParameters stdioParams = ServerParameters.builder("E:\\nodeJs\\npx.cmd")
                .args("-y", "@modelcontextprotocol/server-filesystem", "E:\\ccbft\\logs")
                .build();

        // 同步客户端构建（新增能力协商机制）
        McpSyncClient mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .roots(true)    // 启用文件系统访问能力
                        .sampling()     // 支持LLM响应采样
                        .build()).build();

        mcpClient.initialize(); // 显式初始化连接（新增强制调用）
        return mcpClient;
    }


}
