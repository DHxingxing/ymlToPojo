package org.iflytek.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Spring Boot配置绑定 + Jackson转换器
 * 流程：YAML → Spring Boot Binder → Map<String,Object> → Jackson → ModelParams (支持@JsonAnySetter)
 */
@Slf4j
@Component
@ConfigurationPropertiesBinding
public class ModelParamsConverter implements Converter<Map<String, Object>, ModelParams> {

    // 使用局部ObjectMapper，避免循环依赖
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ModelParams convert(@NotNull Map<String, Object> source) {
        log.debug("开始转换配置参数，源数据: {}", source);
        
        try {
            // 关键：使用Jackson的convertValue，这会触发@JsonAnySetter
            ModelParams result = MAPPER.convertValue(source, ModelParams.class);
            
            log.debug("转换完成，extraParams大小: {}", 
                result.getExtraParams() != null ? result.getExtraParams().size() : 0);
            
            // 输出extraParams内容用于调试
            if (result.getExtraParams() != null && !result.getExtraParams().isEmpty()) {
                log.info("捕获到的动态参数: {}", result.getExtraParams());
            }
            
            return result;
        } catch (Exception e) {
            log.error("参数转换失败，源数据: {}", source, e);
            throw new IllegalArgumentException("无法转换模型参数: " + e.getMessage(), e);
        }
    }
}