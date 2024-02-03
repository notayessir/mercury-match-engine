package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumEntrustProp {


    FOK(1),

    IOC(2)
    ;


    EnumEntrustProp(int type) {
        this.type = type;
    }

    private final int type;

    public static EnumEntrustProp getByType(int type){
        EnumEntrustProp[] values = EnumEntrustProp.values();
        for (EnumEntrustProp value : values) {
            if (value.type == type)
                return value;
        }
        return null;
    }


}
