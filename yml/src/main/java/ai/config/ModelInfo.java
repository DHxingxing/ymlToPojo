package ai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * AI模型完整配置类
 * 对应 YAML 中的顶层模型配置结构
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelInfo {

    // 模型元信息
    @NotBlank(message = "模型名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "提供商不能为空")
    private String provider;

    // 模型参数（核心配置）
    // Spring Boot会自动绑定YAML到这个Map
    @NotNull(message = "模型参数不能为空")
    @JsonIgnore
    private Map<String, Object> params;

    // 缓存转换后的ModelParams对象
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @NotBlank(message = "RequestBody中的模型名不能为空")
    private transient ModelParams modelParams;

    /**
     * 获取转换后的ModelParams对象
     * 这个方法会使用我们的转换器将Map转换为ModelParams
     */
    public ModelParams getParams() {
        if (modelParams == null && params != null) {
            log.debug("开始转换模型参数，原始Map: {}", params);
            // 使用转换器进行转换
            ModelParamsConverter converter = new ModelParamsConverter();
            modelParams = converter.convert(params);
            if (modelParams != null) {
                log.debug("参数转换完成，extraParams大小: {}",
                        modelParams.getExtraParams().size());
            }
        }
        return modelParams;
    }

    /**
     * 设置原始的Map参数
     */
    @JsonIgnore
    public void setParams(Map<String, Object> params) {
        this.params = params;
        this.modelParams = null; // 清除缓存，强制重新转换
        log.debug("设置新的参数Map，大小: {}", params != null ? params.size() : 0);
    }
}
