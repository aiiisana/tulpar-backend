package kz.diploma.tulpar.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.openai")
public class AiProperties {
    private String apiKey;
    private String baseUrl = "https://api.perplexity.ai";
    private String model = "sonar-small-chat";
}
