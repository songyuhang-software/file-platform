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


@Service
public class UpyunFileHostingServiceImpl implements FileHostingService {

    // 又拍云配置
    private static final String BUCKET = "agent69-image";
    private static final String OPERATOR = "songyuhang";
    private static final String PASSWORD = "DCbg9f9MT2NrT9TlBIWKxNAT3dhyAX32";
    private static final String DOMAIN = "v0.api.upyun.com";
    private static final String FILE_PATH = "/defaultAvatar";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String uploadFile(File file) {
        try {
            // 生成时间戳
            String date = getGMTDate();

            // 计算文件大小
            long fileSize = file.length();

            // 构造请求路径
            String uri = "/" + BUCKET + FILE_PATH;
            String url = "https://" + DOMAIN + uri;

            // 计算密码MD5
            String passwordMd5 = md5(PASSWORD);

            // 计算签名
            // signature = md5(method + '&' + uri + '&' + date + '&' + content_length + '&' + md5(password))
            String signatureString = "PUT&" + uri + "&" + date + "&" + fileSize + "&" + passwordMd5;
            String signature = md5(signatureString);

            // 构造Authorization头
            String authorization = "UPYUN " + OPERATOR + ":" + signature;

            System.out.println("上传参数:");
            System.out.println("URL: " + url);
            System.out.println("Date: " + date);
            System.out.println("File Size: " + fileSize);
            System.out.println("Authorization: " + authorization);

            // 读取文件
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));

            // 构造请求
            Request request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .addHeader("Authorization", authorization)
                    .addHeader("Date", date)
                    .addHeader("Content-Length", String.valueOf(fileSize))
                    .addHeader("Content-Type", "application/octet-stream")
                    .build();

            // 发送请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    System.out.println("上传成功: " + responseBody);

                    // 从响应中解析URL
                    String fileUrl = parseUrlFromResponse(responseBody);
                    return fileUrl;
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    System.err.println("上传失败: " + response.code() + " - " + errorBody);
                    throw new RuntimeException("文件上传失败: " + response.code() + " - " + errorBody);
                }
            }

        } catch (Exception e) {
            System.err.println("上传异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件上传异常", e);
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
}







