package file.platform.controller;

import file.platform.entity.DefaultAvatar;
import file.platform.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 头像服务REST控制器
 * 提供头像相关的HTTP接口
 */
@RestController
@RequestMapping("/api/avatars")
@CrossOrigin(origins = "*")
public class AvatarController {

    @Autowired
    private AvatarService avatarService;

    /**
     * 获取所有默认头像列表
     * @return 所有默认头像的列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAvatars() {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用AvatarService获取所有头像
            List<DefaultAvatar> avatars = avatarService.getAllAvatars();

            result.put("success", true);
            result.put("message", "获取所有默认头像成功");
            result.put("data", avatars);
            result.put("total", avatars != null ? avatars.size() : 0);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取所有默认头像失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}