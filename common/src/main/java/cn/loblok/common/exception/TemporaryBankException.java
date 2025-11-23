package cn.loblok.common.exception;

public class TemporaryBankException extends RuntimeException {
    public TemporaryBankException(String message) {
        super(message);
    }

    public TemporaryBankException(String message, Throwable cause) {
        super(message, cause);
    }
}