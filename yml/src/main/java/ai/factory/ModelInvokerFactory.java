package ai.factory;

import com.iflytek.obu.mark.ai.config.ModelConfig;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.ModelInvoker;
import com.iflytek.obu.mark.enums.ai.ModelCallTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hxdu5
 * @since 2025/7/11 16:41
 */
@Slf4j
public class ModelInvokerFactory {

    @Resource
    ModelConfig modelConfig;

    private final List<ModelInvoker> modelInvokers;


    public ModelInvokerFactory(@Autowired List<ModelInvoker> modelInvokers) {
        this.modelInvokers = modelInvokers;
        log.info("加载了 {} 个ModelInvoker实现", modelInvokers.size());
        // 打印所有可用的调用器
        modelInvokers.forEach(invoker ->
                log.info("注册调用器: {}", invoker.getClass().getSimpleName()));
    }

    public ModelInvoker getInvoker(String modelKey){
        ModelInfo modelInfo = modelConfig.get(modelKey);
        ModelCallTypeEnum callType = modelInfo.getParams().getCallTypeEnum();

        for (ModelInvoker invoker : modelInvokers) {
            if (invoker.supports(modelInfo)) {
                log.info("为模型 [{}] 选择调用器: {} (调用方式: {})",
                        modelKey, invoker.getClass().getSimpleName(), callType.getDesc());
                return invoker;
            }
        }

        throw new IllegalArgumentException(String.format(
                "未找到支持调用方式 '%s' 的ModelInvoker，modelKey: %s",
                callType.getDesc(), modelKey));
    }
}
