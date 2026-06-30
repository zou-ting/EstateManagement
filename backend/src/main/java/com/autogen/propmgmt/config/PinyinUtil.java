package com.autogen.propmgmt.config;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

/*
 * @author zouting
 * @Date 2026-06-25-16:31
 *
 */
public class PinyinUtil {
    /**
     * 将中文转换为拼音（全小写，无音调）
     */
    public static String toPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuilder result = new StringBuilder();
        char[] chars = chinese.toCharArray();

        for (char c : chars) {
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyins != null && pinyins.length > 0) {
                    result.append(pinyins[0]); // 取第一个拼音
                } else {
                    result.append(c); // 非中文字符保留
                }
            } catch (Exception e) {
                result.append(c);
            }
        }
        return result.toString().toLowerCase();
    }
}

