package ai.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标记AI模型策略类的注解
 * 用于替代SPI机制，通过Spring Component注册
 * 
 * @author hxdu5
 * @since 2025/7/12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AiModelStrategy {
    /**
     * 支持的模型提供商名称
     */
    String modelType();
    
    /**
     * 策略类型
     */
    StrategyType type();
    
    /**
     * 策略类型枚举
     */
    enum StrategyType {
        RESPONSE_PARSER,
        HEADER_BUILDER,
        REQUEST_BODY_BUILDER
    }
}