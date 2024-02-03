package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumCommandType {



    CANCEL(10),

    PLACE(20),
    ;


    private final int code;

    EnumCommandType(int code) {
        this.code = code;
    }

    public static EnumCommandType getByCode(int code){
        EnumCommandType[] values = EnumCommandType.values();
        for (EnumCommandType value : values) {
            if (value.code == code)
                return value;
        }
        return null;
    }
}
