package com.yiyan.boot.elasticsearch.core.model;

import com.yiyan.boot.elasticsearch.core.enums.RangeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sparkler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsRangeParam {
    /**
     * Range 标识符
     */
    private RangeEnum range;
    /**
     * 范围值
     */
    private Object value;
}