package ai.core.strategy;

import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;
import reactor.core.publisher.Flux;

import java.security.NoSuchAlgorithmException;

/**
 * @author hxdu5
 * @since 2025/7/9 13:53
 */
public interface ModelInvoker {

    boolean supports(ModelInfo modelInfo);

    /**
     * 同步调用，返回完整响应字符串
     */
    String invoke(AiMessageDTO aiMessageDTO);

    /**
     * 流式调用，返回Flux<String>用于实时推送
     */
    default Flux<String> invokeStream(AiMessageDTO aiMessageDTO) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException("当前调用器不支持流式调用");
    }
}
