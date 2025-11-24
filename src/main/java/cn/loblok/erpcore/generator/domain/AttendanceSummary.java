package cn/loblok/erpcore/generator.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

/**
* 月度考勤汇总
* @TableName attendance_summary
*/
public class AttendanceSummary implements Serializable {

    /**
    * 
    */
    (message="[]不能为空")
    @ApiModelProperty("")
    private Long id;
    /**
    * 
    */
    (message="[]不能为空")
    @ApiModelProperty("")
    private Long companyId;
    /**
    * 
    */
    (message="[]不能为空")
    @ApiModelProperty("")
    private Long employeeId;
    /**
    * 薪资月份
    */
    @NotBlank(message="[薪资月份]不能为空")
    @Size(max= 7,message="编码长度不能超过7")
    @ApiModelProperty("薪资月份")
    @Length(max= 7,message="编码长度不能超过7")
    private String payrollMonth;
    /**
    * 应出勤天数
    */
    (message="[应出勤天数]不能为空")
    @ApiModelProperty("应出勤天数")
    private Integer workingDays;
    /**
    * 实际出勤天数
    */
    (message="[实际出勤天数]不能为空")
    @ApiModelProperty("实际出勤天数")
    private Integer actualDays;
    /**
    * 加班小时数
    */
    (message="[加班小时数]不能为空")
    @ApiModelProperty("加班小时数")
    private BigDecimal overtimeHours;
    /**
    * 请假天数（含事假病假）
    */
    (message="[请假天数（含事假病假）]不能为空")
    @ApiModelProperty("请假天数（含事假病假）")
    private BigDecimal leaveDays;

    /**
    * 
    */
    private void setId(Long id){
    this.id = id;
    }

    /**
    * 
    */
    private void setCompanyId(Long companyId){
    this.companyId = companyId;
    }

    /**
    * 
    */
    private void setEmployeeId(Long employeeId){
    this.employeeId = employeeId;
    }

    /**
    * 薪资月份
    */
    private void setPayrollMonth(String payrollMonth){
    this.payrollMonth = payrollMonth;
    }

    /**
    * 应出勤天数
    */
    private void setWorkingDays(Integer workingDays){
    this.workingDays = workingDays;
    }

    /**
    * 实际出勤天数
    */
    private void setActualDays(Integer actualDays){
    this.actualDays = actualDays;
    }

    /**
    * 加班小时数
    */
    private void setOvertimeHours(BigDecimal overtimeHours){
    this.overtimeHours = overtimeHours;
    }

    /**
    * 请假天数（含事假病假）
    */
    private void setLeaveDays(BigDecimal leaveDays){
    this.leaveDays = leaveDays;
    }


    /**
    * 
    */
    private Long getId(){
    return this.id;
    }

    /**
    * 
    */
    private Long getCompanyId(){
    return this.companyId;
    }

    /**
    * 
    */
    private Long getEmployeeId(){
    return this.employeeId;
    }

    /**
    * 薪资月份
    */
    private String getPayrollMonth(){
    return this.payrollMonth;
    }

    /**
    * 应出勤天数
    */
    private Integer getWorkingDays(){
    return this.workingDays;
    }

    /**
    * 实际出勤天数
    */
    private Integer getActualDays(){
    return this.actualDays;
    }

    /**
    * 加班小时数
    */
    private BigDecimal getOvertimeHours(){
    return this.overtimeHours;
    }

    /**
    * 请假天数（含事假病假）
    */
    private BigDecimal getLeaveDays(){
    return this.leaveDays;
    }

}
