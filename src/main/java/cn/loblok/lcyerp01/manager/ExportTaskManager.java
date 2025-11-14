package cn.loblok.lcyerp01.manager;

import cn.loblok.lcyerp01.Enum.ExportStatus;
import cn.loblok.lcyerp01.dao.AssetDetailRepository;
import cn.loblok.lcyerp01.entity.AssetDetail;
import cn.loblok.lcyerp01.entity.ExportTask;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ExportTaskManager {
    private final Map<String, ExportTask> tasks = new ConcurrentHashMap<>();


    //用于提交顶层任务
    private final ExecutorService exportExecutor = Executors.newFixedThreadPool(
            Math.min(16,Runtime.getRuntime().availableProcessors() * 2)
    );

    // 用于分片
    private final ExecutorService segmentExecutor = Executors.newFixedThreadPool(
            Math.min(16, Runtime.getRuntime().availableProcessors() * 2)
    );
    private final AssetDetailRepository repository;
    private final String tempDir = "D:\\面试\\找工作\\temp\\";

    public ExportTaskManager(AssetDetailRepository repository) {
        this.repository = repository;
    }
    public String submitExportTask() {
        String taskId = "task_" + System.currentTimeMillis();
        ExportTask task = new ExportTask();
        task.setTaskId(taskId);
        tasks.put(taskId, task);

        // 异步执行
        exportExecutor.submit(()->processExport(taskId));
        // 这是任务级并行（不同用户的导出请求并行
        return taskId;
    }

    private void processExport(String taskId) {
        ExportTask task = tasks.get(taskId);
        try {
            task.setStatus(ExportStatus.RUNNING);
            String finalFile = doParallelExport(taskId);
            task.setOutputFile(finalFile);
            task.setStatus(ExportStatus.SUCCESS);
        } catch (Exception e) {
            task.setStatus(ExportStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            e.printStackTrace();
        }
    }

    public ExportTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    // ===== 核心：并行分片导出 =====
    private String doParallelExport(String taskId) throws Exception {
        // 1. 获取 ID 范围
        Long minId = repository.findMinId();
        Long maxId = repository.findMaxId();
        if (minId == null || maxId == null) throw new RuntimeException("No data");

        int cores = Runtime.getRuntime().availableProcessors();
        int threadCount = Math.min(16, Math.max(2, cores * 2)); // 范围 [2, 16]

        long segmentSize = (maxId - minId + 1) / threadCount;
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            long startId = minId + i * segmentSize;
            long endId = (i == threadCount - 1) ? maxId : startId + segmentSize - 1;

            final int partNum = i; // ← 创建 effectively final 副本

            CompletableFuture<String> future = CompletableFuture
                    .supplyAsync(() -> exportSegment(startId, endId, taskId + "_part" + partNum), segmentExecutor)
                    .exceptionally(ex -> {
                        throw new RuntimeException("Export segment failed", ex);
                    });
            futures.add(future);
        }
        // 等待所有分片完成
        List<String> partFiles = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // 过滤 null
                .collect(Collectors.toList());


        System.out.println("All segments done, merging: " + partFiles);

        if (partFiles.isEmpty()) {
            throw new RuntimeException("No data exported");
        }
        // 合并分片（按顺序创建多 Sheet）
//        return mergeParts(partFiles, taskId);

        // 改为生成 ZIP
        return createZipFromParts(partFiles, taskId);
    }

    // 导出一个 ID 段到临时文件
    private String exportSegment(long startId, long endId, String partName) {
        System.out.println("Exporting segment: " + startId + " -> " + endId + " (" + partName + ")");
        String tempFile = tempDir + partName + ".xlsx";
        final int BATCH_SIZE = 5000;
        long currentId = startId; // 游标，从 startId 开始（注意：第一个 id 可能是 startId+1）

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Part");
            writeHeader(sheet, 0);
            int rowNum = 1;

            while (currentId < endId) {
                // 查询 currentId 到 endId 之间的下一批数据
                List<AssetDetail> batch = repository.findNextInSegmentNative(currentId, endId, BATCH_SIZE);//每次从上次结束的位置往后查 5000 条，直到 endId
                if (batch.isEmpty()) break;

                for (AssetDetail asset : batch) {
                    Row row = sheet.createRow(rowNum++);
                    writeAssetRow(row, asset);
                }

                // 更新游标：取最后一条的 id
                currentId = batch.get(batch.size() - 1).getId();
            }

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Write segment file failed", e);
        }
    }

    // 合并多个分片文件为一个（每个分片作为单独 Sheet）,大文件合并太慢，弃用

    /**
     *
     * partFiles:["D:\面试\找工作\temp\taskId_part1.xlsx",
     *              "D:\面试\找工作\temp\taskId_part2.xlsx",
     *              "D:\面试\找工作\temp\taskId_part3.xlsx"]
     */

//    private String mergeParts(List<String> partFiles, String taskId) throws IOException {
//        System.out.println("Starting merge for task: " + taskId + ", files: " + partFiles.size());
//        String finalFile = tempDir + "assets_export_" + taskId + ".xlsx";
//        try (XSSFWorkbook merged = new XSSFWorkbook()) {
//            for (int i = 0; i < partFiles.size(); i++) {
//                System.out.println("Merging part: " + partFiles.get(i));
//                try (FileInputStream fis = new FileInputStream(partFiles.get(i));
//                     XSSFWorkbook part = new XSSFWorkbook(fis)) {
//                    Sheet srcSheet = part.getSheetAt(0);
//                    Sheet destSheet = merged.createSheet("资产明细_" + (i + 1));
//
//                    // 复制行（跳过标题？不，每个分片都有标题，但合并时只保留第一个）
//                    for (Row srcRow : srcSheet) {
//                        Row destRow = destSheet.createRow(srcRow.getRowNum());
//                        for (Cell srcCell : srcRow) {
//                            Cell destCell = destRow.createCell(srcCell.getColumnIndex());
//                            copyCellValue(srcCell, destCell);
//                        }
//                    }
//                }
//            }
//
//            try (FileOutputStream fos = new FileOutputStream(finalFile)) {
//                merged.write(fos);
//            }
//
//            // 清理临时分片文件
//            partFiles.forEach(file -> {
//                try {
//                    Files.deleteIfExists(Paths.get(file));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//
//            return finalFile;
//        }
//    }

    private String createZipFromParts(List<String> partFiles, String taskId) throws IOException {
        String zipFile = tempDir + "assets_export_" + taskId + ".zip";
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (int i = 0; i < partFiles.size(); i++) {
                String part = partFiles.get(i);
                if (!Files.exists(Paths.get(part))) continue;

                ZipEntry entry = new ZipEntry("资产明细_" + (i + 1) + ".xlsx");
                zos.putNextEntry(entry);

                Files.copy(Paths.get(part), zos);
                zos.closeEntry();
            }
        }

        // 清理分片
        partFiles.forEach(f -> {
            try { Files.deleteIfExists(Paths.get(f)); } catch (IOException e) { /* ignore */ }
        });

        return zipFile;
    }
    // 工具方法：复制单元格值（简单版）
    private void copyCellValue(Cell src, Cell dest) {
        switch (src.getCellType()) {
            case STRING -> dest.setCellValue(src.getStringCellValue());
            case NUMERIC -> dest.setCellValue(src.getNumericCellValue());
            case BOOLEAN -> dest.setCellValue(src.getBooleanCellValue());
            default -> dest.setCellValue("");
        }
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