package com.github.cpfniliu.common.util.common;

import com.github.cpfniliu.common.outer.guava.Maps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <b>Description : </b>
 *
 * @author CPF
 * Date: 2020/6/15 15:48
 */
@Slf4j
public class BeanListUtils {

    private BeanListUtils() {}

    /**
     * 将 list 按照指定规则转为 Map
     *
     * @param getHKey 从对象中寻求 hash key 的方法
     * @param list 待转换的list
     * @param <K> hash key
     * @param <V> hash value
     * @return 转换后的map
     */
    public static <K, V> Map<K, V> castListToMap(@NonNull List<V> list, @NonNull Function<V, K> getHKey) {
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        Map<K, V> map = Maps.newHashMapWithExpectedSize(list.size());
        list.forEach(it -> {
            if (it == null) {
                return;
            }
            K key = getHKey.apply(it);
            if (key != null) {
                map.put(key, it);
            }
        });
        return map;
    }

}
