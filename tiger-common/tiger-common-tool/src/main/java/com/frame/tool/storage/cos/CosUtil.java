package com.frame.tool.storage.cos;

import com.frame.tool.storage.cos.properties.CosProperties;
import com.frame.tool.uuid.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CosUtil {

    private final CosProperties properties;
    private final COSClient cosClient;

    // ====================== 上传 MultipartFile ======================
    public String upload(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = generateFilePath(file.getOriginalFilename());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());

            cosClient.putObject(properties.getBucketName(), fileName, inputStream, metadata);
            return getFileUrl(fileName);
        } catch (Exception e) {
            log.error("COS文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    // ====================== 流式上传 ======================
    public String upload(InputStream inputStream, String originalFilename) {
        try {
            String fileName = generateFilePath(originalFilename);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(originalFilename));

            cosClient.putObject(properties.getBucketName(), fileName, inputStream, metadata);
            return getFileUrl(fileName);
        } catch (Exception e) {
            log.error("COS流式上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    // ====================== 下载 ======================
    public InputStream download(String objectName) {
        try {
            objectName = trimLeadingSlash(objectName);
            COSObject cosObject = cosClient.getObject(properties.getBucketName(), objectName);
            return cosObject.getObjectContent();
        } catch (Exception e) {
            log.error("COS文件下载失败", e);
            throw new RuntimeException("文件下载失败");
        }
    }

    // ====================== 删除 ======================
    public void delete(String objectName) {
        try {
            objectName = trimLeadingSlash(objectName);
            cosClient.deleteObject(properties.getBucketName(), objectName);
            log.info("COS文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("COS文件删除失败", e);
            throw new RuntimeException("文件删除失败");
        }
    }

    // ====================== 文件列表 ======================
    public List<String> listObjects(String prefix) {
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(properties.getBucketName());
        request.setPrefix(prefix);
        ObjectListing listing = cosClient.listObjects(request);
        return listing.getObjectSummaries().stream()
                .map(COSObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    // ====================== 私有文件临时URL ======================
    public String getTempUrl(String objectName, long expireSeconds) {
        Date expire = new Date(System.currentTimeMillis() + expireSeconds * 1000);
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(
                properties.getBucketName(), objectName);
        presignedUrlRequest.setExpiration(expire);
        URL url = cosClient.generatePresignedUrl(presignedUrlRequest);
        return url.toString();
    }

    // ====================== 工具方法 ======================
    private String generateFilePath(String originalFilename) {
        String uuid = IdUtil.simpleId();
        String ext = getFileExtension(originalFilename);
        String datePath = java.time.LocalDate.now().toString();
        return datePath + "/" + uuid + ext;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String getFileUrl(String filePath) {
        return "https://" + properties.getBucketName() + ".cos." + properties.getRegion() + ".myqcloud.com/" + filePath;
    }

    private String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String getContentType(String fileName) {
        return MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)
                .toString();
    }

}
