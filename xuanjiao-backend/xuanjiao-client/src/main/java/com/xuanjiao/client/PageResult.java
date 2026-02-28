package com.xuanjiao.client;

import lombok.Data;
import java.util.List;

/**
 * 分页结果数据传输对象
 *
 * <p>封装分页查询的结果数据，包含数据列表和分页元信息。
 * 支持泛型，可包装任意类型的数据列表。</p>
 *
 * @param <T> 数据元素的类型
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class PageResult<T> {

    /**
     * 当前页的数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 创建分页结果对象的静态工厂方法
     *
     * @param list     当前页的数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @param <T>      数据元素的类型
     * @return 构造完成的分页结果对象
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
}
