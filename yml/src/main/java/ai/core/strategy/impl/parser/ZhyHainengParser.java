package ai.core.strategy.impl.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import ai.annotation.AiModelStrategy;

/**
 * 中海油海能大模型响应解析器
 * 专门处理中海油海能大模型的HTTP、SSE和WebSocket响应格式
 * 
 * @author hxdu5
 * @since 2025/7/12 21:17
 */
@AiModelStrategy(modelType = "中海油", type = AiModelStrategy.StrategyType.RESPONSE_PARSER)
public class ZhyHainengParser implements ModelResponseParser {

    @Override
    public String parseSync(String responseBody, ModelInfo modelInfo) {
        try {
            JSONObject response = JSON.parseObject(responseBody);
            
            // 检查错误码
            if (response.getJSONObject("header") != null 
                && response.getJSONObject("header").getInteger("code") != 0) {
                throw new RuntimeException("接口返回错误: " 
                    + response.getJSONObject("header").getString("message"));
            }

            // 提取 content 数据
            JSONObject payload = response.getJSONObject("payload");
            JSONObject choices = payload.getJSONObject("choices");
            return choices.getJSONArray("text").getJSONObject(0).getString("content");
        } catch (Exception e) {
            throw new RuntimeException("中海油海能大模型解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String parseStreamLine(String line, ModelInfo modelInfo) {
        try {
            JSONObject response = JSON.parseObject(line);
            
            // 检查错误码
            if (response.getJSONObject("header") != null 
                && response.getJSONObject("header").getInteger("code") != 0) {
                throw new RuntimeException("接口返回错误: " 
                    + response.getJSONObject("header").getString("message"));
            }

            // 提取 content 数据
            JSONObject payload = response.getJSONObject("payload");
            JSONObject choices = payload.getJSONObject("choices");
            return choices.getJSONArray("text").getJSONObject(0).getString("content");
        } catch (Exception e) {
            return null; // 解析失败返回null，让调用器处理
        }
    }

    @Override
    public String parseWebSocketMessage(String message, ModelInfo modelInfo) {
        try {
            JSONObject response = JSON.parseObject(message);
            
            // 检查错误码
            if (response.getJSONObject("header") != null 
                && response.getJSONObject("header").getInteger("code") != 0) {
                throw new RuntimeException("接口返回错误: " 
                    + response.getJSONObject("header").getString("message"));
            }

            // 提取 content 数据
            JSONObject payload = response.getJSONObject("payload");
            JSONObject choices = payload.getJSONObject("choices");
            JSONArray textArray = choices.getJSONArray("text");
            
            if (textArray == null || textArray.isEmpty()) {
                return null;
            }
            
            String content = textArray.getJSONObject(0).getString("content");
            
            // 检查状态码，2表示结束
            int status = choices.getInteger("status");
            if (status == 2) {
                // 最后一条消息，返回完整内容
                return content;
            } else {
                // 中间消息，返回当前片段
                return content;
            }
        } catch (Exception e) {
            return null; // 解析失败返回null
        }
    }
}