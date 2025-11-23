package cn.loblok.common.Enum;

public enum PayrollStatus {
    PENDING,
    SUBMITTED_TO_MQ, //投递成功
    SUCCESS,
    MQ_SEND_FAILED, //投递失败
    FAILED;


    public boolean canTransitionTo(PayrollStatus target) {
        return switch (this) {
            case PENDING    -> target == SUBMITTED_TO_MQ || target == MQ_SEND_FAILED;
            case MQ_SEND_FAILED -> false;
            case SUBMITTED_TO_MQ  -> target == SUCCESS || target == FAILED;
            case SUCCESS, FAILED -> false;
        };
    }
}