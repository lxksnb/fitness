package com.fitness.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 多格式日期反序列化器
 * <p>
 * 依次尝试多种日期格式解析前端传来的日期字符串：
 * 1. yyyy-MM-dd HH:mm:ss (完整日期时间)
 * 2. yyyy-MM-dd (纯日期)
 * 3. yyyy-MM-dd'T'HH:mm:ss (ISO格式)
 * <p>
 * 只要匹配任意一种格式即返回结果；全都不匹配则抛出异常。
 */
public class MultiDateFormatDeserializer extends JsonDeserializer<Date> {

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy-MM-dd'T'HH:mm:ss"
    };

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateStr = parser.getText().trim();
        if (dateStr.isEmpty()) {
            return null;
        }

        // 依次尝试每种格式解析
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false); // 严格模式，防止"2026-05-20 12:00"被"yyyy-MM-dd"误匹配
                ParsePosition pos = new ParsePosition(0);
                Date date = sdf.parse(dateStr, pos);
                // 必须完整消费整个字符串才算匹配成功
                if (date != null && pos.getIndex() == dateStr.length()) {
                    return date;
                }
            } catch (Exception ignored) {
                // 当前格式不匹配，尝试下一种
            }
        }

        throw new IOException("无法解析日期字符串: \"" + dateStr
                + "\", 支持的格式: yyyy-MM-dd, yyyy-MM-dd HH:mm:ss");
    }
}
