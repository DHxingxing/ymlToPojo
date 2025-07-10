package org.iflytek.ai.core.strategy;

import org.iflytek.ai.config.ModelInfo;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 由于每个大模型的请求头不一样所以要使用策略 + SPI的模式
 * @author hxdu5
 * @since 2025/7/9 17:27
 */
public interface HeaderBuilderStrategy {

    boolean supports(ModelInfo modelInfo);

    Map<String,String> builderHeaders(ModelInfo modelInfo) throws NoSuchAlgorithmException;
}
