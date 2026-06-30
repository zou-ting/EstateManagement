package com.autogen.propmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartItem {
    private String name;
    private Double value;

    // 如果你需要手动添加构造方法，可以加上这个
    public ChartItem(String name, Number value) {
        this.name = name;
        this.value = value.doubleValue();
    }
}