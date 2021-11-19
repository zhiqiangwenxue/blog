package com.iszhouhua.blog.model.enums;

/**
 * @author Yzq
 * @date 2021/11/17
 */
public enum DeleteEnum {

    /**
     * 删除
     */
    DELETE(1, "删除"),

    NOTDELETE(0, "未删除");

    private int value;
    private String desc;

    DeleteEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}
