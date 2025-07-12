package ai.core.strategy.impl.modelInvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.obu.mark.ai.config.ModelConfig;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.ModelInvoker;
import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import ai.factory.AnnotationBasedModelStrategyFactory;
import com.iflytek.obu.mark.dto.ai.AiMessageDTO;
import com.iflytek.obu.mark.enums.ai.ModelCallTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * HTTP模型调用器 - 重构后的简洁版本
 *
 * @author hxdu5
 * @since 2025/7/9 13:59
 */
@Slf4j
@Component
public class HttpModelInvoker implements ModelInvoker {

    @Resource
    ModelConfig modelConfig;
    @Resource
    AnnotationBasedModelStrategyFactory strategyFactory;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HttpModelInvoker() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // 配置RestTemplate的超时时间
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(30000);  // 连接超时30秒
            setReadTimeout(120000);    // 读取超时120秒
        }});
    }

    @Override
    public boolean supports(ModelInfo modelInfo) {
        return ModelCallTypeEnum.HTTP_TYPE.equals(modelInfo.getParams().getCallTypeEnum());
    }

    @Override
    public String invoke(AiMessageDTO aiMessageDTO) {
        ModelInfo modelInfo = null;
        try {
            // 获取模型配置
            modelInfo = modelConfig.getModelInfo(aiMessageDTO.getModelKey());

            log.info("开始HTTP调用模型: {} ({})", modelInfo.getName(), modelInfo.getProvider());
            log.debug("调用参数: modelKey={}, prompt={}", aiMessageDTO.getModelKey(), aiMessageDTO.getPrompt());

            // 构建请求头和请求体
            Map<String, String> headers = strategyFactory.getHeaderBuilder(modelInfo.getProvider())
                    .builderHeaders(modelInfo);
            Map<String, Object> requestBody = strategyFactory.getRequestBodyBuilder(modelInfo.getProvider())
                    .builderRequestBody(modelInfo, aiMessageDTO);

            log.debug("请求头: {}", headers);
            log.debug("请求体: {}", requestBody);

            // 设置HTTP请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            // 添加自定义请求头
            headers.forEach(httpHeaders::set);

            // 创建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

            // 发送HTTP POST请求
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    modelInfo.getParams().getEndpoint(),
                    requestEntity,
                    String.class
            );
            long endTime = System.currentTimeMillis();

            log.info("HTTP请求完成，耗时: {}ms, 状态码: {}", (endTime - startTime), response.getStatusCode());
            log.debug("响应体: {}", response.getBody());

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("HTTP请求失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                throw new HttpServerErrorException(response.getStatusCode());
            }

            // 解析响应
            ModelResponseParser parser = strategyFactory.getResponseParser(modelInfo.getProvider());
            return parser.parseSync(response.getBody(), modelInfo);

        } catch (HttpClientErrorException e) {
            // 4xx客户端错误
            log.error("HTTP客户端错误 [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException(String.format("客户端错误: %s", e.getResponseBodyAsString()), e);

        } catch (HttpServerErrorException e) {
            // 5xx服务器错误
            log.error("HTTP服务器错误 [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException(String.format("服务器错误: %s", e.getResponseBodyAsString()), e);

        } catch (ResourceAccessException e) {
            // 网络连接异常、超时等
            log.error("网络连接异常: {}", e.getMessage(), e);
            throw new RuntimeException("网络连接异常: " + e.getMessage(), e);

        } catch (Exception e) {
            // 其他异常
            log.error("HTTP调用模型失败: {}", e.getMessage(), e);
            String errorMsg = String.format("HTTP调用失败: %s", e.getMessage());
            if (modelInfo != null) {
                errorMsg = String.format("调用模型[%s]失败: %s", modelInfo.getName(), e.getMessage());
            }
            throw new RuntimeException(errorMsg, e);
        }
    }
}