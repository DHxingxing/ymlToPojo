package ai.core.strategy;

import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author hxdu5
 * @since 2025/7/9 17:28
 */
public interface RequestBodyBuilderStrategy {

    boolean supports(ModelInfo modelInfo);

    Map<String, Object> builderRequestBody(ModelInfo modelInfo, AiMessageDTO aiMessage) throws NoSuchAlgorithmException;

}
