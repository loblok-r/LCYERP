package cn.loblok.common.Enum;

public enum PayrollStatus {

    // 准备阶段
    DRAFT,                  // 草稿
    UNDER_REVIEW,                   // 审核中（HR提交）
    REVIEW_REJECTED,                // 审核驳回（可重新提交）

    PENDING_CALCULATION,    // 审核通过,财务点击【开始计算】→ 系统将状态设为 PENDING_CALCULATION，等待计算
    CALCULATING,            // 计算中
    CALCULATION_FAILED,     // 计算失败

    // 执行阶段
    SCHEDULED,        // 计算完成，待发薪日
    PENDING, //准备投递发薪请求给发薪模块
    SUBMITTED_TO_MQ, //投递成功
    MQ_SEND_FAILED, //投递失败


    // 处理阶段
    PROCESSING,//处理中
    SUCCESS,//最终发薪成功
    FAILED, //最终发薪失败
    CANCELLED;  // 已取消



    public boolean canTransitionTo(PayrollStatus target) {
        return switch (this) {
            case DRAFT -> target == UNDER_REVIEW;
            case UNDER_REVIEW -> target == REVIEW_REJECTED || target == PENDING_CALCULATION;
            case REVIEW_REJECTED -> target == UNDER_REVIEW;
            case PENDING_CALCULATION -> target == CALCULATING;
            case CALCULATING ->
                    target == SCHEDULED || target == CALCULATION_FAILED;
            case CALCULATION_FAILED -> target == CALCULATING; // 允许重试
            case SCHEDULED -> target == PENDING || target == CANCELLED;
            case PENDING    -> target == SUBMITTED_TO_MQ || target == MQ_SEND_FAILED;
            case MQ_SEND_FAILED -> target == SUBMITTED_TO_MQ;
            case SUBMITTED_TO_MQ  -> target == PROCESSING;
            case PROCESSING  -> target == SUCCESS || target == FAILED;
            default -> false;
        };
    }

    // 定义是否为终态
    public boolean isFinal() {
        return this == SUCCESS ||
                this == FAILED ||
                this == MQ_SEND_FAILED ||
                this == CANCELLED;
    }
}