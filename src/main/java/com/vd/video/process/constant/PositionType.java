package com.vd.video.process.constant;

/**
 * 数据权限来源的枚举类型
 */
public enum PositionType {

    /**
     * 1 是关键字后面插入
     */
    AFTER(1),
    /**
     * 2 是关键字前面插入
     */
    BEFORE(2),
    ;
    private Integer type;

    private PositionType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
