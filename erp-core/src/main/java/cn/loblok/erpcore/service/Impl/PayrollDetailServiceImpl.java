package cn.loblok.erpcore.service.Impl;

import cn.loblok.common.Enum.PayrollStatus;
import cn.loblok.common.dao.PayrollDetailRepository;
import cn.loblok.common.dto.PayRequest;
import cn.loblok.common.entity.PayrollDetail;
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
    //private  final AlertService alertService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 监听 Confirm 事件
    @EventListener
    @Transactional
    public void handleMessageConfirm(MessageConfirmEvent event) {
        try {
            String bizId = event.getBizId();

            PayrollStatus target = event.isAck() ?
                    PayrollStatus.SUBMITTED_TO_MQ : PayrollStatus.MQ_SEND_FAILED;

            // 只允许从 PENDING 转移
            int updated = payrollDetailRepository.updateStatusIfMatch(
                    bizId,
                    PayrollStatus.PENDING,
                    target
            );

            if (updated == 0) {
                log.warn("bizId={} 状态不可变，跳过 confirm 处理", bizId);
            }
        } catch (Exception e) {
            log.error("处理消息确认事件异常", e);
        }
    }


    // 即使支付失败也要持久化状态，因此禁用自动回滚
    @Transactional(noRollbackFor = Exception.class)
    @Override
    public void processSingle(PayrollDetail detail) {

        // 防止同一 bizId 被多次处理
        if (detail.getStatus() != PayrollStatus.PENDING) {
            log.info("bizId={} 状态已变更（{}），跳过发送", detail.getBizId(), detail.getStatus());
            return;
        }
        //这样即使调度任务重复扫描到同一条记录，也会因状态不是 PENDING 而跳过
        sendMessageToQueue(detail);

        // transitionStatus(detail.getId(),PayrollStatus.SUBMITTED);

    }

    /**
     * 重试发送失败的消息
     */
    @Transactional
    public void retryFailedDispatch(String bizId) {

        final int MAX_RETRY_COUNT = 3;
        PayrollDetail detail = payrollDetailRepository.findByBizId(bizId);
        if (detail == null) {
            throw new IllegalArgumentException("薪资记录不存在: " + bizId);
        }

        if (detail.getStatus() != PayrollStatus.MQ_SEND_FAILED) {
            log.warn("记录状态不是MQ_SEND_FAILED，无法重试: {}", detail.getStatus());
            return;
        }

        try {
            // 重新发送消息
            sendMessageToQueue(detail);

            // 更新状态为SUBMITTED_TO_MQ
            payrollDetailRepository.updateStatusIfMatch(
                    bizId,
                    PayrollStatus.MQ_SEND_FAILED,
                    PayrollStatus.SUBMITTED_TO_MQ
            );

            log.info("薪资记录重试发送成功: {}", bizId);
        } catch (Exception e) {
            log.error("重试发送薪资消息失败: {}", bizId, e);
            // 增加重试次数
            detail.setRetryCount(detail.getRetryCount() + 1);
            payrollDetailRepository.save(detail);

            // 如果超过最大重试次数，发送告警
            if (detail.getRetryCount() >= MAX_RETRY_COUNT) {
                // alertService.notifyOps("薪资发放重试次数超限: " + bizId);
            }
        }
    }

    //发送消息
    private void sendMessageToQueue(PayrollDetail detail) {
        // 1. 获取银行通道
        //Optional<String> bankCode = configRepository.findBankCodeByCompanyId(detail.getCompanyId());
        String bankCode = detail.getBankCode(); // 直接使用实体中的快照值

        if (bankCode == null) {
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
                correlationData //传入，用于 confirm 回调匹配
        );
    }


    //状态转换
    @Transactional
    @Override
    public void transitionStatus(long id, PayrollStatus newStatus) {
        PayrollDetail detail = payrollDetailRepository.findById(id).get();
        if (detail == null) {
            throw new IllegalArgumentException("记录不存在");
        }

        PayrollStatus current = detail.getStatus();
        if (!current.canTransitionTo(newStatus)) {
            log.warn("非法状态流转: {} -> {}", current, newStatus);
            throw new IllegalStateException(
                    String.format("非法状态流转: %s -> %s", current, newStatus)
            );
        }

        payrollDetailRepository.updateStatus(id, newStatus, detail.getRetryCount());
    }

    @Override
    public List<PayrollDetail> findBatchByStatus(PayrollStatus payrollStatus, long lastId, int limit) {
        Pageable page = PageRequest.of(0, limit); // 每次只取第一页（limit 条）
        return payrollDetailRepository.findByStatusAndIdGreaterThan(PayrollStatus.PENDING, lastId, page);
    }


    @Override
    public boolean isProcessed(String bizId) {

        return false;
    }

    /**
     * 触发满足条件的发薪动作
     */
    public int markThisMonthAsPending(String payrollMonth, Long companyId) {
        return payrollDetailRepository.updateStatusToPending(PayrollStatus.SCHEDULED, PayrollStatus.PENDING, payrollMonth, companyId);
    }

    /**
     * 触发满足条件的薪资计算动作
     */
    public void markThisMonthForCalculation(String payrollMonth, Long companyId) {
        int count = payrollDetailRepository.updateStatusToPending(
                PayrollStatus.DRAFT,
                PayrollStatus.PENDING_CALCULATION,
                payrollMonth, companyId);

        if (count > 0) {
            log.info("已激活 {} 条 {} 月薪资记录进入发薪队列", count, payrollMonth);
        }
    }
}