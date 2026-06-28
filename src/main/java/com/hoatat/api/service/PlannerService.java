package com.hoatat.api.service;

import com.hoatat.api.dto.WorkflowNode;
import com.hoatat.api.dto.WorkflowPlan;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PlannerService {

    public WorkflowPlan createPlan(String userInput) {
        String symbol = extractSymbol(userInput);
        String interval = extractInterval(userInput);

        return new WorkflowPlan(
                List.of(
                        new WorkflowNode(
                                "price",
                                "get_price",
                                Map.of("symbol", symbol),
                                List.of()
                        ),

                        new WorkflowNode(
                                "ohlc",
                                "get_ohlc",
                                Map.of(
                                        "symbol", symbol,
                                        "interval", interval,
                                        "limit",  50),
                                List.of()
                        ),

                        new WorkflowNode(
                                "trend",
                                "analyze_trend",
                                Map.of(),
                                List.of("ohlc")
                        ),

                        new WorkflowNode(
                                "final",
                                "final_answer",
                                Map.of(),
                                List.of("price", "trend")
                        )
                )
        );
    }

    private String extractSymbol(String input) {

        if(input.contains("BTC")) return "BTC";
        if(input.contains("ETH")) return "ETH";
        if(input.contains("ADA")) return "ADA";
        if(input.contains("ZEC")) return "ZEC";
        if(input.contains("DASH")) return "DASH";
        if(input.contains("XMR")) return "XMR";

        return "BTC";
    }

    private String extractInterval(String input) {

        if(input.contains("15m")) return "15m";
        if(input.contains("30m")) return "30m";
        if(input.contains("1h")) return "1h";
        if(input.contains("4h")) return "4h";

        return "1h";
    }
}
