package file.platform.service.impl;

import com.UpYun;
import com.upyun.FormUploader;
import com.upyun.Result;
import com.upyun.UpException;
import file.platform.dao.DefaultAvatarMapper;
import file.platform.entity.DefaultAvatar;
import file.platform.service.FileHostingService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;


@Service
public class UpyunFileHostingServiceImpl implements FileHostingService {

    // 又拍云配置
    private static final String BUCKET = "agent69-image";
    private static final String OPERATOR = "songyuhang";
    private static final String PASSWORD = "DCbg9f9MT2NrT9TlBIWKxNAT3dhyAX32";
    private static final String DOMAIN = "http://agent69-image.test.upcdn.net/";
    private static final String FILE_PATH = "defaultAvatar";

    // 支持的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );


    @Autowired
    private DefaultAvatarMapper defaultAvatarMapper;

    @Override
    public String uploadImage(File file) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // 验证文件
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证是否为图片文件
        String fileName = file.getName().toLowerCase();
        boolean isImage = false;
        String extension = "";

        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (fileName.endsWith("." + allowedType)) {
                isImage = true;
                extension = allowedType;
                break;
            }
        }

        if (!isImage) {
            throw new IllegalArgumentException("不支持的文件类型，只允许上传图片文件: " + ALLOWED_IMAGE_TYPES);
        }

        // 生成唯一文件名 (UUID + 原始扩展名)
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String savePath = FILE_PATH + "/" + uniqueFileName;

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("save-key", savePath);
        FormUploader uploader = new FormUploader(BUCKET, OPERATOR, PASSWORD);
        Result result = uploader.upload(paramsMap, file);
        JSONObject msgJson = JSONObject.parseObject(result.getMsg());
        return msgJson.getString("url");
    }

    @Override
    public boolean deleteFile(String fileName) throws UpException, IOException {

        DefaultAvatar defaultAvatar = defaultAvatarMapper.selectByAvatarUrl(fileName);

        if (Objects.nonNull(defaultAvatar)) {
            return false;
        }

        UpYun upyun = new UpYun(BUCKET, OPERATOR, PASSWORD);
        String filePath = "/" + fileName;
        return upyun.deleteFile(filePath, null);
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }
}


















