package ai.factory;

import com.iflytek.obu.mark.ai.annotation.AiModelStrategy;
import com.iflytek.obu.mark.ai.core.strategy.HeaderBuilderStrategy;
import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import com.iflytek.obu.mark.ai.core.strategy.RequestBodyBuilderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于注解的模型策略工厂
 * 使用Spring注解机制替代SPI机制
 * 
 * @author hxdu5
 * @since 2025/7/12
 */
@Slf4j
@Component
public class AnnotationBasedModelStrategyFactory {

    private final Map<String, ModelResponseParser> responseParserMap = new ConcurrentHashMap<>();
    private final Map<String, HeaderBuilderStrategy> headerBuilderMap = new ConcurrentHashMap<>();
    private final Map<String, RequestBodyBuilderStrategy> requestBodyBuilderMap = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.info("开始加载注解策略类...");
        
        // 加载ResponseParser
        loadStrategies(AiModelStrategy.StrategyType.RESPONSE_PARSER, ModelResponseParser.class, responseParserMap);
        
        // 加载HeaderBuilder
        loadStrategies(AiModelStrategy.StrategyType.HEADER_BUILDER, HeaderBuilderStrategy.class, headerBuilderMap);
        
        // 加载RequestBodyBuilder
        loadStrategies(AiModelStrategy.StrategyType.REQUEST_BODY_BUILDER, RequestBodyBuilderStrategy.class, requestBodyBuilderMap);
        
        log.info("注解策略类加载完成: 响应解析器={}, 头部构建器={}, 请求体构建器={}", 
                responseParserMap.size(), headerBuilderMap.size(), requestBodyBuilderMap.size());
    }

    @SuppressWarnings("unchecked")
    private <T> void loadStrategies(AiModelStrategy.StrategyType type, Class<T> strategyClass, Map<String, T> strategyMap) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(AiModelStrategy.class);
        
        for (Object bean : beans.values()) {
            AiModelStrategy annotation = bean.getClass().getAnnotation(AiModelStrategy.class);
            if (annotation != null && annotation.type() == type) {
                String modelType = annotation.modelType();
                if (strategyClass.isAssignableFrom(bean.getClass())) {
                    strategyMap.put(modelType, (T) bean);
                    log.info("注册{}策略: {} -> {}", type, modelType, bean.getClass().getSimpleName());
                }
            }
        }
    }

    public ModelResponseParser getResponseParser(String modelType) {
        return responseParserMap.get(modelType);
    }

    public HeaderBuilderStrategy getHeaderBuilder(String modelType) {
        return headerBuilderMap.get(modelType);
    }

    public RequestBodyBuilderStrategy getRequestBodyBuilder(String modelType) {
        return requestBodyBuilderMap.get(modelType);
    }

    public boolean hasResponseParser(String modelType) {
        return responseParserMap.containsKey(modelType);
    }

    public boolean hasHeaderBuilder(String modelType) {
        return headerBuilderMap.containsKey(modelType);
    }

    public boolean hasRequestBodyBuilder(String modelType) {
        return requestBodyBuilderMap.containsKey(modelType);
    }
}