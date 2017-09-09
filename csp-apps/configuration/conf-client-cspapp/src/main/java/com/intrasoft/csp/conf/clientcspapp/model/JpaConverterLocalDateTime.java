package com.intrasoft.csp.conf.clientcspapp.model;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by tangelatos on 09/09/2017.
 */
@Converter
public class JpaConverterLocalDateTime implements AttributeConverter<LocalDateTime, String> {

    static final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public String convertToDatabaseColumn(LocalDateTime dt) {
        return dt != null ? dtf.print(dt) : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        return dbData != null ? dtf.parseLocalDateTime(dbData) : null;
    }
}
