package com.github.cpfniliu.tool.binpic;

import com.github.sinjar.common.util.common.ArrUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <b>Description : </b> 解析二进制图片工具类
 *
 * @author CPF
 * Date: 2020/5/19 18:15
 */
@Slf4j
public class BinPicRecognizer {

    public static void convertBinPicToFile(String picPath, String saveDirPath) throws IOException {
        BinPicRecognizer recognizer = new BinPicRecognizer();
        recognizer.load(picPath);
        recognizer.distinguish();
        recognizer.pxReader.readFileInfo();
        boolean check = recognizer.pxReader.check();
        if (!check) {
            log.error("转换文件失败, MD5值不一样");
        }
        try (FileOutputStream outputStream = new FileOutputStream(new File(saveDirPath + recognizer.pxReader.getBinPicHeader().getFileName()))){
            outputStream.write(recognizer.pxReader.fileContent);
        }
    }

    public static void convertBinPicToFileFromSourcePath(String picPath) throws IOException {
        convertBinPicToFile(picPath, new File(picPath).getParentFile().getPath() + File.separator + DateFormatUtils.format(new Date(), "hh-mm-ss_"));
    }


    private BufferedImage image;

    /**
     * 左上方标记点
     */
    @Getter
    private Point leftTopPoint;
    /**
     * 右上方标记点
     */
    @Getter
    private Point rightTopPoint;
    /**
     * 左下方标记点
     */
    @Getter
    private Point leftBottomPoint;

    private PxReader pxReader;

