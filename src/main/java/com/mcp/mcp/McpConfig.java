package com.mcp.mcp;

import com.mcp.service.SampleMyBatisService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider sampleTools(SampleMyBatisService sampleMyBatisService) {
        return MethodToolCallbackProvider.builder().toolObjects(sampleMyBatisService).build();
    }

}
