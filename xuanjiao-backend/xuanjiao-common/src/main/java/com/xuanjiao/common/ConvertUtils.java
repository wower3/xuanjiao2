package com.xuanjiao.common;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * 对象转换工具类
 */
public class ConvertUtils {

    /**
     * 复制对象属性（忽略 null 值）
     * 注意：source 为 null 时不做任何处理
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 复制对象属性（包含 null 值，覆盖目标对象的所有属性）
     * 注意：source 为 null 时不做任何处理
     */
    public static void copyPropertiesIncludeNull(Object source, Object target) {
        if (source == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

    /**
     * 复制对象属性并返回目标对象（忽略 null 值）
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败: " + targetClass.getName(), e);
        }
    }

    /**
     * 获取对象中 null 值的属性名
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        return emptyNames.toArray(new String[0]);
    }
}
