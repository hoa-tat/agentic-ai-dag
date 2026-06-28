package com.hoatat.api.tool;

import com.hoatat.api.dto.ResourceType;
import com.hoatat.api.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetOhlcTool implements AgentTool {

    private final ToolService toolService;

    @Override
    public String getName() {
        return "get_ohlc";
    }

    @Override
    public String getDescription() {
        return "Get OHLC data for a symbol. Input is the symbol, e.g. BTC, ETH.";
    }

    @Override
    public ResourceType getResource() {
        return ResourceType.BINANCE;
    }

    @Override
    public Object execute(java.util.Map<String, Object> params, Map<String,Object> context) {
        String result =
                toolService.getOHLC(
                        (String) params.get("symbol"),
                        (String) params.get("interval"),
                        ((Number) params.get("limit")).intValue()
                );

        return result;
    }
}
