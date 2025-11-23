package cn.loblok.bankchannelservice.service.impl;

import cn.loblok.bankchannelservice.service.PayrollCallbackService;
import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dto.PayrollCallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

//在银行代发处理完成后（无论成功或失败），将结果“回调”回薪资主流程，更新对应薪资明细（PayrollDetail）的状态
@Service
@Slf4j
public class PayrollCallbackServiceImpl implements PayrollCallbackService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erp.core.url}") // e.g., http://erp-core
    private String erpCoreUrl;

    @Override
    public void updateStatus(String bizId, PayrollStatus status) {
        // ✅ 使用 DTO 封装请求体
        PayrollCallbackRequest request = new PayrollCallbackRequest();
        request.setBizId(bizId);
        request.setStatus(status); // 注意：这里传的是枚举对象，不是 .name()
        restTemplate.postForObject(erpCoreUrl + "/api/payroll/callback", request, Void.class);
    }

    @Override
    public boolean isProcessed(String bizId) {
        // 查询 ERP Core 数据库 or 缓存
        ResponseEntity<Boolean> resp = restTemplate.getForEntity(
                erpCoreUrl + "/api/payroll/check?bizId=" + bizId, Boolean.class
        );
        return resp.getBody();
    }
}