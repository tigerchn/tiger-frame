package com.frame.tool.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface StorageService {

    String upload(MultipartFile file);

    String upload(InputStream inputStream, String originalFilename);

    InputStream download(String objectName);

    void delete(String objectName);

    List<String> listObjects(String prefix);

    String getTempUrl(String objectName, long expireSeconds);

}
