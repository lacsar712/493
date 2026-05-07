package com.example.tcm.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class SuggestionService {

    private static final Map<String, String> DEFAULT_SUGGESTIONS = new HashMap<>();

    static {
        DEFAULT_SUGGESTIONS.put("平和体质", "饮食均衡，规律作息，保持心情愉悦。");
        DEFAULT_SUGGESTIONS.put("气虚体质", "多吃补气食物（如山药、大枣），避免过度劳累，适度运动。");
        DEFAULT_SUGGESTIONS.put("痰湿体质", "饮食清淡，少食肥甘厚味，加强运动出汗，祛除湿气。");
    }

    public String getSuggestion(String bodyType) {
        return DEFAULT_SUGGESTIONS.getOrDefault(bodyType, "保持健康生活方式，如有不适请及时就医。");
    }
}
