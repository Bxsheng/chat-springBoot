package cn.kdream.chat;

import cn.kdream.utils.EncodeImgZxing;
import cn.kdream.utils.MinioUtils;
import cn.kdream.utils.QRcodeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
class ChatApplicationTests {

    @Autowired
    private MinioUtils minioUtils;

    @Test
    void contextLoads() throws IOException {

//        File file = new File("E:\\Desktop\\logo.jpg");
//
//        FileInputStream inputStream = new FileInputStream(file);
//
//        MultipartFile multipartFile = new MockMultipartFile("file",file.getName()
//        ,"text/plain", inputStream);
//
//        minioUtils.uploadImage(multipartFile);

        System.out.println(minioUtils.uploadQRcode("李胜昇"));

        return;
    }

    /**
     * 生成二维码图片文件
     * @throws IOException
     */
    private void setLogoImage() throws IOException {
        String content = "http://baidu.com";
        String format =  "jpg";
        //生成二维码
        File img = new File("E:\\Desktop\\logo.jpg");
        EncodeImgZxing.writeToFile(content,format,img);

        //添加logo
        File imgLogo = new File("E:\\Desktop\\beanFatory.png");
        File imgAndLogo = new File("E:\\Desktop\\logo1.jpg");
        QRcodeUtils.writeToFile(img,imgLogo,format,imgAndLogo);
    }

}
