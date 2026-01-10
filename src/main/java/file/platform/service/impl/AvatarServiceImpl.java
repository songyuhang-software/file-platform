package file.platform.service.impl;

import file.platform.dao.DefaultAvatarMapper;
import file.platform.entity.DefaultAvatar;
import file.platform.service.AvatarService;
import file.platform.service.FileHostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class AvatarServiceImpl implements AvatarService {

    @Autowired
    private FileHostingService fileHostingService;

    @Autowired
    private DefaultAvatarMapper defaultAvatarMapper;

    @Override
    public String uploadDefaultAvatar(byte[] imageBytes, Integer recommendedGender) {
        // 创建临时文件
        try {
            Path tempFile = Files.createTempFile("avatar_", ".jpg");
            Files.write(tempFile, imageBytes);

            File file = tempFile.toFile();

            // 上传文件并获取URL
            String avatarUrl = fileHostingService.uploadImage(file);

            // 保存到数据库
            DefaultAvatar defaultAvatar = new DefaultAvatar(avatarUrl, recommendedGender);
            defaultAvatarMapper.insert(defaultAvatar);

            // 清理临时文件
            Files.deleteIfExists(tempFile);

            return avatarUrl;

        } catch (IOException e) {
            throw new RuntimeException("处理头像文件时发生错误", e);
        }
    }

    @Override
    public String getRandomAvatar(String gender, List<Long> excludeIds) {
        // 将gender字符串转换为数字
        Integer genderValue = null;
        if ("male".equalsIgnoreCase(gender)) {
            genderValue = 1;
        } else if ("female".equalsIgnoreCase(gender)) {
            genderValue = 2;
        }
        // 如果gender为null或空字符串，genderValue保持null（表示查询所有）

        // 查询符合条件的所有记录数量
        int availableCount = defaultAvatarMapper.countByGender(genderValue);

        if (availableCount == 0) {
            return null; // 没有可用的头像
        }

        // 如果排除了所有记录，返回null
        if (excludeIds != null && excludeIds.size() >= availableCount) {
            return "已经是最后一张啦";
        }

        // 随机查询一条记录
        List<DefaultAvatar> result = defaultAvatarMapper.selectRandomByGenderAndExcludes(genderValue, excludeIds);

        if (result != null && !result.isEmpty()) {
            return result.get(0).getAvatarUrl();
        }

        return null; // 没有找到符合条件的结果
    }
}



