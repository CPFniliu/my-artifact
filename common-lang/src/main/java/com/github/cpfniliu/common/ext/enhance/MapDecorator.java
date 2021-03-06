package com.github.cpfniliu.common.ext.enhance;

import com.github.cpfniliu.common.base.IGetter;
import com.github.cpfniliu.common.util.common.Maps;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

/**
 * <b>Description : </b>
 *
 * @author CPF
 * Date: 2020/7/28 16:54
 */
public class MapDecorator<K, V> implements IGetter<K> {

    @Getter
    private Map<K, V> map;

    public MapDecorator(@NonNull Map<K, V> map) {
        this.map = map;
    }

    public MapDecorator<K, V> put(K key, V val) {
        map.put(key, val);
        return this;
    }

    @Override
    public Object get(K key) {
        return map.get(key);
    }

    public MapDecorator<K, V> changeKey(K oldKey, K newKey) {
        Maps.changeKey(map, oldKey, newKey);
        return this;
    }

}
