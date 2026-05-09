package com.frame.tool.storage.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.frame.tool.date.DateObscureUtil;
import com.frame.tool.storage.service.StorageService;
import com.frame.tool.storage.properties.oss.OssProperties;
import com.frame.tool.uuid.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class OssStorageService implements StorageService {

    private final OssProperties properties;
    private final OSS ossClient;

    // ====================== 上传 MultipartFile ======================
    public String upload(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = generateFilePath(file.getOriginalFilename());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());

            ossClient.putObject(properties.getBucketName(), fileName, inputStream, metadata);
            return getFileUrl(fileName);
        } catch (Exception e) {
            log.error("OSS文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    // ====================== 流式上传 ======================
    public String upload(InputStream inputStream, String originalFilename) {
        try {
            String fileName = generateFilePath(originalFilename);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(originalFilename));

            ossClient.putObject(properties.getBucketName(), fileName, inputStream, metadata);
            return getFileUrl(fileName);
        } catch (Exception e) {
            log.error("OSS流式上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    // ====================== 下载 ======================
    public InputStream download(String objectName) {
        try {
            objectName = trimLeadingSlash(objectName);
            OSSObject ossObject = ossClient.getObject(properties.getBucketName(), objectName);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("OSS文件下载失败", e);
            throw new RuntimeException("文件下载失败");
        }
    }

    // ====================== 删除 ======================
    public void delete(String objectName) {
        try {
            objectName = trimLeadingSlash(objectName);
            ossClient.deleteObject(properties.getBucketName(), objectName);
            log.info("OSS文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("OSS文件删除失败", e);
            throw new RuntimeException("文件删除失败");
        }
    }

    // ====================== 文件列表 ======================
    public List<String> listObjects(String prefix) {
        ListObjectsRequest request = new ListObjectsRequest(properties.getBucketName());
        request.setPrefix(prefix);
        ObjectListing listing = ossClient.listObjects(request);
        return listing.getObjectSummaries().stream()
                .map(OSSObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    // ====================== 私有文件临时URL ======================
    public String getTempUrl(String objectName, long expireSeconds) {
        Date expire = new Date(System.currentTimeMillis() + expireSeconds * 1000);
        URL url = ossClient.generatePresignedUrl(properties.getBucketName(), objectName, expire);
        return url.toString();
    }

    // ====================== 工具方法 ======================
    private String generateFilePath(String originalFilename) {
        String uuid = IdUtil.simpleId();
        String ext = getFileExtension(originalFilename);
        String datePath = DateObscureUtil.nowToObscureHex();
        return datePath + "/" + uuid + ext;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String getFileUrl(String filePath) {
        return "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/" + filePath;
    }

    private String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String getContentType(String fileName) {
        return org.springframework.http.MediaTypeFactory.getMediaType(fileName)
                .orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .toString();
    }
}