package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumExceptionCode {



    IDEMPOTENT(10, "request id is duplicated"),


    COMMAND_DISORDER(20, "command disorder"),
    ;

    ;

    EnumExceptionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;

}
