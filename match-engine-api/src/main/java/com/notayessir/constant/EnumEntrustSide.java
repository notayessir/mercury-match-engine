package com.notayessir.constant;

import lombok.Getter;

@Getter
public enum EnumEntrustSide {

    BUY(1),

    SELL(0)
    ;

    private final int code;

    EnumEntrustSide(int code) {
        this.code = code;
    }

    public static EnumEntrustSide getEntrustSide(int side){
        EnumEntrustSide[] values = EnumEntrustSide.values();
        for (EnumEntrustSide value : values) {
            if (side == value.code){
                return value;
            }
        }
        return null;
    }

}
