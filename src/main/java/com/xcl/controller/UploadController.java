package com.xcl.controller;

import com.xcl.pojo.UploadResult;
import com.xcl.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("oss")
public class UploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public UploadResult upload(@RequestParam("file") MultipartFile multipartFile){
        return  fileUploadService.upload(multipartFile);
    }

    @GetMapping("/download")
    public UploadResult download(HttpServletResponse response, @RequestParam("objectName") String objectName){
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(objectName.getBytes(), "ISO-8859-1"));
            return  fileUploadService.download(response.getOutputStream(),objectName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/delete")
    public UploadResult delete(String objectName){
        return  fileUploadService.delete(objectName);
    }
}
