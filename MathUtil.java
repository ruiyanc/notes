package com.zchd.qianzi.common.util;

import cn.hutool.core.util.RandomUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class MathUtil {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    public static final String DICTIONARY = "a39mgx15sh0qjvkunbrwcp8y674t2dezf";
    public static final Integer RANDOM_REVERSED_KEY = 39355393;
    public static final Integer RANDOM_COMMON_KEY = 52162709;

    // 正则表达式验证身份证号的格式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{6}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])\\d{3}([0-9]|X|x)$");

    // 校验码计算权重
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    // 校验码对应的值
    private static final String[] VALIDITY_CHECK_CODE = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};

    /**
     * 校验身份证号是否合法，包括格式、日期和校验码验证。
     * @param idCard 身份证号
     * @return true 如果身份证号合法，否则false
     */
    public static boolean isValidIdCard(String idCard) {
        // 步骤1：正则表达式验证格式
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }

        // 步骤2：日期有效性验证
        String birthStr = idCard.substring(6, 14);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            // 如果格式错误，这里会抛出异常
            LocalDate localDate = LocalDate.parse(birthStr, formatter);
            if (localDate.isAfter(LocalDate.now())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // 步骤3：校验码验证
        // 计算身份证前17位的校验码
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Integer.parseInt(String.valueOf(idCard.charAt(i))) * WEIGHTS[i];
        }
        int mod = sum % 11;
        String checkCode = VALIDITY_CHECK_CODE[mod];

        // 比较计算的校验码与身份证最后一位（不区分大小写）
        return checkCode.equalsIgnoreCase(String.valueOf(idCard.charAt(17)));
    }

    /**
     * 三数相加 若为null则赋值0
     */
    public static Integer sumThreeNumber(Integer a, Integer b, Integer c) {
        int a1 = a == null ? 0 : a;
        int b1 = b == null ? 0 : b;
        int c1 = c == null ? 0 : c;
        return a1 + b1 + c1;
    }

    /**
     * 三数相加 若为null则赋值0
     */
    public static Long sumThreeNumber(Long a, Long b, Long c) {
        long a1 = a == null ? 0 : a;
        long b1 = b == null ? 0 : b;
        long c1 = c == null ? 0 : c;
        return a1 + b1 + c1;
    }

    /**
     * 生成随机code码
     * @param id 基数
     */
    public static String generateInviteCode(Long id) {
        if(id < 1 || id > 799999999){
            throw new RuntimeException("OUT OF RANGE");
        }
        long l = id + RANDOM_REVERSED_KEY + Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(2, 9));
        return decimalConversion(Long.parseLong(new StringBuilder(String.valueOf(l)).reverse().toString()) + RANDOM_COMMON_KEY);
    }

    private static String decimalConversion(Long number) {
        Long dividend = number;
        int moreThan;
        StringBuilder returns = new StringBuilder();
        int decimal = DICTIONARY.length();
        while (dividend > 0){
            moreThan = (int) (dividend % decimal);
            returns.insert(0, DICTIONARY.charAt(moreThan));
            dividend /= decimal;
        }
        return returns.toString();
    }

    public static String generateRandomString() {
        StringBuilder sb = new StringBuilder();

        // 生成四位小写字母
        for (int i = 0; i < 4; i++) {
            char randomChar = (char) (RANDOM.nextInt(26) + 'a');
            sb.append(randomChar);
        }

        // 生成四位随机数
        for (int i = 0; i < 4; i++) {
            int randomNumber = RANDOM.nextInt(10);
            sb.append(randomNumber);
        }

        return sb.toString();
    }

    /**
     * 生成订单号 当前时间+6位随机数
     */
    public static String generateOrderSn(){
        String date = DateUtil.formatTime(LocalDateTime.now(), DateUtil.DATEFORMAT_HMS);
        int orderNum = RandomUtil.randomInt(100000, 1000000);
        return date + orderNum;
    }

    /**
     * 校验手机号
     * @param mobile
     */
    public static void isValidMobile(String mobile) {
        if(mobile.length() < 11 || !Pattern.matches("^1[3-9]\\d{9}$", mobile)){
            throw new RuntimeException("手机号不正确 " + mobile);
        }
    }

    /**
     * 校验String为价格格式
     */
    public static BigDecimal validateAndConvertPrice(String tempPrice) throws IllegalArgumentException {
        // 校验规则的正则表达式：正数，最多两位小数
        String regex = "^(\\d+(\\.\\d{1,2})?)$";

        if (tempPrice == null || tempPrice.trim().isEmpty()) {
            throw new IllegalArgumentException("价格字段不能为空");
        }

        if (!tempPrice.matches(regex)) {
            throw new IllegalArgumentException("价格字段有误，请输入数字、最多保留2位小数、不可为负数、不可输入特殊符号");
        }

        try {
            // 转换为 BigDecimal
            BigDecimal price = new BigDecimal(tempPrice);

            // 检查是否为负数
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("价格不可为负数");
            }

            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("价格字段转换失败，请输入有效数字");
        }
    }

    /**
     * 基本校验
     */
    public static boolean isCheckValidIdCardV3(String idCard) {
        // 步骤1：正则表达式验证格式
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }

        // 步骤2：日期有效性验证
        String birthStr = idCard.substring(6, 14);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            // 如果格式错误，这里会抛出异常
            LocalDate localDate = LocalDate.parse(birthStr, formatter);
            if (localDate.isAfter(LocalDate.now())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 计算加价后的金额
     * @param price 原价
     * @param markupRatio 加价比例 单位%
     */
    public static BigDecimal getMarkupPrice(BigDecimal price, int markupRatio) {
        BigDecimal multiple = new BigDecimal(markupRatio).divide(new BigDecimal(100));
        return price.multiply(new BigDecimal("1.0").add(multiple));
    }

    /**
     * 打乱现有的所有人，再相互评价，但不能评价自己
     */
    public static Map<String, String> assignReviewers(List<String> people) {
        Map<String, String> assignments = new LinkedHashMap<>();

        if (people.size() == 1) {
            // 特殊处理：只有一个人，只能评价自己
            assignments.put(people.get(0), people.get(0));
            return assignments;
        }

        List<String> reviewers = new ArrayList<>(people);
        List<String> reviewees;

        while (true) {
            reviewees = new ArrayList<>(people);
            Collections.shuffle(reviewees, RANDOM);

            boolean valid = true;
            for (int i = 0; i < reviewers.size(); i++) {
                if (reviewers.get(i).equals(reviewees.get(i))) {
                    valid = false;
                    break;
                }
            }

            if (valid) break;
        }

        for (int i = 0; i < reviewers.size(); i++) {
            assignments.put(reviewers.get(i), reviewees.get(i));
        }

        return assignments;
    }
}
