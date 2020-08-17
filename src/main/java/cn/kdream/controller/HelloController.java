package cn.kdream.controller;


import cn.kdream.utils.MinioUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * @author 笨小昇
 */
@RestController
public class HelloController {


    @Autowired
    private MinioUtils minioUtils;
    @GetMapping("/hello")
    public String hello(HttpServletRequest request){

        return  "hello";
    }


}
