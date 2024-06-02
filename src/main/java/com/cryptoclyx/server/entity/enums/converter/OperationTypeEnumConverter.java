package com.cryptoclyx.server.entity.enums.converter;

import com.cryptoclyx.server.entity.enums.OperationType;
import org.springframework.core.convert.converter.Converter;

public class OperationTypeEnumConverter implements Converter<String, OperationType> {

    @Override
    public OperationType convert(String source) {
        return OperationType.valueOf(source.toUpperCase());
    }
}
