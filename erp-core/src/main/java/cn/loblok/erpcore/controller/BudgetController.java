package cn.loblok.erpcore.controller;

import cn.loblok.erpcore.dto.BudgetPageResponse;
import cn.loblok.erpcore.entity.BudgetRecord;
import cn.loblok.erpcore.service.Impl.BudgetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

    @GetMapping("/listpage")
    public ResponseEntity<Page<BudgetRecord>> getPage(
            @RequestParam Integer deptId,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "1") Byte status,
            @RequestParam Integer page,
            @RequestParam Integer size
    ){

        long start = System.currentTimeMillis();
        Page<BudgetRecord> result = service.getDeepPage(deptId, year,status, page, size);
//        List<BudgetRecord> result = service.getDeepPageByCursor(deptId, year,status, cursorTime,cursorId, size);
        long cost = System.currentTimeMillis() - start;
        return ResponseEntity.ok(result);
    }

    @GetMapping("/listpage/cursor")
    public BudgetPageResponse getPage(
            @RequestParam Integer deptId,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "1") Byte status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam Integer size
    ){

        long start = System.currentTimeMillis();
        BudgetPageResponse pageByCursor = service.getPageByCursor(deptId, year, status, cursorTime, cursorId, size);
//        List<BudgetRecord> result = service.getDeepPageByCursor(deptId, year,status, cursorTime,cursorId, size);
        long cost = System.currentTimeMillis() - start;
        return pageByCursor;
    }








}