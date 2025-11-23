package cn.loblok.common.dto;

import cn.loblok.common.Enum.PayrollStatus;
import lombok.Data;

@Data
public class PayrollCallbackRequest {

    private String bizId;
    private PayrollStatus status;

}