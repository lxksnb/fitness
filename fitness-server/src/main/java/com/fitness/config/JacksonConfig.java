package com.fitness.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.TimeZone;

/**
 * Jackson 全局配置
 * <p>
 * 解决前后端日期格式不一致问题：
 * 前端可能传 "yyyy-MM-dd"（纯日期）或 "yyyy-MM-dd HH:mm:ss"（日期时间），
 * 后端通过多格式反序列化器统一兼容解析。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 设置时区
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            // 忽略未知字段
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            // 注册多格式日期反序列化器
            SimpleModule dateModule = new SimpleModule();
            dateModule.addDeserializer(Date.class, new MultiDateFormatDeserializer());
            // Long序列化为String，防止JS精度丢失
            dateModule.addSerializer(Long.class, ToStringSerializer.instance);
            dateModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modules(dateModule);
        };
    }
}
