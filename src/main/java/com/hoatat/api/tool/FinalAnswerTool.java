package com.hoatat.api.tool;

import com.hoatat.api.dto.ResourceType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FinalAnswerTool implements AgentTool {
    private final ChatClient chatClient;

    public FinalAnswerTool(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getName() {
        return "final_answer";
    }

    @Override
    public String getDescription() {
        return "Generate final answer";
    }

    @Override
    public ResourceType getResource() {
        return ResourceType.OPENAI;
    }

    @Override
    public Object execute(Map<String,Object> params, Map<String,Object> context) {

        String prompt = """
            Hãy trả lời bằng tiếng Việt.
        
            Dữ liệu công cụ:
    
            Giá:
            %s
    
            Xu hướng:
            %s
    
            Viết câu trả lời ngắn gọn,
            dễ hiểu cho nhà đầu tư.
        """.formatted(context.get("price"), context.get("trend")
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
