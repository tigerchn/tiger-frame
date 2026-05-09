package com.frame.tool.date;

import java.time.LocalDate;

/**
 * 日期混淆工具类
 * 1. 异或盐值 + 16进制混淆，可逆还原
 * 2. 固定长度补零，格式统一
 * 3. 带随机后缀不可逆版本，适合订单号/流水号
 */
public final class DateObscureUtil {

    // 自定义异或盐值，自行可修改，改后旧串无法还原
    private static final int XOR_SALT = 87654321;
    // 固定16进制长度，不足前置补0
    private static final int FIX_HEX_LENGTH = 8;

    private DateObscureUtil() {
        throw new AssertionError("禁止实例化");
    }

    // ===================== 可逆混淆（可还原日期）=====================

    /**
     * 当前日期 → 固定长度混淆16进制（可逆）
     */
    public static String nowToObscureHex() {
        return dateToObscureHex(LocalDate.now());
    }

    /**
     * 指定日期 → 固定长度混淆16进制（可逆）
     */
    public static String dateToObscureHex(LocalDate date) {
        int yyyyMMdd = date.getYear() * 10000
                + date.getMonthValue() * 100
                + date.getDayOfMonth();
        int obscure = yyyyMMdd ^ XOR_SALT;
        // 转16进制大写
        String hex = Integer.toHexString(obscure).toUpperCase();
        // 前置补0，固定长度
        return String.format("%" + FIX_HEX_LENGTH + "s", hex).replace(' ', '0');
    }

    /**
     * 混淆串 → 还原 LocalDate
     */
    public static LocalDate obscureHexToDate(String obscureHex) {
        // 去除前置补的0，再解析
        String hex = obscureHex.replaceFirst("^0+", "");
        int obscure = Integer.parseInt(hex, 16);
        int yyyyMMdd = obscure ^ XOR_SALT;

        int year = yyyyMMdd / 10000;
        int month = (yyyyMMdd % 10000) / 100;
        int day = yyyyMMdd % 100;

        return LocalDate.of(year, month, day);
    }
}