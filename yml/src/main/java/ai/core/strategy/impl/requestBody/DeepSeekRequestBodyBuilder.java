package ai.core.strategy.impl.requestBody;

import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.RequestBodyBuilderStrategy;
import com.iflytek.obu.mark.ai.utils.MapUtils;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;
import ai.annotation.AiModelStrategy;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iflytek.obu.mark.ai.utils.MapUtils.mapOf;

/**
 * @author hxdu5
 * @since 2025/7/11 09:49
 */
@AiModelStrategy(modelType = "DeepSeek", type = AiModelStrategy.StrategyType.REQUEST_BODY_BUILDER)
public class DeepSeekRequestBodyBuilder implements RequestBodyBuilderStrategy {
    @Override
    public boolean supports(ModelInfo modelInfo) {
        return "DeepSeek".equalsIgnoreCase(modelInfo.getProvider());
    }

    @Override
    public Map<String, Object> builderRequestBody(ModelInfo modelInfo, AiMessageDTO aiMessage) throws NoSuchAlgorithmException {
        List<Map<String, Object>> messages = Collections.singletonList(
                new HashMap<String, Object>() {{
                    put("role", "user");
                    put("content", aiMessage.getPrompt());
                }}
        );
        return MapUtils.mapOf(
                "model", modelInfo.getParams().getModelName(),
                "messages", messages,
                "stream", modelInfo.getParams().getStream(),
                "temperature", modelInfo.getParams().getTemperature(),
                "max_tokens", modelInfo.getParams().getMaxTokens(),
                "stream_options", mapOf("include_usage", true)
        );
    }
}