package com.tcc.rj_system.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.tcc.rj_system.enums.StatusProcesso;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new StatusProcessoReadConverter(),
                new StatusProcessoWriteConverter()));
    }

    @ReadingConverter
    private static class StatusProcessoReadConverter implements Converter<String, StatusProcesso> {

        @Override
        public StatusProcesso convert(String source) {
            return StatusProcesso.from(source);
        }
    }

    @WritingConverter
    private static class StatusProcessoWriteConverter implements Converter<StatusProcesso, String> {

        @Override
        public String convert(StatusProcesso source) {
            return source.name();
        }
    }
}
