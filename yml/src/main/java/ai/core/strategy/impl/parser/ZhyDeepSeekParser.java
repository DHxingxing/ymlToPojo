package ai.core.strategy.impl.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import ai.annotation.AiModelStrategy;

/**
 * 中海油DeepSeek解析器
 * 处理中海油定制的DeepSeek模型响应
 *
 * @author hxdu5
 * @since 2025/7/11 16:03
 */
@AiModelStrategy(modelType = "中海油-DS", type = AiModelStrategy.StrategyType.RESPONSE_PARSER)
public class ZhyDeepSeekParser implements ModelResponseParser {

    @Override
    public String parseSync(String responseBody, ModelInfo modelInfo) {
        try {
            JSONObject response = JSON.parseObject(responseBody);

            // 检查错误码
            if (response.getJSONObject("header").getInteger("code") != 0) {
                throw new RuntimeException("接口返回错误: " + response.getJSONObject("header").getString("message"));
            }

            // 提取 content 数据
            JSONObject payload = response.getJSONObject("payload");
            JSONObject choices = payload.getJSONObject("choices");
            return choices.getJSONArray("text").getJSONObject(0).getString("content");
        } catch (Exception e) {
            throw new RuntimeException("中海油DeepSeek解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String parseStreamLine(String line, ModelInfo modelInfo) {
        try {
            JSONObject response = JSON.parseObject(line);

            // 检查错误码
            if (response.getJSONObject("header").getInteger("code") != 0) {
                return null; // SSE流式中忽略错误行
            }

            // 提取 content 数据
            JSONObject payload = response.getJSONObject("payload");
            JSONObject choices = payload.getJSONObject("choices");
            return choices.getJSONArray("text").getJSONObject(0).getString("content");
        } catch (Exception e) {
            return null; // 解析失败返回null
        }
    }

    @Override
    public String parseWebSocketMessage(String message, ModelInfo modelInfo) {
        return parseSync(message, modelInfo); // WebSocket和HTTP格式相同
    }
}