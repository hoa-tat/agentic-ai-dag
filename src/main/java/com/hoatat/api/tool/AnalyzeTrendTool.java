package com.hoatat.api.tool;

import com.hoatat.api.dto.ResourceType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnalyzeTrendTool implements AgentTool {

    private final ChatClient chatClient;

    public AnalyzeTrendTool(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getName() {
        return "analyze_trend";
    }

    @Override
    public String getDescription() {
        return "Analyze trend from OHLC data";
    }

    @Override
    public ResourceType getResource() {
        return ResourceType.INTERNAL;
    }

    @Override
    public Object execute(java.util.Map<String, Object> params, Map<String,Object> context) {
        String ohlc = (String) context.get("ohlc");

        String prompt = """
                Analyze trend from this data.
                
                Return:
                - trend
                - explanation
                
                Data:
                %s
                """.formatted(ohlc);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
