package cn.loblok.lcyerp01.service;

import cn.loblok.lcyerp01.dto.BudgetRecordRepository;
import cn.loblok.lcyerp01.entity.BudgetRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetQueryService {

    @Autowired
    private BudgetRecordRepository repository;

    // 模拟一个常见的复杂查询：按部门、年份、月份范围、状态查询
    public List<BudgetRecord> queryBudgets(Integer deptId, Integer year, Byte startMonth, Byte endMonth, Byte status) {
        return repository.findByDeptIdAndYearAndMonthBetweenAndStatus(deptId, year, startMonth, endMonth, status);
    }
}

