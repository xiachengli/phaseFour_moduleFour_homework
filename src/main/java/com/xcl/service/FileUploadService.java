package com.xcl.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.xcl.config.AliyunConfig;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.xcl.pojo.UploadResult;

import java.io.*;
import java.util.UUID;

@Service
public class FileUploadService {

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private OSSClient ossClient;

    // 允许上传的格式
    private static final String[] IMAGE_TYPE = new String[]{ ".jpg",
            ".jpeg",".png"};

    private static final Long FILE_LENGTH = 5 * 1024 * 1024L;

    public UploadResult  upload(MultipartFile multipartFile){
        // 校验图片格式
        boolean  isLegal = false;
        for (String type:IMAGE_TYPE){
            if(StringUtils.endsWithIgnoreCase(multipartFile.getOriginalFilename(),type)){
                isLegal = true;
                break;
            }
        }
        UploadResult upLoadResult = new UploadResult();
        if (!isLegal){
            upLoadResult.setStatus("error");
            return  upLoadResult;
        }

        //限制大小5M
        long size = multipartFile.getSize();
        if (FILE_LENGTH.compareTo(size) < 0) {
            upLoadResult.setStatus("大小超过5M");
            return upLoadResult;
        }
        String fileName = multipartFile.getOriginalFilename();
        String filePath = getFilePath(fileName);
        try {
            //上传
            ossClient.putObject(aliyunConfig.getBucketName(),filePath,new ByteArrayInputStream(multipartFile.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
            // 上传失败
            upLoadResult.setStatus("error");
            return  upLoadResult;
        }
        upLoadResult.setStatus("done");
        upLoadResult.setName(aliyunConfig.getUrlPrefix()+filePath);
        upLoadResult.setUid(filePath);
        return  upLoadResult;
    }
    // 生成不重复的文件路径和文件名
    private String getFilePath(String sourceFileName) {
        DateTime dateTime = new DateTime();
        return "images/" + dateTime.toString("yyyy")
                + "/" + dateTime.toString("MM") + "/"
                + dateTime.toString("dd") + "/" + UUID.randomUUID().toString() + "." +
                StringUtils.substringAfterLast(sourceFileName, ".");
    }

    /**
     * 下载
     * @param objectName
     * @return
     */
    public UploadResult download(OutputStream os, String objectName) throws IOException {
        UploadResult upLoadResult = new UploadResult();
        OSSObject result = ossClient.getObject(aliyunConfig.getBucketName(), objectName);

        BufferedInputStream in = new BufferedInputStream(result.getObjectContent());
        BufferedOutputStream out = new BufferedOutputStream(os);
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = in.read(buffer)) != -1) {
            out.write(buffer,0,len);
        }
        if (out != null) {
            out.flush();
            out.close();
        }
        if (in != null) {
            in.close();
        }

        upLoadResult.setStatus("done");
        upLoadResult.setName(aliyunConfig.getUrlPrefix()+result.getKey());
        upLoadResult.setUid(result.toString());
        return upLoadResult;
    }

    public UploadResult delete(String objectName) {
        // 根据BucketName,objectName删除文件
        ossClient.deleteObject(aliyunConfig.getBucketName(), objectName);
        UploadResult uploadResult = new UploadResult();
        uploadResult.setName(objectName);
        uploadResult.setStatus("removed");
        uploadResult.setResponse("success");

        return uploadResult;
    }
}
