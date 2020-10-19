package com.dajiang.platform.utils;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

@Component
public class DataTranslate {

    /**
     * 将字节流转换成ASCII码
     * @param bytes 字节流
     * @return ASCII码
     */
    public static String getASCII(byte[] bytes) {
        String data = "";
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(bb);
        cb.array();
        for (int j = 0; j < cb.array().length; j++)
            data += cb.array()[j];
        return data;
    }

    /**
     * 将单个字节转换成十六进制数
     * @param b 单个字节
     * @return 十六进制数
     */
    public static String getHex(byte b) {
        String HexString = Integer.toString(b & 0xFF, 16);
        if (HexString.length() == 1)
            HexString = "0" + HexString;
        return HexString.toUpperCase();

    }

    /**
     * 将十六进制字符串转换为字节流
     * @param hexString 十六进制字符串
     * @return 字节流
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 将单个字符转换为字节流
     * @param c 单个字符
     * @return 字节流
     */
    private static int charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将字节流转换成十六进制字符串
     * @param bytes 字节流
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 将字节流转换成二进制字符串
     * @param bArray 字节流
     * @return 二进制字符串
     */
    public static String bytes2BinaryStr(byte[] bArray) {
        String[] binaryArray =
                {"0000", "0001", "0010", "0011",
                        "0100", "0101", "0110", "0111",
                        "1000", "1001", "1010", "1011",
                        "1100", "1101", "1110", "1111"};
        String outStr = "";
        int pos = 0;
        for (byte b : bArray) {
            //高四位
            pos = (b & 0xF0) >> 4;
            outStr += binaryArray[pos];
            //低四位
            pos = b & 0x0F;
            outStr += binaryArray[pos];
        }
        return outStr;

    }

    /**
     * 将字符串转换成字节流
     * @param chars 字符串
     * @return 字节流
     */
    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    /**
     * 将字节流转换成十进制数
     * @param intByte 字节流
     * @return 十进制数
     */
    public static int bytesToInt(byte[] intByte) {
        int fromByte = 0;

        for (int i = 0; i < 2; i++) {
            int n = (intByte[i] < 0 ? (int) intByte[i] + 256 : (int) intByte[i]) << (8 * i);
            System.out.println(n);
            fromByte += n;
        }
        return fromByte;
        /*//改成16进制转10进制float
        float abaaba = 0;

        int intBits = Integer.parseInt(s, 16);
        float f = Float.intBitsToFloat(intBits);

        return  abaaba;*/
    }

}
