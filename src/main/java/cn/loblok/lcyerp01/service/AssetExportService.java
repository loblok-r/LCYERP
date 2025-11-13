package cn.loblok.lcyerp01.service;

import cn.loblok.lcyerp01.dao.AssetDetailRepository;
import cn.loblok.lcyerp01.entity.AssetDetail;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


@Service
public class AssetExportService {

    @Autowired
    private AssetDetailRepository repository;

    public void exportAllToFileV1(String outputPath) throws Exception {
        System.out.println("【V1】开始全量查询...");
        long start = System.currentTimeMillis();

        // ❌ 全量加载到内存
        List<AssetDetail> allAssets = repository.findAll();
        System.out.println("查询完成，共 " + allAssets.size() + " 条，耗时: "
                + (System.currentTimeMillis() - start) + " ms");

        // ❌ 使用 XSSFWorkbook（全内存模式）
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("资产明细");

        // 写标题
        writeHeader(sheet, 0);

        // 写数据
        for (int i = 0; i < allAssets.size(); i++) {
            AssetDetail a = allAssets.get(i);
            Row row = sheet.createRow(i + 1);
            writeAssetRow(row, a);
        }

        // 写文件
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            workbook.write(out);
        }
        workbook.close();

        System.out.println("导出完成，总耗时: " + (System.currentTimeMillis() - start) + " ms");
    }

    public void exportAllToExcelV2(OutputStream outputStream) throws IOException {
        final int BATCH_SIZE = 2000;      // 每批查 2000 条
        final int WINDOW_SIZE = 100;       // SXSSF 内存保留 100 行
        final int MAX_ROWS_PER_SHEET = 1_048_575; // 留1行给标题？不，标题也算一行！

        long lastId = 0L;
        int rowNum = 0;
        int totalRowCount = 0; // 总行数（含标题）

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(WINDOW_SIZE)) {

            Sheet currentSheet = workbook.createSheet("资产明细_" + (totalRowCount / MAX_ROWS_PER_SHEET + 1));

            // 写标题行（每个 sheet 都要有标题）
            writeHeader(currentSheet, 0);
            totalRowCount = 1; // 标题占第0行，下一行从1开始

            List<AssetDetail> batch;
            do {
                // 分页查询：id > lastId，取 BATCH_SIZE 条
                Pageable page = PageRequest.of(0, BATCH_SIZE, Sort.by("id").ascending());
                batch = repository.findNextBatch(lastId, page);

                for (AssetDetail asset : batch) {
                    // 检查是否需要新 sheet
                    if (totalRowCount % MAX_ROWS_PER_SHEET == 0) {
                        // 当前行号 == MAX_ROWS_PER_SHEET 时，必须换 sheet
                        currentSheet = workbook.createSheet("资产明细_" + (totalRowCount / MAX_ROWS_PER_SHEET + 1));
                        writeHeader(currentSheet, 0);
                        totalRowCount = 1; // 新 sheet 的标题行
                    }
                    Row row = currentSheet.createRow(totalRowCount % MAX_ROWS_PER_SHEET);
                    writeAssetRow(row, asset);
                    totalRowCount++;
                }

                if (!batch.isEmpty()) {
                    lastId = batch.get(batch.size() - 1).getId(); // 更新游标
                }

                // 可选：定期 flush（SXSSF 会自动管理，但显式 flush 更安全）
                // ((SXSSFSheet) sheet).flushRows();

            } while (batch.size() == BATCH_SIZE); // 没满说明到底了

            // 写出到输出流
            workbook.write(outputStream);
        }
        // dispose() 会自动删除临时文件
    }

    // 抽离写标题逻辑
    private void writeHeader(Sheet sheet, int rowIndex) {
        Row header = sheet.createRow(rowIndex);
        String[] headers = {"ID", "资产编码", "名称", "类别", "公司", "部门", "原值", "状态", "采购日期", "位置"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }
    // 抽离写数据行逻辑
    private void writeAssetRow(Row row, AssetDetail asset) {
        int col = 0;
        row.createCell(col++).setCellValue(asset.getId());
        row.createCell(col++).setCellValue(asset.getAssetCode());
        row.createCell(col++).setCellValue(asset.getAssetName());
        row.createCell(col++).setCellValue(asset.getCategoryName());
        row.createCell(col++).setCellValue(asset.getCompanyName());
        row.createCell(col++).setCellValue(asset.getDeptName());
        row.createCell(col++).setCellValue(asset.getOriginalValue() != null ? asset.getOriginalValue().doubleValue() : 0.0);
        row.createCell(col++).setCellValue(getStatusText(asset.getStatus()));
        row.createCell(col++).setCellValue(asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : "");
        row.createCell(col++).setCellValue(asset.getLocation());
    }


    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "在用"; case 1 -> "闲置"; case 2 -> "维修中"; case 3 -> "报废";
            default -> "未知";
        };
    }
}