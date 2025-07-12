package ai.core.strategy.impl.requestBody;

import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.RequestBodyBuilderStrategy;
import com.iflytek.obu.mark.ai.utils.MapUtils;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;
import ai.annotation.AiModelStrategy;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author hxdu5
 * @since 2025/7/11 09:49
 */
@AiModelStrategy(modelType = "中海油-DS", type = AiModelStrategy.StrategyType.REQUEST_BODY_BUILDER)
public class ZhyDeepseekRequestBodyBuilder implements RequestBodyBuilderStrategy {
    @Override
    public boolean supports(ModelInfo modelInfo) {
        return "中海油-DS".equalsIgnoreCase(modelInfo.getProvider());
    }

    @Override
    public Map<String, Object> builderRequestBody(ModelInfo modelInfo, AiMessageDTO aiMessage) throws NoSuchAlgorithmException {
        return MapUtils.mapOf(
                "prompt", aiMessage.getPrompt(),
                "max_tokens", modelInfo.getParams().getMaxTokens(),
                "stream", modelInfo.getParams().getStream(),
                "do_sample", true,
                "repetition_penalty", modelInfo.getParams().getExtraParams().get("repetition_penalty"),
                "temperature", modelInfo.getParams().getTemperature(),
                "top_p", modelInfo.getParams().getExtraParams().get("top_p"),
                "top_k", modelInfo.getParams().getExtraParams().get("top_k"),
                "model", modelInfo.getParams().getModelName()
        );
    }
}