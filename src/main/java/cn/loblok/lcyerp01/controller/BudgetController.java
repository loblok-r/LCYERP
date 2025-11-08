package cn.loblok.lcyerp01.controller;

import cn.loblok.lcyerp01.entity.BudgetRecord;
import cn.loblok.lcyerp01.service.BudgetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    @Autowired
    private BudgetQueryService service;

    @GetMapping("/list")
    public ResponseEntity<List<BudgetRecord>> listBudgets(
            @RequestParam Integer deptId,
            @RequestParam Integer year,
            @RequestParam Byte startMonth,
            @RequestParam Byte endMonth,
            @RequestParam(defaultValue = "1") Byte status) {

        long startTime = System.currentTimeMillis();
        List<BudgetRecord> result = service.queryBudgets(deptId, year, startMonth, endMonth, status);
        long endTime = System.currentTimeMillis();

        System.out.println("【查询耗时】" + (endTime - startTime) + " ms");

        return ResponseEntity.ok(result);
    }
}