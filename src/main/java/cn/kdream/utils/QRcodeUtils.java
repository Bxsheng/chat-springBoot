package cn.kdream.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-04
 * JDK 1.8
 */
public class QRcodeUtils {
    /**
     * 二维码绘制logo
     * @param twodimensioncodeImg 二维码图片文件
     * @param logoImg logo图片文件
     * */
    public static BufferedImage encodeImgLogo(File twodimensioncodeImg, File logoImg){
        BufferedImage twodimensioncode = null;
        try{
            if(!twodimensioncodeImg.isFile() || !logoImg.isFile()){
                System.out.println("输入非图片");
                return null;
            }
            //读取二维码图片
            twodimensioncode = ImageIO.read(twodimensioncodeImg);
            //获取画笔
            Graphics2D g = twodimensioncode.createGraphics();
            //读取logo图片
            BufferedImage logo = ImageIO.read(logoImg);
            //设置二维码大小，太大，会覆盖二维码，此处20%
            int logoWidth = logo.getWidth(null) > twodimensioncode.getWidth()*2 /10 ? (twodimensioncode.getWidth()*2 /10) : logo.getWidth(null);
            int logoHeight = logo.getHeight(null) > twodimensioncode.getHeight()*2 /10 ? (twodimensioncode.getHeight()*2 /10) : logo.getHeight(null);
            //设置logo图片放置位置
            //中心
            int x = (twodimensioncode.getWidth() - logoWidth) / 2;
            int y = (twodimensioncode.getHeight() - logoHeight) / 2;
            //右下角，15为调整值
//          int x = twodimensioncode.getWidth()  - logoWidth-15;
//          int y = twodimensioncode.getHeight() - logoHeight-15;
            //开始合并绘制图片
            g.drawImage(logo, x, y, logoWidth, logoHeight, null);
            g.drawRoundRect(x, y, logoWidth, logoHeight, 15 ,15);
            //logo边框大小
            g.setStroke(new BasicStroke(2));
            //logo边框颜色
            g.setColor(Color.WHITE);
            g.drawRect(x, y, logoWidth, logoHeight);
            g.dispose();
            logo.flush();
            twodimensioncode.flush();
        }catch(Exception e){
            System.out.println("二维码绘制logo失败");
        }
        return twodimensioncode;
    }

    /**
     * 二维码输出到文件
     * @param twodimensioncodeImg 二维码图片文件
     * @param logoImg logo图片文件
     * @param format 图片格式
     * @param file 输出文件
     * */
    public static void writeToFile(File twodimensioncodeImg,File logoImg,String format,File file) throws IOException {
        BufferedImage image = encodeImgLogo(twodimensioncodeImg, logoImg);
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) {
            System.out.println("二维码写入文件失败"+e.getMessage());
        }
    }
    /**
     * 二维码流式输出
     * @param twodimensioncodeImg 二维码图片文件
     * @param logoImg logo图片文件
     * @param format 图片格式
     * @param stream 输出流
     * */
    public static void writeToStream(File twodimensioncodeImg, File logoImg, String format, OutputStream stream){
        BufferedImage image = encodeImgLogo(twodimensioncodeImg, logoImg);
        try {
            ImageIO.write(image, format, stream);
        } catch (IOException e) {
            System.out.println("二维码写入流失败"+e.getMessage());
        }
    }
}
