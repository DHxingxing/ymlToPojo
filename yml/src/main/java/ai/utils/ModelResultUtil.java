package ai.utils;

import com.iflytek.obu.mark.dto.ai.ModelResult;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hxdu5
 * @since 2025/7/11 15:48
 */
public class ModelResultUtil {
    public static ModelResult fromFlux(Flux<String> flux, String provider, String modelKey) {
        Map<String, Object> meta = MapUtils.mapOf(
                "flux", flux,
                "provider", provider,
                "modelKey", modelKey
        );
        return ModelResult.successStream(meta);
    }

    public static ModelResult fromString(String content, String provider, String modelKey) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("provider", provider);
        meta.put("modelKey", modelKey);
        return ModelResult.success(content, meta);
    }
}
