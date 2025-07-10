package org.iflytek.ai.core.impl.header;

import org.iflytek.ai.config.ModelConfig;
import org.iflytek.ai.config.ModelInfo;
import org.iflytek.ai.core.strategy.HeaderBuilderStrategy;
import org.springframework.stereotype.Component;

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
@Component
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

            Map<String, String> header = new HashMap<>();
            header.put("authorization", authorization);
            header.put("host", url.getHost());
            header.put("date", date);
            return header;
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
