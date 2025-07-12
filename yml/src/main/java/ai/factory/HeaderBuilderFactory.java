package ai.factory;

import com.iflytek.obu.mark.ai.config.ModelConfig;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.HeaderBuilderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author hxdu5
 * @since 2025/7/9 18:09
 */
@Slf4j
@Component
public class HeaderBuilderFactory {


    private final List<HeaderBuilderStrategy> headerBuilderStrategyList;

    @Resource
    private ModelConfig modelConfig;

    public HeaderBuilderFactory(@Autowired List<HeaderBuilderStrategy> headerBuilderStrategyList) {
        this.headerBuilderStrategyList = headerBuilderStrategyList;
    }

    public Map<String, String> getHeaders(String modelKey) {

        try {
            // 根据modelKey获取ModelInfo
            ModelInfo modelInfo = modelConfig.getModelInfo(modelKey);
            return getHeaders(modelInfo);
        } catch (Exception e) {
            log.error("构建请求头失败，modelKey: {}", modelKey, e);
            throw new RuntimeException("构建请求头失败: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getHeaders(ModelInfo modelInfo) throws NoSuchAlgorithmException {
        try {
            for (HeaderBuilderStrategy headerBuilderStrategy : headerBuilderStrategyList) {
                if (headerBuilderStrategy.supports(modelInfo)) {
                    log.debug("找到支持提供商 {} 的策略: {}", modelInfo.getProvider(), headerBuilderStrategy.getClass().getSimpleName());
                    return headerBuilderStrategy.builderHeaders(modelInfo);
                }
            }
            // 如果没有找到合适的策略，抛出异常
            throw new IllegalArgumentException("未找到支持提供商 '" + modelInfo.getProvider() + "' 的HeaderBuilderStrategy");
        } catch (NoSuchAlgorithmException e) {
            log.error("构建请求头时发生算法异常", e);
            throw new RuntimeException("构建请求头失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("构建请求头失败", e);
            throw new RuntimeException("构建请求头失败: " + e.getMessage(), e);
        }
    }
}
