package com.swp.fileupload.util;

import cn.hutool.core.lang.Validator;
import org.apache.commons.codec.binary.Base64;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhouhl
 * @version $Revision: 102186 $, $Date: 2018-10-25 16:52:55 +0800 (Thu, 25 Oct 2018) $
 */
public class StringUtil {

    private static Pattern HTML_PATTERN = Pattern.compile("<([^>]*)>");

    /**
     * 将String类型的时间转变成Date
     *
     * @param dateTime
     *            格式为yyyy-MM-dd HH:mm:ss 的字符串
     * @return 返回格式为yyyy-MM-dd HH:mm:ss的Date
     */
    public static Date string2Date(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateTime);
            return date;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int compareString(String o1, String o2) {
        o1 = o1.toLowerCase();
        o2 = o2.toLowerCase();

        int size1 = o1.length();
        int size2 = o2.length();

        if (size1 < size2) {
            return -1;
        }
        else if (size1 > size2) {
            return 1;
        }
        else {
            for (int i = 0; i < size1; i++) {
                char ch1 = o1.charAt(i);
                char ch2 = o2.charAt(i);
                int value = ch1 - ch2;
                if (value == 0) {
                    continue;
                }
                else {
                    return value;
                }
            }
        }

        return 0;
    }

    public static String toBreakWord(String strContent, int length) {
        // 如果为空，则返回空字符串
        if (strContent == null) {
            return "";
        }
        // 如果长度不够，则直接返回
        if (strContent.length() <= length) {
            return strContent;
        }

        StringBuilder strTemp = new StringBuilder();
        while (strContent.length() > length) {
            strTemp.append(strContent.substring(0, length)).append(System.getProperty("line.separator"));
            strContent = strContent.substring(length, strContent.length());
        }
        strTemp.append(" ").append(strContent);
        return strTemp.toString();
    }

    /**
     * 获取当月的第一天
     *
     * @return
     */
    public static String getMonthFirstDay() {
        Calendar cal = Calendar.getInstance();
        Calendar f = (Calendar) cal.clone();
        f.clear();
        f.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        f.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        String firstday = new SimpleDateFormat("yyyy-MM-dd").format(f.getTime());

        return firstday;
    }

    /**
     * 获取当月的最后一天
     *
     * @return
     */
    public static String getMonthLastDay() {
        Calendar cal = Calendar.getInstance();
        Calendar l = (Calendar) cal.clone();
        l.clear();
        l.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        l.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        l.set(Calendar.MILLISECOND, -1);
        String lastday = new SimpleDateFormat("yyyy-MM-dd").format(l.getTime());

        return lastday;
    }

    /**
     * 指定字符串显示的长度，超出部分用...取代
     *
     * @return
     */
    public static String simplify(String str, int length) {
        if (Validator.isEmpty(str)) {
            return "";
        }
        if ((str.length() <= length) || (length <= 0)) {
            return str;
        }
        else {
            return str.substring(0, length) + "...";
        }

    }

    public static String getChinaDate() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年M月d日HH时mm分");
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTime(new Date());
        return sf.format(c.getTime());
    }

    public static String strNameHide(String strs) {
        if (Validator.isEmpty(strs)) {
            return strs;
        }
        String[] strArr = strs.split("<br />");
        StringBuilder sb = new StringBuilder();
        for (int m = 0; m < strArr.length; m++) {
            String str = strArr[m];
            if (str.endsWith("的家长")) {
                for (int i = 0; i < str.length() - 3; i++) {
                    if (i == 0) {
                        sb.append(str.charAt(i));
                        continue;
                    }
                    sb.append("*");
                }
                sb.append("的家长");
            }
            else {
                sb.append(str.charAt(0));
                for (int i = 1; i < str.length(); i++) {
                    sb.append("*");
                }
            }
            if (m != strArr.length - 1 && strArr.length > 1) {
                sb.append("<br />");
            }
        }
        sb.substring(0, sb.length() - 1);
        return sb.toString();
    }

    /**
     *
     * 基本功能：过滤所有以"<"开头以">"结尾的标签
     * <p>
     *
     * @param str
     * @return String
     */
    public static String filterHtml(String str) {
        if (Validator.isEmpty(str)) {
            return "";
        }

        Matcher matcher = HTML_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        while (result) {
            matcher.appendReplacement(sb, "");
            result = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 根据参数个数生成IN括弧里面的部分sql，包含括弧
     *
     * @param fromUserType
     *            参数个数
     * @return IN括弧里面的部分sql
     */
    public static String getFromUserTypesSQL(Integer[] fromUserType) {
        if (Validator.isEmpty(fromUserType)) {
            return "";
        }
        StringBuilder inSql = new StringBuilder();

        inSql.append("(");
        for (int i = 0; i < fromUserType.length; i++) {
            if (i == 0) {
                inSql.append(fromUserType[i]);
            }
            else {
                inSql.append("," + fromUserType[i]);
            }
        }
        inSql.append(")");

        return inSql.toString();
    }

    public static String mergeStrings(String... strs) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSize < 1024) {
            fileSizeString = df.format((double) fileSize) + "B";
        }
        else if (fileSize < 1048576) {
            fileSizeString = df.format((double) fileSize / 1024) + "K";
        }
        else if (fileSize < 1073741824) {
            fileSizeString = df.format((double) fileSize / 1048576) + "M";
        }
        else {
            fileSizeString = df.format((double) fileSize / 1073741824) + "G";
        }
        return fileSizeString;
    }


    /**
     * 获取单位为元的价格
     *
     * @param price
     *            单位为分的价格
     */

    /**
     * base64解密
     *
     * @param encode
     * @return
     * @author huwh
     */
    public static String decodeByBase64(String encode) {
        String decoded = null;
        try {
            byte[] clearText = Base64.decodeBase64(encode.getBytes());
            decoded = new String(clearText);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not decodeByBase64", e);
        }
        return decoded;
    }

    /**
     * base64加密
     *
     * @param str
     * @return
     */
    public static String encodeByBase64(String str) {
        String encoded = null;
        try {
            byte[] bytes = str.getBytes();
            encoded = new String(Base64.encodeBase64(bytes));
        }
        catch (Exception e) {
            throw new RuntimeException("Could not encodeByBase64", e);
        }
        return encoded;
    }

    public static int getRealLength(String str) {
        if (str == null) {
            return 0;
        }
        else {
            char separator = 256;
            int realLength = 0;

            for (int i = 0; i < str.length(); ++i) {
                if (str.charAt(i) >= separator) {
                    realLength += 2;
                }
                else {
                    ++realLength;
                }
            }

            return realLength;
        }
    }
}
