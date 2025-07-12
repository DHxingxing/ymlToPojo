// DeepSeekHeaderBuilder.java
package ai.core.strategy.impl.header;

import cn.hutool.json.JSONObject;
import com.iflytek.obu.mark.ai.config.ModelInfo;
import com.iflytek.obu.mark.ai.core.strategy.HeaderBuilderStrategy;
import com.iflytek.obu.mark.ai.utils.MapUtils;
import ai.annotation.AiModelStrategy;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AiModelStrategy(modelType = "DeepSeek", type = AiModelStrategy.StrategyType.HEADER_BUILDER)
public class DeepSeekHeaderBuilder implements HeaderBuilderStrategy {

    @Override
    public boolean supports(ModelInfo modelInfo) {
        return "DeepSeek".equalsIgnoreCase(modelInfo.getProvider());
    }

    @Override
    public Map<String, String> builderHeaders(ModelInfo modelInfo) throws NoSuchAlgorithmException {

        String appKey = modelInfo.getParams().getExtraString("appKey");
        String appid = modelInfo.getParams().getExtraString("appid");
        String appName = modelInfo.getParams().getEndpoint().split("/")[3];
        String uuid = UUID.randomUUID().toString();

        while (appName.length() < 24) {
            appName += "0";
        }
        String capabilityname = appName;

        String csid = appid + capabilityname + uuid;
        Map<String, String> tmp_xServerParam = new HashMap<>();

        tmp_xServerParam.put("appid", appid);
        tmp_xServerParam.put("csid", csid);

        String xCurTime = String.valueOf(System.currentTimeMillis() / 1000);
        String xServerParam = Base64.getEncoder().encodeToString(new JSONObject(tmp_xServerParam).toString().getBytes());
        String xCheckSum = generateMD5(appKey + xCurTime + xServerParam);


        return MapUtils.mapOf(
                "appKey", appKey,
                "X-Server-Param", xServerParam,
                "X-CurTime", xCurTime,
                "X-CheckSum", xCheckSum,
                "content-type", "application/json"
        );
    }

    private String generateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());
        byte[] digest = md.digest();
        BigInteger no = new BigInteger(1, digest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}
