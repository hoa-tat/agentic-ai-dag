package com.hoatat.api.tool;

import com.hoatat.api.dto.ResourceType;
import com.hoatat.api.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ComparePriceTool implements AgentTool {

    private final ToolService toolService;

    @Override
    public String getName() {
        return "compare_price";
    }

    @Override
    public String getDescription() {
        return "Compare crypto prices";
    }

    @Override
    public ResourceType getResource() {
        return ResourceType.INTERNAL;
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String,Object> context) {

        List<String> symbols = (List<String>) params.get("symbols");

        return toolService.comparePrice(symbols);
    }
}
