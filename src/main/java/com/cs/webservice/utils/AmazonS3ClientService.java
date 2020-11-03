package com.cs.webservice.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface AmazonS3ClientService
{
    void uploadFileToS3Bucket(MultipartFile multipartFile, String fileName, boolean enablePublicReadAccess) throws IOException;

    void deleteFileFromS3Bucket(String fileName);
}
