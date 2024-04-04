package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumMatchStatus {

    /**
     * cancel order
     */
    CANCEL(-20),


    /**
     * order now in order book
     */
    NEW(10),

    /**
     * order fully fill
     */
    FILLED(20),

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
