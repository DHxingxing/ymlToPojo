package ai.utils;

import java.util.HashMap;
import java.util.Map;
/**
 * @author hxdu5
 * @since 2025/7/11 11:07
 */
public class MapUtils {
    public static <K, V> Map<K, V> mapOf(Object... kvs) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((K) kvs[i], (V) kvs[i + 1]);
        }
        return map;
    }

}
