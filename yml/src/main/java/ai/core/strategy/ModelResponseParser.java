package ai.core.strategy;

import com.iflytek.obu.mark.ai.config.ModelInfo;

/**
 * @author hxdu5
 * @since 2025/7/11 16:01
 */
public interface ModelResponseParser {

    /**
     * 解析非流式响应文本（一次性 JSON）
     */
    String parseSync(String responseBody, ModelInfo modelInfo);

    /**
     * 解析流式响应行（data: {...}）
     */
    String parseStreamLine(String line, ModelInfo modelInfo);

    /**
     * 解析WebSocket消息
     */
    default String parseWebSocketMessage(String message, ModelInfo modelInfo) {
        // 默认实现：直接返回消息内容
        return message;
    }
}
