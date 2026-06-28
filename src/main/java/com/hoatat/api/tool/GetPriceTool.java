package com.hoatat.api.tool;

import com.hoatat.api.dto.ResourceType;
import com.hoatat.api.service.ToolService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetPriceTool implements AgentTool{

    private final ToolService toolService;

    public GetPriceTool(ToolService toolService) {
        this.toolService = toolService;
    }

    @Override
    public String getName() {
        return "get_price";
    }

    @Override
    public String getDescription() {
        return "Get latest crypto price. Input is the symbol, e.g. BTC, ETH.";
    }

    @Override
    public ResourceType getResource() {
        return ResourceType.BINANCE;
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String,Object> context) {
        String input = (String) params.get("symbol");
        // call tool service to get price
        return toolService.getPrice(input);
    }
}
