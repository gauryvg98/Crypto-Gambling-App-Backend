package com.cryptoclyx.server.entity.enums.converter;

import com.cryptoclyx.server.entity.enums.TransactionStatus;
import org.springframework.core.convert.converter.Converter;

public class TransactionStatusEnumConverter implements Converter<String, TransactionStatus> {

    @Override
    public TransactionStatus convert(String source) {
        return TransactionStatus.valueOf(source.toUpperCase());
    }
}
