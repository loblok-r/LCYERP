package cn.loblok.erpcore.controller;

import cn.loblok.common.Enum.ExportStatus;
import cn.loblok.common.dto.ApiResponse;
import cn.loblok.erpcore.entity.ExportTask;
import cn.loblok.erpcore.manager.ExportTaskManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportTaskManager taskManager;
    //提交导出任务（触发异步处理）
    @PostMapping("/async")
    public ApiResponse<String> submitExport() {
        String taskId = taskManager.submitExportTask();
        return ApiResponse.success(taskId);
    }

    //轮询任务状态（等待完成）
    @GetMapping("/status/{taskId}")
    public ApiResponse<ExportTask> getExportStatus(@PathVariable String taskId) {
        ExportTask task = taskManager.getTask(taskId);
        if (task == null) {
            return ApiResponse.error("Task not found");
        }
        return ApiResponse.success(task);
    }

    //下载导出文件（zip文件）
    @GetMapping("/download/{taskId}")
    public void downloadExport(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        ExportTask task = taskManager.getTask(taskId);
        if (task == null || !ExportStatus.SUCCESS.equals(task.getStatus())) {

            // ✅ 返回 JSON 错误
            writeJsonErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "任务未完成或不存在");
            return;
        }

        Path file = Paths.get(task.getOutputFile());
        if (!Files.exists(file)) {
            writeJsonErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "导出文件已过期或不存在");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getFileName());
        Files.copy(file, response.getOutputStream());
    }
    private void writeJsonErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("code", status);
        error.put("message", message);

        String json = new ObjectMapper().writeValueAsString(error);
        response.getWriter().write(json);
    }
    //下载导出文件（获取结果）
//    @GetMapping("/download/{taskId}")
//    public void downloadExportV(@PathVariable String taskId, HttpServletResponse response) throws IOException {
//        ExportTask task = taskManager.getTask(taskId);
//        if (task == null || task.getStatus() != ExportStatus.SUCCESS) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Export not ready");
//            return;
//        }
//
//        String filePath = task.getOutputFile();
//        File file = new File(filePath);
//        if (!file.exists()) {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
//            return;
//        }
//
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition", "attachment; filename=" +
//                URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
//
//        try (FileInputStream fis = new FileInputStream(file);
//             OutputStream os = response.getOutputStream()) {
//            fis.transferTo(os);
//            os.flush();
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}