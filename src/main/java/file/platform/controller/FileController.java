package file.platform.controller;

import file.platform.service.FileHostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务REST控制器
 * 提供文件上传、下载、列表等HTTP接口
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileHostingService fileHostingService;

    /**
     * 删除文件
     * @param fileName 要删除的文件名
     * @return 删除结果
     */
    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        try {
            boolean success = fileHostingService.deleteFile(fileName);
            if (success) {
                return ResponseEntity.ok("文件删除成功: " + fileName);
            } else {
                return ResponseEntity.status(500).body("文件删除失败: " + fileName);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("删除文件时发生异常: " + e.getMessage());
        }
    }
}