package com.notayessir.constant;


import lombok.Getter;

@Getter
public enum EnumMatchResp {


    IDEMPOTENT(-2),


    SUCCESS(1),

            ;


    private final int code;

    EnumMatchResp(int code) {
        this.code = code;
    }
}
