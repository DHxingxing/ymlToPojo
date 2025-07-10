package org.iflytek.ai.config;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模型参数配置类
 * 对应 YAML 中的 params 部分
 * 
 * 工作流程：
 * 1. Spring Boot 配置绑定：YAML → Map<String,Object>
 * 2. ModelParamsConverter：Map<String,Object> → Jackson → ModelParams
 * 3. Jackson 调用 @JsonAnySetter 收集未知字段到 extraParams
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelParams {

    /*--- 公共必填参数 ---*/
    @NotBlank(message = "API密钥不能为空")
    @JsonProperty("api-key")
    private String apiKey;

    @NotBlank(message = "端点地址不能为空")
    private String endpoint;

    /*--- 公共可选参数（带默认值） ---*/
    @Builder.Default
    private Integer timeout = 30000;

    @Builder.Default
    @JsonProperty("max-tokens")
    private Integer maxTokens = 2048;

    @Builder.Default
    private Boolean stream = false;

    @Builder.Default
    private Double temperature = 0.7;

    /*--- 动态参数收集器 ---*/
    @Builder.Default
    private Map<String, Object> extraParams = new LinkedHashMap<>();

    /**
     * Jackson反序列化时调用，收集未知字段
     */
    @JsonAnySetter
    public void putExtra(String key, Object value) {
        if (extraParams == null) {
            extraParams = new LinkedHashMap<>();
        }
        log.debug("收集动态参数: {} = {}", key, value);
        extraParams.put(key, value);
    }

    /**
     * Jackson序列化时调用，展开extraParams到顶层
     */
    @JsonAnyGetter
    public Map<String, Object> getExtraParams() {
        return extraParams != null ? extraParams : Collections.emptyMap();
    }

    /*--- 便捷访问方法 ---*/
    
    /**
     * 获取字符串类型的动态参数
     */
    public String getExtraString(String key) {
        if (extraParams == null) return null;
        Object value = extraParams.get(key);
        return ObjectUtil.isNotNull(value) ? value.toString() : null;
    }

    /**
     * 获取整数类型的动态参数
     */
    public Integer getExtraInt(String key) {
        if (extraParams == null) return null;
        Object value = extraParams.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (ObjectUtil.isNotNull(value)) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                log.warn("无法解析整数参数 {}: {}", key, value);
                return null;
            }
        }
        return null;
    }

    /**
     * 获取Map类型的动态参数
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getExtraMap(String key) {
        if (extraParams == null) return Collections.emptyMap();
        Object value = extraParams.get(key);
        if (value instanceof Map) {
            return (Map<String, String>) value;
        }
        return Collections.emptyMap();
    }
}
