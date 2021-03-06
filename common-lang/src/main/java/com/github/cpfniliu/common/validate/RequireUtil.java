package com.github.cpfniliu.common.validate;

import com.github.cpfniliu.common.lang.CheckException;

import java.util.*;

public class RequireUtil {

    private RequireUtil(){}

    /**
     * 为空则抛出异常
     *
     * @param str 需要验证的字符串
     */
    public static void requireStringNonBlank(String str) {
        if (str == null || str.length() == 0) {
            throw new CheckException("字符串为空!");
        }
    }

    /**
     * 为空则抛出异常
     *
     * @param strs 需要验证的字符串数组
     */
    public static void requireStringNonBlank(String... strs) {
        for (String str : strs) {
            requireStringNonBlank(str);
        }
    }

    /**
     * 为空则抛出异常
     */
    public static <T> void requireCollectNonBlank(Collection<T> collection) {
        Objects.requireNonNull(collection);
        if (collection.isEmpty()) {
            throw new CheckException("集合为空!");
        }
    }

    /**
     * 为空则抛出异常
     */
    public static void requireMapNonBlank(Map<?, ?> map) {
        Objects.requireNonNull(map);
        if (map.isEmpty()) {
            throw new CheckException("map为空!");
        }
    }

    /**
     * flag不为true则抛出异常
     *
     * @param flag 待验证的值
     */
    public static void requireBooleanTrue(boolean flag) {
        if (!flag) {
            throw new CheckException("boolean 为false");
        }
    }

    /**
     * flag不为true则抛出异常
     *
     * @param flag 待验证的值
     */
    public static void requireBooleanTrue(boolean flag, String message) {
        if (!flag) {
            throw new CheckException("false: " + message);
        }
    }

    /**
     * arrs 中不包含 target 则抛出异常
     *
     * @param target 目标
     * @param arrs   数组
     */
    public static void requireContainsTargetInArrays(Object target, Object... arrs) {
        if (!Arrays.asList(arrs).contains(target)) {
            throw new CheckException("array 中不包含 target");
        }
    }

    public static String concat(String str1, String str2) {
        Objects.requireNonNull(str1);
        if (str2 != null && !"".equals(str2)) {
            return str1 + str2;
        }
        return str1;
    }

}
