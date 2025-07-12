package ai.core.strategy.impl.modelInvoker;

import com.iflytek.obu.mark.ai.config.ModelConfig;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.ModelInvoker;
import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import ai.factory.AnnotationBasedModelStrategyFactory;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;
import com.iflytek.obu.mark.enums.ai.ModelCallTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

/**
 * @author hxdu5
 * @since 2025/7/11 16:40
 */
@Slf4j
@Component
public class SseModelInvoker implements ModelInvoker {

    @Resource
    private ModelConfig modelConfig;
    @Resource
    private AnnotationBasedModelStrategyFactory strategyFactory;

    @Override
    public boolean supports(ModelInfo modelInfo) {
        return ModelCallTypeEnum.SSE_TYPE.equals(modelInfo.getParams().getCallTypeEnum());
    }

    @Override
    public String invoke(AiMessageDTO aiMessageDTO) {
        throw new UnsupportedOperationException("SSE调用器不支持同步调用，请使用invokeStream方法");
    }

    @Override
    public Flux<String> invokeStream(AiMessageDTO aiMessageDTO) throws NoSuchAlgorithmException {
        // 1. 根据modelKey获取模型配置信息
        ModelInfo modelInfo = modelConfig.getModelInfo(aiMessageDTO.getModelKey());

        // 2. 使用工厂模式构建请求头和请求体
        Map<String, String> headers = strategyFactory.getHeaderBuilder(modelInfo.getProvider())
                .builderHeaders(modelInfo);
        Map<String, Object> requestBody = strategyFactory.getRequestBodyBuilder(modelInfo.getProvider())
                .builderRequestBody(modelInfo, aiMessageDTO);

        log.info("开始SSE流式调用模型: {} (提供商: {})", modelInfo.getName(), modelInfo.getProvider());
        log.debug("请求端点: {}", modelInfo.getParams().getEndpoint());

        // 3. 创建WebClient实例用于异步HTTP调用
        WebClient webClient = WebClient.create();

        // 4. 构建SSE流式请求并返回Flux
        return webClient.post()
                .uri(modelInfo.getParams().getEndpoint())  // 设置请求URL
                .headers(httpHeaders -> {
                    // 添加所有自定义请求头
                    headers.forEach(httpHeaders::set);
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(requestBody)  // 设置请求体
                .retrieve()  // 执行请求
                .bodyToFlux(String.class)  // 将响应转换为字符串流
                .map(line -> {
                    // 5. 使用提供商特定的解析器处理SSE数据
                    ModelResponseParser parser = strategyFactory.getResponseParser(modelInfo.getProvider());
                    return parser.parseStreamLine(line, modelInfo);
                })
                .filter(Objects::nonNull)  // 过滤掉null值
                .doOnNext(line -> log.debug("接收到SSE数据: {}", line))  // 记录接收到的数据
                .doOnError(e -> log.error("SSE调用过程中发生异常: {}", e.getMessage(), e))  // 记录异常
                .doOnComplete(() -> log.info("SSE流式调用完成"))  // 记录完成状态
                .doOnCancel(() -> log.warn("SSE流式调用被取消"));  // 记录取消状态
    }
}