    public void load(String picPath) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(picPath));
        image = ImageIO.read(is);
    }

    static class PxReader{
        private BufferedImage image;

        private int[] xArr;

        private int[] yArr;

        public PxReader(BufferedImage image, int[] xArr, int[] yArr) {
            this.image = image;
            this.xArr = xArr;
            this.yArr = yArr;
            init();
        }

        /**
         * 读取像素位置(用于计算下次读取位置)
         */
        int no;

        private byte[] fileContent;

        private int[] byteModal;

        @Getter
        private int radix;

        private int powOf2;

        @Getter
        private BinPicHeader binPicHeader;

        @Getter
        private int contentLength;

        public void init() {
            no = 0;
            int[] powOf2Bin = readPixel(8);
            int[] oneTwo = readPixel(2);
            no -= 2;
            powOf2 = deCode(oneTwo, powOf2Bin, 1);
            radix = (int) Math.pow(2, powOf2);
            byteModal = readPixel(radix);
        }

        public int deCode(int[] byteModal, int[] vals, int bit) {
            int val = 0;
            for (int i : vals) {
                int i1 = ArrUtils.indexOf(byteModal, i);
                if (i1 < 0) {
                    throw new RuntimeException();
                }
                val = val << bit | i1;
            }
            return val;
        }

        public byte[] deCodeToByte(int[] byteModal, int[] vals) {
            int max = byteModal.length;
            byte[] bytes = new byte[vals.length];
            for (int i = 0; i < vals.length; i++) {
                int v = ArrUtils.indexOf(byteModal, vals[i]);
                if (v < 0 || v >= max) {
                    throw new RuntimeException();
                }
                bytes[i] = (byte) v;
            }
            return BinPicUtils.deCodeToByte(powOf2, bytes);
        }

        private boolean check() {
            String md5Hex = DigestUtils.md5Hex(fileContent);
            String md5 = binPicHeader.getMd5();
            log.info("像素head信息MD5值: {}", md5);
            log.info("文件解析内容MD5值: {}", md5);
            return md5.equalsIgnoreCase(md5Hex);
        }

        /**
         * 读取文件信息
         */
        public void readFileInfo() {
            // 一行有多少像素
            int[] rowPixelCnt = readPixel(4 * 8 / powOf2);
            int rowPxNumLength = deCode(byteModal, rowPixelCnt, powOf2);
            Validate.isTrue(rowPxNumLength == xArr.length, String.format("rowPxNumLength : %S != xArr.length: %s", rowPxNumLength, xArr.length));
            // 文件信息长度
            int[] fileInfoLength = readPixel(4 * 8 / powOf2);
            contentLength = deCode(byteModal, fileInfoLength, powOf2);
            // 读取文件信息
            byte[] content = readByte(contentLength);
            // 文件头
            binPicHeader = BinPicHeader.fromJson(new String(content));
            log.info("文件头信息: {}", binPicHeader);
            // 文件内容
            fileContent = readByte(binPicHeader.getFileContentLength());
        }

        private byte[] readByte(long byteLength) {
            int[] ints = readPixel((int) (byteLength * 8 / powOf2));
            return deCodeToByte(byteModal, ints);
        }

        /**
         * @param number 读取 pixel 数目
         * @return 读取的像素值
         */
        @SuppressWarnings({"java:S1994", "java:S127"})
        public int[] readPixel(int number) {
            int xLength = xArr.length;
            int yLength = yArr.length;
            int[] arr = new int[number];
            int i = 0;
            for (int y = no / xLength, x = no % xLength; y < yLength && i < number; y++){
                for (; x < xLength && i < number; x++) {
                    int rgb = image.getRGB(xArr[x], yArr[y]);
                    arr[i] = rgb;
                    i ++;
                }
                x = 0;
            }
            no += number;
            return arr;
        }
    }

    /**
     * 识别
     */
    @SuppressWarnings({"java:S3776", "java:S1199", "java:S3518"})
    public void distinguish(){
        // 图片高度
        int height = image.getHeight();
        // 图片宽度
        int width = image.getWidth();

        {
            // 寻找四边
            // 斜线切入
            int min = Math.min(height, width);
            int r = 0;
            while (r < min) {
                int rgb = image.getRGB(r, r);
                if (isBorderVal(rgb)) {
                    break;
                }
                r++;
            }
            // 判断是否是横轴还是中轴
            if (isBorderVal(image.getRGB(r - 1, r))) {
                int x = r - 2;
                while (isBorderVal(image.getRGB(x, r))) x--;
                leftTopPoint = new Point(++ x, r);
            } else if (isBorderVal(image.getRGB(r, r-1))) {
                int y = r - 2;
                while (isBorderVal(image.getRGB(r, y))) y--;
                leftTopPoint = new Point(r, ++ y);
            } else {
                leftTopPoint = new Point(r, r);
            }
        }

        // 验证
        if (isBorderVal(image.getRGB(leftTopPoint.x - 1, leftTopPoint.y)) || isBorderVal(image.getRGB(leftTopPoint.x, leftTopPoint.y - 1))) {
            throw new RuntimeException("gfdsgdfs");
        }
        // 左上方标记点应为黑色
        Validate.isTrue(isBlack(image.getRGB(leftTopPoint.x, leftTopPoint.y)), "isNotBlack");

        // 找到xy 有效像素列表
        {
            boolean isBlack = true;
            int cnt = 0;
            int sum = 0;
            List<Integer> xList = new ArrayList<>();
            for (int x = leftTopPoint.x, y = leftTopPoint.y; x < width; x++) {
                int rgb = image.getRGB(x, y);
                if (!isBorderVal(rgb)) {
                    xList.add(sum / cnt);
                    rightTopPoint = new Point(x - 1, y);
                    break;
                }
                if ((isBlack && isBlack(rgb)) || (!isBlack && isWhite(rgb))) {
                    cnt ++;
                    sum += x;
                } else {
                    xList.add(sum / cnt);
                    cnt = 1;
                    sum = x;
                    isBlack = !isBlack;
                }
            }
            xList.remove(xList.size() - 1);
            xList.remove(0);
            int[] xArr = xList.stream().mapToInt(it -> it).toArray();
            xList.clear();
            isBlack = true;
            cnt = 0;
            sum = 0;
            for (int x = leftTopPoint.x, y = leftTopPoint.y; y < height; y++) {
                int rgb = image.getRGB(x, y);
                if (!isBorderVal(rgb)) {
                    xList.add(sum / cnt);
                    leftBottomPoint = new Point(x, y - 1);
                    break;
                }
                if ((isBlack && isBlack(rgb)) || (!isBlack && isWhite(rgb))) {
                    cnt ++;
                    sum += y;
                } else {
                    xList.add(sum / cnt);
                    cnt = 1;
                    sum = y;
                    isBlack = !isBlack;
                }
            }
            xList.remove(xList.size() - 1);
            xList.remove(0);
            int[] yArr = xList.stream().mapToInt(it -> it).toArray();

            pxReader = new PxReader(image, xArr, yArr);
        }
    }


    /**
     * 判断定位区像素是否是白色或黑色
     *
     * @param rgb rgb 值
     */
    public static boolean isBorderVal(int rgb) {
        return isBlack(rgb) || isWhite(rgb);
    }

    /**
     * 判断定位区像素是否是黑色
     *
     * @param rgb rgb 值
     */
    public static boolean isBlack(int rgb) {
        int n = 0;
        n += (rgb >>> 16 & 0xFF);
        n += (rgb >>> 8 & 0xFF);
        n += (rgb & 0xFF);
        return n < 100;
    }

    /**
     * 判断定位区像素是否是白色
     *
     * @param rgb rgb 值
     */
    public static boolean isWhite(int rgb) {
        int n = 0;
        n += (rgb >>> 16 & 0xFF);
        n += (rgb >>> 8 & 0xFF);
        n += (rgb & 0xFF);
        return n > 665;
    }

}