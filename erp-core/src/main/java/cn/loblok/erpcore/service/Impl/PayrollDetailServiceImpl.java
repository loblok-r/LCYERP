package cn.loblok.erpcore.service.Impl;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.erpcore.dao.PayrollDetailRepository;
import cn.loblok.common.dto.PayRequest;
import cn.loblok.erpcore.entity.PayrollDetail;
import cn.loblok.erpcore.event.MessageConfirmEvent;
import cn.loblok.erpcore.service.PayrollDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollDetailServiceImpl implements PayrollDetailService {

    private final PayrollDetailRepository payrollDetailRepository;
    private  final AlertService alertService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 监听 Confirm 事件
    @EventListener
    @Transactional
    public void handleMessageConfirm(MessageConfirmEvent event) {
        try {
            Long bizId = Long.parseLong(event.getBizId());
            if (event.isAck()) {
                transitionStatus(bizId, PayrollStatus.SUBMITTED_TO_MQ);
                log.info("✅ 消息确认成功, bizId={}", bizId);
            } else {
                transitionStatus(bizId, PayrollStatus.MQ_SEND_FAILED);
                log.error("❌ 消息确认失败, bizId={}, 原因: {}", bizId, event.getCause());
            }
        } catch (Exception e) {
            log.error("处理消息确认事件异常", e);
        }
    }




    // 即使支付失败也要持久化状态，因此禁用自动回滚
    @Transactional(noRollbackFor = Exception.class)
    @Override
    public void processSingle(PayrollDetail detail) {

        // 1. 获取银行通道
        //Optional<String> bankCode = configRepository.findBankCodeByCompanyId(detail.getCompanyId());
        String bankCode = detail.getBankCode(); // 直接使用实体中的快照值

        if (bankCode  == null) {
            log.warn("💼 payroll 缺少 bankCode, bizId={}", detail.getBizId());
            transitionStatus(detail.getId(), PayrollStatus.FAILED);
            return;
        }

            // 2. 构造请求
            PayRequest request = new PayRequest();
            request.setBizId(detail.getBizId());
            request.setEmployeeId(detail.getEmployeeId());
            request.setBankCard(detail.getBankCard());
            request.setAmount(detail.getAmount());
            
            String routingKey = bankCode.toLowerCase(); // "ICBC" → "icbc"

            CorrelationData correlationData = new CorrelationData(detail.getBizId()); // 用 bizId 作为 ID
            rabbitTemplate.convertAndSend(
                    "salary.pay.exchange", // 交换机名
                    routingKey,
                    request,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }, //消息持久化
                    correlationData // 👈 传入，用于 confirm 回调匹配
            );
            
           // transitionStatus(detail.getId(),PayrollStatus.SUBMITTED);

    }


    //状态转换
    @Transactional
    @Override
    public void transitionStatus(long id, PayrollStatus newStatus) {
        PayrollDetail detail = payrollDetailRepository.findById(id).get();
        if (detail == null) {
            throw new IllegalArgumentException("记录不存在");
        }

        PayrollStatus current = PayrollStatus.valueOf(detail.getStatus());
        if (!current.canTransitionTo(newStatus)) {
            log.warn("❌ 非法状态流转: {} -> {}", current, newStatus);
            throw new IllegalStateException(
                    String.format("非法状态流转: %s -> %s", current, newStatus)
            );
        }

        payrollDetailRepository.updateStatus(id, newStatus.name(), detail.getRetryCount());
    }

    @Override
    public List<PayrollDetail> findPendingBatch(long lastId, int limit) {
//        Pageable pageable = PageRequest.of(offset, limit, Sort.by("id").ascending());
        Pageable page = PageRequest.of(0, limit); // 每次只取第一页（limit 条）
        return payrollDetailRepository.findPendingBatch(lastId, page);
    }

    @Override
    public List<PayrollDetail> findSentRecordsBatch(long lastId, int limit) {
        Pageable page = PageRequest.of(0, limit);
        return payrollDetailRepository.findSentRecordsBatch(lastId, page);
    }


    @Override
    public boolean isProcessed(String bizId) {

        return false;
    }



}