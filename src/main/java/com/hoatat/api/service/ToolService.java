package com.hoatat.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolService {

    public String comparePrice(List<String> symbols) {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> results = new ArrayList<>();

        for (String symbol : symbols) {
            String url = "https://api.binance.com/api/v3/ticker/price?symbol="
                    + symbol + "USDT";

            String res = restTemplate.getForObject(url, String.class);

            try {
                JsonNode node = mapper.readTree(res);
                Map<String, Object> data = new HashMap<>();
                data.put("symbol", symbol);
                data.put("price", node.get("price").asText());
                results.add(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results.toString();
    }

    public String getOHLC(String input, String interval, int limit) {

        String symbol = extractSymbol(input);
        String url = String.format(
                "https://api.binance.com/api/v3/klines?symbol=%sUSDT&interval=%s&limit=%d",
                symbol, interval, limit
        );

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    public String getPrice(String input) {

        String symbol = extractSymbol(input);
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol + "USDT";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    private String extractSymbol(String input) {
        if (input.contains("btc")) return "BTC";
        if (input.contains("eth")) return "ETH";
        if (input.contains("ada")) return "ADA";
        if (input.contains("zec")) return "ZEC";
        return input; // default
    }
}
