package cn.loblok.erpcore.controller;



import cn.loblok.common.dto.PayrollCallbackRequest;
import cn.loblok.erpcore.service.Impl.PayrollDetailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/api/payroll")
@Slf4j
public class PayrollController {

    @Autowired
    PayrollDetailServiceImpl payrollDetailServiceImpl;


    /**
     * 银行通道服务回调：更新薪资状态
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> callBack(@RequestBody PayrollCallbackRequest request){
        if(request.getBizId() == null || request.getStatus() == null ){
            return ResponseEntity.badRequest().build();
        }
        try {
            payrollDetailServiceImpl.transitionStatus(Long.parseLong(request.getBizId()), request.getStatus());
            return ResponseEntity.ok().build(); // 200 OK 表示处理成功
        } catch (Exception e) {
            log.error("回调处理失败, bizId={}", request.getBizId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 幂等检查：判断该业务ID是否已处理
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkProcessed(@RequestParam String bizId){

        if(bizId == null || bizId.trim().isEmpty()){
            return ResponseEntity.badRequest().body(false);
        }

        boolean processed = payrollDetailServiceImpl.isProcessed(bizId);

        return ResponseEntity.ok(processed);

    }
}