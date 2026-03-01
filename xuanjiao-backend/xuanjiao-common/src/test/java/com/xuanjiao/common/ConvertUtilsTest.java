package com.xuanjiao.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConvertUtils 单元测试
 */
class ConvertUtilsTest {

    /**
     * 测试基本属性复制
     */
    @Test
    void testCopyProperties_basicCopy() {
        SourceBean source = new SourceBean();
        source.setId(1L);
        source.setName("test");
        source.setValue("value");

        TargetBean target = new TargetBean();
        ConvertUtils.copyProperties(source, target);

        assertEquals(1L, target.getId());
        assertEquals("test", target.getName());
        assertEquals("value", target.getValue());
    }

    /**
     * 测试忽略 null 值
     */
    @Test
    void testCopyProperties_ignoreNull() {
        SourceBean source = new SourceBean();
        source.setId(1L);
        source.setName("test");
        source.setValue(null); // null 值

        TargetBean target = new TargetBean();
        target.setValue("originalValue"); // 已有值
        ConvertUtils.copyProperties(source, target);

        assertEquals(1L, target.getId());
        assertEquals("test", target.getName());
        assertEquals("originalValue", target.getValue()); // 保持原值，未被 null 覆盖
    }

    /**
     * 测试包含 null 值（覆盖目标）
     */
    @Test
    void testCopyPropertiesIncludeNull() {
        SourceBean source = new SourceBean();
        source.setId(1L);
        source.setName("test");
        source.setValue(null);

        TargetBean target = new TargetBean();
        target.setValue("originalValue");
        ConvertUtils.copyPropertiesIncludeNull(source, target);

        assertEquals(1L, target.getId());
        assertEquals("test", target.getName());
        assertNull(target.getValue()); // 被 null 覆盖
    }

    /**
     * 测试 source 为 null 时不做处理
     */
    @Test
    void testCopyProperties_sourceNull() {
        TargetBean target = new TargetBean();
        target.setId(1L);
        target.setName("original");

        ConvertUtils.copyProperties(null, target);

        // 目标对象应该保持不变
        assertEquals(1L, target.getId());
        assertEquals("original", target.getName());
    }

    /**
     * 测试复制并创建新对象
     */
    @Test
    void testCopyProperties_withClass() {
        SourceBean source = new SourceBean();
        source.setId(1L);
        source.setName("test");
        source.setValue("value");

        TargetBean target = ConvertUtils.copyProperties(source, TargetBean.class);

        assertNotNull(target);
        assertEquals(1L, target.getId());
        assertEquals("test", target.getName());
        assertEquals("value", target.getValue());
    }

    /**
     * 测试 source 为 null 时 copyProperties 返回 null
     */
    @Test
    void testCopyProperties_class_sourceNull() {
        TargetBean target = ConvertUtils.copyProperties(null, TargetBean.class);
        assertNull(target);
    }

    /**
     * 测试部分字段匹配（source 有更多字段）
     */
    @Test
    void testCopyProperties_partialFields() {
        SourceBean source = new SourceBean();
        source.setId(1L);
        source.setName("test");

        TargetBean target = new TargetBean();
        target.setValue("existingValue");
        ConvertUtils.copyProperties(source, target);

        assertEquals(1L, target.getId());
        assertEquals("test", target.getName());
        assertEquals("existingValue", target.getValue()); // 保持不变
    }

    // -------- 测试用的 Bean 类 --------

    public static class SourceBean {
        private Long id;
        private String name;
        private String value;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class TargetBean {
        private Long id;
        private String name;
        private String value;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
