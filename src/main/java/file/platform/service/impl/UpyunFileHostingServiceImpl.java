package file.platform.service.impl;

import file.platform.service.FileHostingService;
import okhttp3.*;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


@Service
public class UpyunFileHostingServiceImpl implements FileHostingService {

    // 又拍云配置
    private static final String BUCKET = "agent69-image";
    private static final String OPERATOR = "songyuhang";
    private static final String PASSWORD = "DCbg9f9MT2NrT9TlBIWKxNAT3dhyAX32";
    private static final String DOMAIN = "https://v0.api.upyun.com/";
    private static final String FILE_PATH = "/defaultAvatar";

    // 支持的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String uploadImage(File file) {
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

        try {
            // 生成时间戳
            String date = getGMTDate();

            // 计算文件大小
            long fileSize = file.length();

            // 构造请求路径
            String uri = BUCKET + FILE_PATH + "/" + uniqueFileName;
            String url =  DOMAIN + uri;

            // 计算密码MD5
            String passwordMd5 = md5(PASSWORD);

            // 计算签名
            // signature = md5(method + '&' + uri + '&' + date + '&' + content_length + '&' + md5(password))
            String signatureString = "PUT&" + uri + "&" + date + "&";

            // 对signatureString做HMAC-SHA1，密钥用passwordMd5
            String signature = hmacSha1(signatureString, passwordMd5);


            // 构造Authorization头
            String authorization = "UPYUN " + OPERATOR + ":" + md5(signature);

            System.out.println("上传参数:");
            System.out.println("原始文件名: " + fileName);
            System.out.println("唯一文件名: " + uniqueFileName);
            System.out.println("URL: " + url);
            System.out.println("Date: " + date);
            System.out.println("File Size: " + fileSize);
            System.out.println("Authorization: " + authorization);

            // 读取文件
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/" + extension));

            // 构造请求
            Request request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .addHeader("Authorization", authorization)
                    .addHeader("Date", date)
                    .addHeader("Content-Length", String.valueOf(fileSize))
                    .addHeader("Content-Type", "image/" + extension)
                    .build();

            // 发送请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    System.out.println("上传成功: " + responseBody);

                    // 返回完整的上传文件URL
                    return "https://" + BUCKET + ".b0.aicdn.com" + FILE_PATH + "/" + uniqueFileName;
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    System.err.println("上传失败: " + response.code() + " - " + errorBody);
                    throw new RuntimeException("图片上传失败: " + response.code() + " - " + errorBody);
                }
            }

        } catch (Exception e) {
            System.err.println("上传异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("图片上传异常", e);
        }
    }

    /**
     * 生成GMT格式的时间戳
     */
    private String getGMTDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    /**
     * MD5加密
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }

    /**
     * HMAC-SHA1签名
     */
    private String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA1");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes("UTF-8"));

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA1计算失败", e);
        }
    }

    /**
     * 从又拍云API响应中解析URL
     */
    private String parseUrlFromResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSONObject.parseObject(responseBody);

            // 检查是否有url字段
            if (jsonResponse.containsKey("url")) {
                return jsonResponse.getString("url");
            }

            // 如果没有url字段，构造默认URL
            return "https://" + BUCKET + ".b0.aicdn.com" + FILE_PATH;

        } catch (Exception e) {
            System.err.println("解析响应失败: " + e.getMessage());
            // 解析失败时返回默认URL
            return "https://" + BUCKET + ".b0.aicdn.com" + FILE_PATH;
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            // 生成时间戳
            String date = getGMTDate();

            // 构造请求路径 - fileName只包含文件名，不包含路径
            String url = DOMAIN + BUCKET + "/" + fileName;

            // 计算密码MD5
            String passwordMd5 = md5(PASSWORD);

            // 计算签名（DELETE方法）
            // signature = md5(method + '&' + uri + '&' + date + '&' + content_length + '&' + md5(password))
            String signatureString = "DELETE&" + BUCKET + "/" + fileName + "&" + date + "&0&" + passwordMd5;
            String signature = hmacSha1(signatureString, passwordMd5);

            // 构造Authorization头
            String authorization = "UPYUN " + OPERATOR + ":" + signature;

            System.out.println("删除参数:");
            System.out.println("文件名: " + fileName);
            System.out.println("完整路径: " + fileName);
            System.out.println("URL: " + url);
            System.out.println("Date: " + date);
            System.out.println("Authorization: " + authorization);

            // 构造DELETE请求
            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .addHeader("Authorization", authorization)
                    .addHeader("Date", date)
                    .addHeader("Content-Length", "0")
                    .build();

            // 发送请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("文件删除成功: " + fileName);
                    return true;
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    System.err.println("删除失败: " + response.code() + " - " + errorBody);
                    return false;
                }
            }

        } catch (Exception e) {
            System.err.println("删除异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
















