package com.notayessir.ex;

import com.notayessir.constant.EnumExceptionCode;
import lombok.Getter;

import java.io.Serial;

@Getter

public class MatchEngineException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 5435818706800833876L;


    private int code;


    public MatchEngineException() {
        super();
    }


    public MatchEngineException(int code, String message) {
        super(String.valueOf(code));
        this.code = code;
    }

    public MatchEngineException(EnumExceptionCode ex) {
        super(String.valueOf(ex.getCode()));
        this.code = ex.getCode();
    }
}
