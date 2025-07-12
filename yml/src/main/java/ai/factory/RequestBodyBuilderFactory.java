package ai.factory;

import com.iflytek.obu.mark.ai.config.ModelConfig;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.RequestBodyBuilderStrategy;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 请求体构建工厂
 * @author hxdu5
 * @since 2025/7/11
 */
@Slf4j
@Component
public class RequestBodyBuilderFactory {

    private final List<RequestBodyBuilderStrategy> requestBodyBuilderStrategyList;

    @Resource
    private ModelConfig modelConfig;

    public RequestBodyBuilderFactory(@Autowired List<RequestBodyBuilderStrategy> requestBodyBuilderStrategyList) {
        this.requestBodyBuilderStrategyList = requestBodyBuilderStrategyList;
    }

    public Map<String, Object> getRequestBody(String modelKey, AiMessageDTO aiMessageDTO) {
        try {
            // 根据modelKey获取ModelInfo
            ModelInfo modelInfo = modelConfig.getModelInfo(modelKey);
            return getRequestBody(modelInfo, aiMessageDTO);
        } catch (Exception e) {
            log.error("构建请求体失败，modelKey: {}", modelKey, e);
            throw new RuntimeException("构建请求体失败: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getRequestBody(ModelInfo modelInfo, AiMessageDTO aiMessageDTO) throws NoSuchAlgorithmException {
        try {
            for (RequestBodyBuilderStrategy requestBodyBuilderStrategy : requestBodyBuilderStrategyList) {
                if (requestBodyBuilderStrategy.supports(modelInfo)) {
                    log.debug("找到支持提供商 {} 的策略: {}", modelInfo.getProvider(), requestBodyBuilderStrategy.getClass().getSimpleName());
                    return requestBodyBuilderStrategy.builderRequestBody(modelInfo, aiMessageDTO);
                }
            }
            // 如果没有找到合适的策略，抛出异常
            throw new IllegalArgumentException("未找到支持提供商 '" + modelInfo.getProvider() + "' 的RequestBodyBuilderStrategy");
        } catch (NoSuchAlgorithmException e) {
            log.error("构建请求体时发生算法异常", e);
            throw new RuntimeException("构建请求体失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("构建请求体失败", e);
            throw new RuntimeException("构建请求体失败: " + e.getMessage(), e);
        }
    }
}