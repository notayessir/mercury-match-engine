package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumMatchCommand {



    CANCEL(10),

    PLACE(20),
    ;


    private final int code;

    EnumMatchCommand(int code) {
        this.code = code;
    }

    public static EnumMatchCommand getByCode(int code){
        EnumMatchCommand[] values = EnumMatchCommand.values();
        for (EnumMatchCommand value : values) {
            if (value.code == code)
                return value;
        }
        return null;
    }
}
