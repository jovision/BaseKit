package com.jovision.basekit;

import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ZHP on 2017/6/24.
 */
public class StringUtil extends BaseSingleton {

    /**
     * 实例化
     */
    public static StringUtil getInstance() {
        return getSingleton(StringUtil.class);
    }


    /**
     * 判断字符串是 空
     *
     * @param str null、“ ”、“null”都返回true
     * @return
     */
    public static boolean isNullString(String str) {
        return (null == str || str.trim().isEmpty() || "null".equals(str.trim()
                .toLowerCase(Locale.getDefault())));
    }

    /**
     * 判断字符串不是 空
     *
     * @param str null、“ ”、“null”都返回true
     * @return
     */
    public static boolean isNotNullString(String str) {
        return !isNullString(str);
    }

    public static String formatNullString(String str) {
        return isNotNullString(str) ? str : "";
    }

    /**
     * 特殊比较字符串 ""、null、"null"
     *
     * @param lhs
     * @param rhs
     * @return
     */
    public static boolean equalSpecialStr(String lhs, String rhs) {
        if (isNullString(lhs) && isNullString(rhs)) {
            return true;
        } else {
            if (lhs.equals(rhs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否有值.
     * <p>
     * 当value为null,空字符串,"null"字符串或只有空格时,则认为无值,返回true
     *
     * @param value
     * @return
     */
    public static boolean isEmpty(String value) {
        return isEmpty(value, false);
    }

    /**
     * 判断字符串是否有值.
     * <p>
     * 当value为null,空字符串或只有空格时,则认为无值,返回true
     * <p>
     * "null"字符串的判断,使用nullIsNormal设置
     *
     * @param value
     * @param nullIsNormal "null"字符串是否为正常值.true:"null"字符串属于正常值.false:"null"字符串属于空值
     * @return
     */
    public static boolean isEmpty(String value, boolean nullIsNormal) {
        if (value != null
                && !"".equalsIgnoreCase(value.trim())
                && (nullIsNormal || !"null".equalsIgnoreCase(value.trim()))) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断多个字符串是否相等，如果其中有一个为空字符串或者null，则返回false，只有全相等才返回true
     */
    public static boolean isEquals(String... agrs) {
        String last = null;
        for (int i = 0; i < agrs.length; i++) {
            String str = agrs[i];
            if (isEmpty(str)) {
                return false;
            }
            if (last != null && !str.equalsIgnoreCase(last)) {
                return false;
            }
            last = str;
        }
        return true;
    }

    /**
     * 截取数字
     *
     * @return
     */
    public static String cutNumber(String content) {
        if (isNotNullString(content)) {
            Pattern p = Pattern.compile("[^0-9]");
            Matcher m = p.matcher(content);
            return m.replaceAll("");
        } else {
            return "";
        }
    }

    /*
     *替换日期格式
     * 2016-05-13 --> 2016/05/13
     */
    public static String replaceDate(String Date) {
        return Date.replaceAll("-", "/");
    }


    /**
     * 时间转换
     * 900 --> 09:00
     * 2100 --> 21:00
     */
    public static String changeTime(String start_time) {
        String h;
        String m;
        if (start_time.length() < 4) {
            h = "0" + start_time.substring(0, 1);
            m = start_time.substring(1);
        } else {
            h = start_time.substring(0, 2);
            m = start_time.substring(2);
        }
        return h + ":" + m;
    }

    /**
     * 根据设备生成一个唯一标识
     *
     * @return
     */
    public static String generateOpenUDID() {
        // Try to get the ANDROID_ID
        String OpenUDID = Settings.Secure.getString(ApplicationKit.getInstance().getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (OpenUDID == null || OpenUDID.equals("9774d56d682e549c")
                | OpenUDID.length() < 15) {
            // if ANDROID_ID is null, or it's equals to the GalaxyTab generic
            // ANDROID_ID or bad, generates a new one
            final SecureRandom random = new SecureRandom();
            OpenUDID = new BigInteger(64, random).toString(16);
        }
        return OpenUDID;
    }

//    public static String getDecimal(Double d) {
//        DecimalFormat df = new DecimalFormat("0.00");
//        String result = df.format(d);
//        return result;
//
//    }


//    public static String getDecimal(BigDecimal d) {
//        DecimalFormat df = new DecimalFormat("0.00");
//        df.setRoundingMode(RoundingMode.FLOOR);//放弃四舍五入
//        String result = df.format(d);
//        return result;
//
//    }

    public static String getDecimalPrice(String price) {
        if (TextUtils.isEmpty(price)) {
            return "0";
        }
        if (TextUtils.equals(price, "1")) {
            return "0.01";
        }
        if (price.endsWith("00")) {
            return getDecimalZero(Double.valueOf(price) / 100);
        }
        return getDecimal(Double.valueOf(price) / 100);
    }

    public static String getDecimalZero(double d) {
        DecimalFormat df = new DecimalFormat("0");
        df.setRoundingMode(RoundingMode.FLOOR);//放弃四舍五入
        String result = df.format(d);
        return result;

    }

    public static String getDecimalTwo(double d) {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.FLOOR);//放弃四舍五入
        String result = df.format(d);
        return result;

    }

    public static String getDecimal(double d) {
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.FLOOR);//放弃四舍五入
        String result = df.format(d);
        return result;

    }

    public static String getPlayCount(int i) {
        BigDecimal d = new BigDecimal(i / 10000);
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.FLOOR);
        String result = df.format(d);
        return result;

    }

    /**
     * String 转 double 并保留x位小数
     *
     * @param x,d
     * @return
     */
    public static String getDecimalX(int x, double d) {
        String result;
        BigDecimal b = new BigDecimal(d);
        double f = b.setScale(x, BigDecimal.ROUND_HALF_UP).doubleValue();
        result = String.valueOf(f);
        return result;

    }

    public static String getDecimalMoney(String str) {
        if (StringUtil.isNullString(str)) {
            return null;
        }
        String result = null;
        if (Double.parseDouble(str) > 1) {
            DecimalFormat myformat = new DecimalFormat("###,###.00");
            result = myformat.format(new BigDecimal(str));
        } else {
            DecimalFormat myformat = new DecimalFormat("0.00");
            result = str.equals("0") ? "0" : myformat.format(new BigDecimal(str));
        }
        return result;

    }


    /**
     * 富文本
     *
     * @param str
     * @param start
     * @param end
     * @return
     */
    public static SpannableString getSpannableString(String str, int start, int end, int style_left, int style_right) {
        SpannableString styledText = new SpannableString(str);
        styledText.setSpan(new TextAppearanceSpan(ApplicationKit.getInstance().getContext(), style_left), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(ApplicationKit.getInstance().getContext(), style_right), end, str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return styledText;
    }

    public static SpannableString getSpannableString(String str, int start, int mide, int end, int style_left, int style_mid, int style_right) {
        SpannableString styledText = new SpannableString(str);
        styledText.setSpan(new TextAppearanceSpan(ApplicationKit.getInstance().getContext(), style_left), start, mide, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(ApplicationKit.getInstance().getContext(), style_mid), mide, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(ApplicationKit.getInstance().getContext(), style_right), end, str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return styledText;
    }

    /**
     * 加%号
     *
     * @param str
     * @return
     */
    public static String getNumberFormat(String str) {
        if (StringUtil.isNullString(str)) {
            return null;
        }
        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        percentInstance.setMinimumFractionDigits(2);
        return percentInstance.format((Double.valueOf(str)));
    }

    /**
     * 限制最多输入两位小数
     *
     * @param number
     * @return
     */
    public static boolean isOnlyPointNumber(String number) {//保留两位小数正则
        Pattern pattern = Pattern.compile("^\\d+\\.?\\d{0,2}$");
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    /**
     * 密码正则
     *
     * @param str
     * @return
     */
    public static boolean isSurePwd(String str) {//保留两位小数正则
        Pattern pattern = Pattern.compile("(?!^[0-9]{8,20}$)^[0-9A-Za-z\\u0021-\\u007e]{8,20}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 生成uuid
     *
     * @return
     */
    public static String getMyUUID() {

        UUID uuid = UUID.randomUUID();

        String uniqueId = uuid.toString();

        return uniqueId;

    }


/*// 支付宝ID
export const REG_ALIPAYID = /^\d{16}$/;
//身份证ID
export const REG_ID = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
//手机号
export const REG_PHONE = /^\d{8,}$/;
//校验手机号或者邮箱
export const REG_EMAIL_PHONE = /(^[\w.\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\.)+[a-z]{2,3}$)|(^\d{8,}$)/;
//登录密码
export const REG_LOGPWD=/(?!^[0-9]{6,20}$)^[0-9A-Za-z\u0021-\u007e]{6,20}$/;
//资金密码
export const REG_PAYPWD=/(?!^[0-9]{8,20}$)^[0-9A-Za-z\u0021-\u007e]{8,20}$/;
//校验护照账号
export const REG_PASSPORT = /^[a-zA-Z0-9]{3,50}$/;
*/


    public static String substringBefore(String s, String sub) {
        if (TextUtils.isEmpty(s) || TextUtils.isEmpty(sub)) {
            return "";
        }
        int idx = s.indexOf(sub);
        if (idx < 0) {
            return "";
        }
        return s.substring(0, idx);
    }

    public static String substringBetween(String s, String subA, String subB) {
        if (TextUtils.isEmpty(s) || TextUtils.isEmpty(subA) || TextUtils.isEmpty(subB)) {
            return "";
        }

        int idxA = s.indexOf(subA);
        if (idxA < 0) {
            return "";
        }
        int idxB = s.indexOf(subB);
        if (idxB < 0) {
            return "";
        }
        return s.substring(idxA + subA.length(), idxB);
    }

    public static String getSubtext(String text, String[] keys) {
        return getSubtext(text, Arrays.asList(keys));
    }

    public static String getSubtext(String text, List<String> keys) {
        if (keys.size() == 1) {
            return substringBefore(text, keys.get(0));
        } else if (keys.size() == 2) {
            return substringBetween(text, keys.get(0), keys.get(1));
        }
        return "";
    }

    public static boolean containsAll(String text, String[] keys) {
        return containsAll(text, Arrays.asList(keys));
    }

    public static boolean containsAll(String text, List<String> keys) {
        for (String key : keys) {
            if (!text.contains(key))
                return false;
        }
        return true;
    }

    public static boolean containsOnly(String text, List<String> keys) {
        for (String key : keys) {
            if (text.contains(key))
                return true;
        }
        return false;
    }

    public static boolean containsOnly(String text, String[] keys) {
        for (String key : keys) {
            if (text.contains(key))
                return true;
        }
        return false;
    }

    public static <T> boolean isEmptyList(List<T> list) {
        if (null == list) {
            return true;
        } else if (("").equals(list)) {
            return true;
        } else if (("null").equals((list + ("")).trim())) {
            return true;
        } else if ((list + "").trim().equals("[]")) {
            return true;
        } else {
            return false;
        }

    }

    //1 转化为 01
    public static String intFormat(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return i + "";
    }


    /**
     * 获取加密的手机号
     * @param originPhone
     * @return
     */
    public static String getMaskPhone(String originPhone){
        if(null == originPhone){
            return "";
        }
        return originPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 裁剪头部字符数.默认"null"字符串为空值
     *
     * @param source
     * @param size
     * @return
     */
    public static String subHeader(String source, int size) {
        return subHeader(source, size, false);
    }

    /**
     * 裁剪头部字符数
     *
     * @param source
     * @param size
     * @param nullIsNormal "null"字符串是否为正常值.
     *                     true:"null"字符串为正常值,正常截取;
     *                     false:"null"字符串为空值,返回空字符串;
     * @return
     */
    public static String subHeader(String source, int size, boolean nullIsNormal) {
        if (isEmpty(source, nullIsNormal)) {
            return "";
        }
        if (source.length() <= size) {
            return source;
        } else {
            return source.substring(0, size) + "...";
        }
    }

    //假方法，防止后续在改需求
    public static String subHeader1(String source , int size){
        return source;
    }


    public static String subStringLastList(List<String> datas){

        if(datas == null || datas.size() == 0){
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();
        for(String device : datas){
            stringBuffer.append(device);
            stringBuffer.append(",");
        }
        String value = stringBuffer.toString();
        if(value.length() > 0){
            value = value.substring(0,value.length()-1);
        }

        return value;
    }

    public static String subStringLastList2(List<String> datas){

        if(datas == null || datas.size() == 0){
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();
        for(String device : datas){
            stringBuffer.append(device);
            stringBuffer.append("、");
        }
        String value = stringBuffer.toString();
        if(value.length() > 0){
            value = value.substring(0,value.length()-1);
        }

        return value;
    }


}
