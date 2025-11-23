package cn.loblok.erpcore.controller;

import cn.loblok.erpcore.dao.AssetDetailRepository;
import cn.loblok.common.dto.ApiResponse;
import cn.loblok.erpcore.entity.AssetDetail;
import cn.loblok.erpcore.manager.AssetValidationCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private AssetValidationCoordinator validationCoordinator;

    @Autowired
    private AssetDetailRepository repository;

    @GetMapping("/validation")
    public ApiResponse<String> testAssetValidationCoordinator(){
        // 1. 加载全量数据（或分页加载）
        List<AssetDetail> assets = repository.findAll(); // 或分批查
        // 2. 【新增】前置校验
        if (!validationCoordinator.runValidations(assets)) {
            System.out.println("数据校验失败..");
            return ApiResponse.error("数据校验失败");
        }else{
            System.out.println("数据校验通过..");
            return ApiResponse.success();
        }
    }
}