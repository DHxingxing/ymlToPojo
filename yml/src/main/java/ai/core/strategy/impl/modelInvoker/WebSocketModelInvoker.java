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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket模型调用器 - 支持流式和非流式WebSocket调用
 * 
 * @author hxdu5
 * @since 2025/7/12
 */
@Slf4j
@Component
public class WebSocketModelInvoker implements ModelInvoker {

    @Resource
    private ModelConfig modelConfig;
    @Resource
    private AnnotationBasedModelStrategyFactory strategyFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(ModelInfo modelInfo) {
        return ModelCallTypeEnum.WEBSOCKET_TYPE.equals(modelInfo.getParams().getCallTypeEnum());
    }

    @Override
    public String invoke(AiMessageDTO aiMessageDTO) {
        ModelInfo modelInfo = null;
        WebSocketClient webSocketClient = null;
        
        try {
            // 获取模型配置
            modelInfo = modelConfig.getModelInfo(aiMessageDTO.getModelKey());
            
            log.info("开始WebSocket调用模型: {} (提供商: {})", modelInfo.getName(), modelInfo.getProvider());
            log.debug("调用参数: modelKey={}, prompt={}", aiMessageDTO.getModelKey(), aiMessageDTO.getPrompt());

            // 使用工厂模式构建请求头和请求体
            Map<String, String> headers = strategyFactory.getHeaderBuilder(modelInfo.getProvider())
                    .builderHeaders(modelInfo);
            Map<String, Object> requestBody = strategyFactory.getRequestBodyBuilder(modelInfo.getProvider())
                    .builderRequestBody(modelInfo, aiMessageDTO);

            // 获取WebSocket连接URL
            String wsUrl = buildWebSocketUrl(modelInfo, headers);
            log.debug("WebSocket连接URL: {}", wsUrl);

            // 创建响应处理器
            WebSocketResponseHandler responseHandler = new WebSocketResponseHandler(modelInfo, strategyFactory);
            
            // 创建WebSocket客户端
            webSocketClient = createWebSocketClient(wsUrl, responseHandler);
            
            // 建立连接
            boolean connected = webSocketClient.connectBlocking();
            if (!connected) {
                throw new RuntimeException("WebSocket连接失败");
            }
            log.info("WebSocket连接已建立");

            // 发送请求
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("发送WebSocket请求: {}", requestJson);
            webSocketClient.send(requestJson);

            // 等待响应完成
            String result = responseHandler.waitForResponse(modelInfo.getParams().getTimeout());
            
            log.info("WebSocket调用完成，提供商: {}", modelInfo.getProvider());
            return result;

        } catch (Exception e) {
            log.error("WebSocket调用失败: {}", e.getMessage(), e);
            String errorMsg = String.format("WebSocket调用失败: %s", e.getMessage());
            if (modelInfo != null) {
                errorMsg = String.format("调用模型[%s]失败: %s", modelInfo.getName(), e.getMessage());
            }
            throw new RuntimeException(errorMsg, e);
            
        } finally {
            if (webSocketClient != null) {
                try {
                    webSocketClient.close();
                    log.debug("WebSocket连接已关闭");
                } catch (Exception e) {
                    log.warn("关闭WebSocket连接时发生异常: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 构建WebSocket连接URL
     */
    private String buildWebSocketUrl(ModelInfo modelInfo, Map<String, String> headers) throws URISyntaxException {
        String endpoint = modelInfo.getParams().getEndpoint();
        
        // 处理URL中的认证参数
        if (headers != null && !headers.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(endpoint);
            boolean firstParam = !endpoint.contains("?");
            
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (isQueryParam(entry.getKey())) {
                    urlBuilder.append(firstParam ? "?" : "&")
                             .append(entry.getKey()).append("=").append(entry.getValue());
                    firstParam = false;
                }
            }
            return urlBuilder.toString();
        }
        
        return endpoint;
    }

    /**
     * 判断header是否应该作为URL参数传递
     */
    private boolean isQueryParam(String key) {
        return key.equalsIgnoreCase("api-key") || 
               key.equalsIgnoreCase("apikey") || 
               key.equalsIgnoreCase("token") ||
               key.equalsIgnoreCase("authorization");
    }

    /**
     * 创建WebSocket客户端
     */
    private WebSocketClient createWebSocketClient(String wsUrl, WebSocketResponseHandler handler) throws URISyntaxException {
        return new WebSocketClient(new URI(wsUrl), new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("WebSocket连接已建立: {}", handshakedata.getHttpStatus());
            }

            @Override
            public void onMessage(String message) {
                try {
                    log.debug("收到WebSocket消息: {}", message);
                    handler.handleMessage(message);
                } catch (Exception e) {
                    log.error("处理WebSocket消息异常: {}", e.getMessage(), e);
                    handler.onError(e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("WebSocket连接关闭: code={}, reason={}, remote={}", code, reason, remote);
                handler.onClose();
            }

            @Override
            public void onError(Exception ex) {
                log.error("WebSocket错误: {}", ex.getMessage(), ex);
                handler.onError(ex);
            }
        };
    }

    /**
     * WebSocket响应处理器
     */
    private static class WebSocketResponseHandler {
        private final ModelInfo modelInfo;
        private final ModelResponseParser parser;
        private final StringBuilder responseBuilder = new StringBuilder();
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile String result;
        private volatile Exception error;

        public WebSocketResponseHandler(ModelInfo modelInfo, AnnotationBasedModelStrategyFactory strategyFactory) {
            this.modelInfo = modelInfo;
            this.parser = strategyFactory.getResponseParser(modelInfo.getProvider());
        }

        public void handleMessage(String message) {
            try {
                // 使用提供商特定的解析器处理消息
                String content = parser.parseWebSocketMessage(message, modelInfo);
                
                if (content != null) {
                    responseBuilder.append(content);
                }

                // 检查是否完成（根据提供商的结束标志）
                if (isResponseComplete(message)) {
                    result = responseBuilder.toString();
                    latch.countDown();
                }
                
            } catch (Exception e) {
                error = e;
                latch.countDown();
            }
        }

        public String waitForResponse(long timeoutMillis) throws Exception {
            boolean completed = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!completed) {
                throw new RuntimeException("WebSocket响应超时");
            }
            
            if (error != null) {
                throw error;
            }
            
            return result != null ? result : responseBuilder.toString();
        }

        public void onClose() {
            if (latch.getCount() > 0) {
                latch.countDown();
            }
        }

        public void onError(Exception ex) {
            error = ex;
            latch.countDown();
        }

        /**
         * 根据提供商判断响应是否完成
         */
        private boolean isResponseComplete(String message) {
            String provider = modelInfo.getProvider();
            
            switch (provider) {
                case "中海油":
                case "zhy-haineng":
                    return message.contains("\"status\":2") || message.contains("\"status\": 2");
                case "DeepSeek":
                    return message.contains("\"finish_reason\":") && !message.contains("\"finish_reason\":null");
                default:
                    return message.contains("\"done\":true") || message.contains("\"finish\":true");
            }
        }
    }
}