package file.platform.controller;

import file.platform.entity.DefaultAvatar;
import file.platform.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ImageController {

    @Autowired
    private AvatarService avatarService;

    /**
     * 上传默认头像
     * @param file 图片文件
     * @param recommendedGender 推荐使用性别：0-通用，1-男性，2-女性
     * @return 操作结果
     */
    @PostMapping("/uploadDefaultAvatar")
    public ResponseEntity<Map<String, Object>> uploadDefaultAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "recommendedGender", defaultValue = "0") Integer recommendedGender) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "只支持图片文件");
                return ResponseEntity.badRequest().body(result);
            }

            // 验证推荐性别参数
            if (recommendedGender < 0 || recommendedGender > 2) {
                result.put("success", false);
                result.put("message", "推荐性别参数无效：0-通用，1-男性，2-女性");
                return ResponseEntity.badRequest().body(result);
            }

            // 获取文件字节数组
            byte[] imageBytes = file.getBytes();

            // 调用AvatarService处理头像上传
            String uri = avatarService.uploadDefaultAvatar(imageBytes, recommendedGender);

            result.put("success", true);
            result.put("message", "默认头像上传成功");
            result.put("recommendedGender", recommendedGender);
            result.put("uri", uri);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 获取随机头像
     * @param gender 性别：male-男性，female-女性，null-不限制
     * @param excludeIds 排除的ID列表，用逗号分隔（如：1,2,3）
     * @return 随机头像URL
     */
    @GetMapping("/getRandomAvatar")
    public ResponseEntity<Map<String, Object>> getRandomAvatar(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "excludeIds", required = false) String excludeIds) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 解析排除ID列表
            List<Long> excludeIdList = null;
            if (excludeIds != null && !excludeIds.trim().isEmpty()) {
                excludeIdList = Arrays.stream(excludeIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .distinct()
                        .collect(Collectors.toList());
            }

            // 验证gender参数
            if (gender != null && !gender.trim().isEmpty()) {
                String lowerGender = gender.trim().toLowerCase();
                if (!"male".equals(lowerGender) && !"female".equals(lowerGender)) {
                    result.put("success", false);
                    result.put("message", "性别参数无效：只能为'male'、'female'或不传");
                    return ResponseEntity.badRequest().body(result);
                }
            }

            // 调用AvatarService获取随机头像
            DefaultAvatar avatar = avatarService.getRandomAvatar(gender, excludeIdList);

            if (avatar == null) {
                result.put("success", false);
                result.put("message", "已经是最后一张啦");
                return ResponseEntity.ok(result);
            }


            result.put("success", true);
            result.put("avatarUrl", avatar.getAvatarUrl());
            result.put("avatarId", avatar.getId());
            result.put("message", "获取随机头像成功");

            return ResponseEntity.ok(result);

        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "排除ID列表格式无效：请使用数字ID，用逗号分隔");
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取随机头像失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}


