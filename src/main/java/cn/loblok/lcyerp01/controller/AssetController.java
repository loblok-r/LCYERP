package cn.loblok.lcyerp01.controller;

import cn.loblok.lcyerp01.dto.ApiResponse;
import cn.loblok.lcyerp01.service.AssetExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/asset")
public class AssetController {

    @Autowired
    private AssetExportService exportService;


    @PostMapping("/export/v1")
    public ResponseEntity<ApiResponse<String>> exportV1Debug(
            @RequestParam(defaultValue = "assets_v1.xlsx") String filename) {

        // 安全校验：防止覆盖系统文件
//        if (!filename.matches("^[a-zA-Z0-9_\\-]+\\.xlsx$")) {
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error("文件名非法"));
//        }

//        String outputPath = "/tmp/" + filename; // Linux/macOS
         String outputPath = "D:\\面试\\找工作\\temp\\" + filename; // Windows

        try {
            long start = System.currentTimeMillis();
            exportService.exportAllToFileV1(outputPath);
            long cost = System.currentTimeMillis() - start;

            String msg = String.format("导出完成！文件: %s, 耗时: %d ms", outputPath, cost);
            System.out.println(msg);
            return ResponseEntity.ok(ApiResponse.success(msg));

        } catch (Exception e) {
            String errorMsg = "导出失败: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(errorMsg));
        }
    }

    @GetMapping("/export/excel-stream") // 改用 GET，符合“下载”语义
    public void exportExcelStream(HttpServletResponse response) throws IOException {
        String filename = "assets_export_" + System.currentTimeMillis() + ".xlsx";
        String outputPath = "D:\\面试\\找工作\\temp\\" + filename;

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(outputPath, StandardCharsets.UTF_8));

        exportService.exportAllToExcelV2(response.getOutputStream());
        response.getOutputStream().flush();
    }



}