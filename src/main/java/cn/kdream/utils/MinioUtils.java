package cn.kdream.utils;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-03
 * JDK 1.8
 */
@Configuration
public class MinioUtils {


    @Autowired
    private MinioProperties minioData;

    private  MinioClient  minioClient;

    @Bean
    public  void initMinioClient(){
        //初始化链接
        try {
            minioClient = new MinioClient(minioData.getUrl(),minioData.getPort(),minioData.getAccessKey(),
                    minioData.getSecretKey(),minioData.getSecure());
            //检查桶是否存在
            boolean isExist = minioClient.bucketExists(minioData.getBucketName());
            if (isExist) {
                System.err.println("Minio文件存储链接成功");
            }
        } catch (MinioException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 单独上传一张图片信息
     * @param file MultipartFile
     * @return
     */
    public String uploadImage(MultipartFile file){
        //判断文件大小
        if (file.getSize() > minioData.getFileSize()){
            return null;
        }
        String fileNameOld = file.getOriginalFilename();
        String fileName =this.getPrefixName()+this.getImageName()+fileNameOld.substring(fileNameOld.indexOf("."),fileNameOld.length()) ;
        System.out.println(fileName);
        try {
            minioClient.putObject(minioData.getBucketName(),fileName,file.getInputStream()
            ,file.getSize(),file.getContentType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return minioData.getUrl()+":"+minioData.getPort()+"/"+minioData.getBucketName()+"/"+fileName;
    }

    /**
     * 上传两张图片其中一张为缩略图
     * @param file MultipartFile
     * @return 返回map url 为原图 urlThumb 缩略图
     */
   public Map<String,String> uploadImageThumb(MultipartFile file){
        String imageUrl = this.uploadImage(file);
        String imageUrlThumb = this.getTempThumb(file);
        Map<String,String> imageMap = new HashMap<>(2);
        imageMap.put("url",imageUrl);
        imageMap.put("urlThumb",imageUrlThumb);
        return imageMap;

   }

    /**
     * 移除一个文件信息
     * @param imageName
     */
   public void removeImage(String imageName){
       try {
           minioClient.removeObject(minioData.getBucketName(),imageName);
       } catch (Exception e) {
           System.err.println("移除文件出错");
           e.printStackTrace();
       }
   }

    /**
     * 移除多个文件
     * @param images
     */
   public void removeImages(List<String> images){
       try {
           // 删除my-bucketname里的多个对象
           for (Result<DeleteError> errorResult: minioClient.removeObject(minioData.getBucketName(), images)) {
               DeleteError error = errorResult.get();
               System.err.println("Failed to remove '" + error.objectName() + "'. Error:" + error.message());
           }
       } catch (Exception e) {
           System.out.println("Error: " + e);
       }
   }








    /**
     * 获取临时文件信息
     * @param file
     * @return
     */
    private String getTempThumb(MultipartFile file) {
        //获取创建的临时文件信息
        File thumb = null;
        String fileName =null;
        try {
            thumb = this.getTempThumb();
        } catch (IOException e) {
            System.err.println("临时文件创建失败" + e.getMessage());
            e.printStackTrace();
        }
        try {
            //输出到临时文件
            Thumbnails.of(file.getInputStream()).forceSize(60, 60)
                    .toFile(thumb);
            //获取临时文件名 前缀 + 文件名
            fileName= this.getPrefixName() + thumb.getName();
            System.out.println(fileName);
            minioClient.putObject(minioData.getBucketName(), fileName, thumb.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //删除临时文件
            thumb.delete();
        }
        return minioData.getUrl()+":"+minioData.getPort()+"/"+minioData.getBucketName()+"/"+fileName;
    }

    /**
     * 随机生成25位图片名
     * @return
     */
    private String getImageName() {
        String base = "0123456789ABCDEFGHIJKLMNOPQRSTabcdefghijklmnopqrst";
        int size = base.length();
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 25; i++) {
            //产生0到size-1的随机值
            int index = r.nextInt(size);
            //在base字符串中获取下标为index的字符
            char c = base.charAt(index);
            //将c放入到StringBuffer中去
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 创建临时图片文件
     * @return
     * @throws IOException
     */
    private File getTempThumb () throws IOException {
        String path = getImageName();
        File thumb = File.createTempFile(path,".jpg");
        thumb.renameTo(new File(thumb.getPath()+path + ".jpg"));
        return thumb;

    }

    /**
     * 判断是否有前缀
     * @return
     */
    private String getPrefixName(){
        if (minioData.getPrefixName() == null){
            return "";
        }else{
            return minioData.getPrefixName()+"/";
        }
    }


    /**
     * 生成二维码图片并进行上传
     * @param content
     * @throws IOException
     */
    public String uploadQRcode(String content) throws IOException {
        //获取临时文件
        File imageLogoTemp = this.getTempThumb();
        //获取路径信息
        File imageLogo = new File(imageLogoTemp.getPath());
        System.out.println(imageLogo.getPath());
        String format =  "jpg";
        //生成二维码
        EncodeImgZxing.writeToFile(content,format,imageLogo);
        //进行图片上传
        try {
            minioClient.putObject(minioData.getBucketName(),this.getPrefixName()+imageLogo.getName(),
                   imageLogo.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            imageLogoTemp.delete();
        }

        return this.getFileUrl()+imageLogo.getName();
    }

    /**
     * 获取拼接路径信息
     * @return
     */
    private String getFileUrl(){
        if (minioData.getPrefixName() != null){
            return  minioData.getUrl()+":"+minioData.getPort()+"/"+minioData.getBucketName()
                    +"/"+minioData.getPrefixName()+"/";
        }else {
            return  minioData.getUrl()+":"+minioData.getPort()+"/"+minioData.getBucketName()
                    +"/";
        }
    }
}
