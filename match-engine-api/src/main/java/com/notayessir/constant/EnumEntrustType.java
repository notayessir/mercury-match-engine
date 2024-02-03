package com.notayessir.constant;

import lombok.Getter;

@Getter
public enum EnumEntrustType {

    MARKET(1),

    NORMAL_LIMIT(2),

    PREMIUM_LIMIT(3)

    ;


    private final int type;


    EnumEntrustType(int type) {
        this.type = type;
    }

    public static EnumEntrustType getByType(int type){
        EnumEntrustType[] values = EnumEntrustType.values();
        for (EnumEntrustType value : values) {
            if (value.type == type)
                return value;
        }
        return null;
    }

}
