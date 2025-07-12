package ai.core.strategy.impl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import ai.annotation.AiModelStrategy;


/**
 * @author hxdu5
 * @since 2025/7/11 16:02
 */
@AiModelStrategy(modelType = "DeepSeek", type = AiModelStrategy.StrategyType.RESPONSE_PARSER)
public class DeepSeekParser implements ModelResponseParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String parseSync(String responseBody, ModelInfo modelInfo) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String parseStreamLine(String line, ModelInfo modelInfo) {
        try {
            if (line.startsWith("data: ")) {
                line = line.substring(6).trim();
            }
            JsonNode node = mapper.readTree(line);
            return node.path("choices").get(0).path("delta").path("content").asText("");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String parseWebSocketMessage(String message, ModelInfo modelInfo) {
        // 对于DeepSeek，WebSocket和HTTP使用相同的解析格式
        return parseSync(message, modelInfo);
    }
}
