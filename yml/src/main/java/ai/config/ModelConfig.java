package ai.config;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * AI模型配置服务
 * 自动绑定 ai-models.* 配置
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "ai-models")
public class ModelConfig extends HashMap<String, ModelInfo> {
    // 直接继承 HashMap，Spring Boot 会自动绑定 ai-models.* 到 Map

    @PostConstruct
    public void init() {
        log.info("加载了 {} 个AI模型配置", this.size());
        this.forEach((key, config) -> log.info("模型 [{}]: {} ({})", key, config.getName(), config.getProvider()));
        System.out.println(this.size());
    }

    public ModelInfo getModelInfo(String modelKey) {
        ModelInfo modelInfo = this.get(modelKey);
        if (ObjectUtil.isNull(modelInfo)) {
            throw new IllegalArgumentException("找不到模型配置: " + modelKey);
        }
        return modelInfo;
    }

    public ModelParams getModelParams(String modelKey) {
        return getModelInfo(modelKey).getParams();
    }
}