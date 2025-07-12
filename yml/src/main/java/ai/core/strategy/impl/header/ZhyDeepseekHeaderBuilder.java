package ai.core.strategy.impl.header;

import com.iflytek.obu.mark.ai.config.ModelConfig;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.HeaderBuilderStrategy;
import com.iflytek.obu.mark.ai.utils.MapUtils;
import ai.annotation.AiModelStrategy;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hxdu5
 * @since 2025/7/9 17:34
 */
@AiModelStrategy(modelType = "中海油-DS", type = AiModelStrategy.StrategyType.HEADER_BUILDER)
public class ZhyDeepseekHeaderBuilder implements HeaderBuilderStrategy {

    @Resource
    ModelConfig modelConfig;

    private static final String HMAC_ALGORITHM = "hmac-sha256";

    @Override
    public boolean supports(ModelInfo modelInfo) {
        return "中海油-DS".equalsIgnoreCase(modelInfo.getProvider());
    }

    @Override
    public Map<String, String> builderHeaders(ModelInfo modelInfo) {
        String requestUrl = modelInfo.getParams().getExtraString("requestUrl");
        String apiSecret = modelInfo.getParams().getExtraString("apiSecret");
        String apiKey = modelInfo.getParams().getApiKey();

        try {
            URL url = new URL(requestUrl);
            String date = buildGmtDate();
            String signature = computeHmacSignature(url, date, apiSecret);
            String authorization = buildAuthorizationHeader(apiKey, signature);

            return MapUtils.mapOf(
                    "authorization", authorization,
                    "host", url.getHost(),
                    "date", date
            );
        } catch (Exception e) {
            throw new RuntimeException("assemble requestHeader  error:" + e.getMessage());
        }
    }

    private String buildGmtDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(new Date());
    }

    private String computeHmacSignature(URL url, String date, String secret) throws Exception {
        String content = "host: " + url.getHost() + "\n"
                + "date: " + date + "\n"
                + "POST " + url.getPath() + " HTTP/1.1";

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hmacBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    private String buildAuthorizationHeader(String apiKey, String signature) {
        return String.format("hmac api_key=\"%s\", algorithm=\"%s\", headers=\"host date request-line\", signature=\"%s\"",
                apiKey, HMAC_ALGORITHM, signature);
    }
}
