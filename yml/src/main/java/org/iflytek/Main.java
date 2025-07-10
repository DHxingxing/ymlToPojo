package org.iflytek;

import org.iflytek.ai.config.ModelConfig;
import org.iflytek.ai.config.ModelInfo;
import org.iflytek.ai.config.ModelParams;
import org.iflytek.ai.factory.HeaderBuilderFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author hxdu5
 * @since 2025/7/10 20:05
 */


@SpringBootApplication//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {

    @Resource
    private ModelConfig modelConfig;
    @Resource
    private HeaderBuilderFactory headerBuilderFactory;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public void testBuilder() throws NoSuchAlgorithmException {
        ModelInfo modelInfo = modelConfig.getModelInfo("deepseek-r1-v1");
        
        // 测试我们的转换机制
        ModelParams params = modelInfo.getParams();
        
        System.out.println("=== 测试动态参数捕获 ===");
        System.out.println("extraParams 大小: " + params.getExtraParams().size());
        System.out.println("extraParams 内容: " + params.getExtraParams());
        
        // 测试具体的字段
        System.out.println("appid: " + params.getExtraString("appid"));
        System.out.println("appKey: " + params.getExtraString("appKey"));
        System.out.println("model-name: " + params.getExtraString("model-name"));
        System.out.println("requestUrl: " + params.getExtraString("requestUrl"));
        System.out.println("apiSecret: " + params.getExtraString("apiSecret"));
        
        Map<String, String> headers = headerBuilderFactory.getHeaders(modelInfo);
        System.out.println("生成的请求头: " + headers);
    }
}