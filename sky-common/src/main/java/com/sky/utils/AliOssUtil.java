package com.sky.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String region;
    private String bucketName;


    /**
     * 文件上传
     * @param file
     * @return
     * @throws IOException
     * @throws com.aliyuncs.exceptions.ClientException
     */
    public String upload(MultipartFile file) throws IOException, com.aliyuncs.exceptions.ClientException {
        //获取环境中的阿里云密钥
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        //创建OSS对象
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();
        //获得输入流
        InputStream inputStream = file.getInputStream();
        //OSS文件名
        String originalFilename = file.getOriginalFilename();
        String objectName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        //上传
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
        PutObjectResult result = ossClient.putObject(putObjectRequest);
        //拼接URL
        String URL = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
        //关闭ossClient
        ossClient.shutdown();
        //返回URL
        return URL;
    }
}
