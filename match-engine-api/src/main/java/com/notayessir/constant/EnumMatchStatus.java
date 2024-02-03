package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumMatchStatus {


    OPEN(10),

//    PARTIAL_FILLED(20),

//    PARTIAL_FILLED_CLOSE(30),

//    PARTIAL_FILLED_CANCEL(40),

    FILLED(50),

    CLOSE(60),

    ;


    private final int status;

    EnumMatchStatus(int status) {
        this.status = status;
    }


    public static EnumMatchStatus getByStatus(int status){
        EnumMatchStatus[] values = EnumMatchStatus.values();
        for (EnumMatchStatus value : values) {
            if (value.status == status)
                return value;
        }
        return null;
    }
}
