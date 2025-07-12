package ai.factory;

import com.iflytek.obu.mark.ai.core.strategy.ModelResponseParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hxdu5
 * @since 2025/7/11 16:04
 */
@Component
public class ModelResponseParserFactory {

    private final Map<String, ModelResponseParser> parserMap;

    @Autowired
    public ModelResponseParserFactory(List<ModelResponseParser> parsers) {
        this.parserMap = new HashMap<>();
        for (ModelResponseParser parser : parsers) {
            String name = parser.getClass().getAnnotation(Component.class).value();
            parserMap.put(name, parser);
        }
    }

    public ModelResponseParser getParser(String provider) {
        return null;
    }
}
