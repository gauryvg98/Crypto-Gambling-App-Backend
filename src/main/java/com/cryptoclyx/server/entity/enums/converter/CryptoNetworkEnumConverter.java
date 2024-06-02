package com.cryptoclyx.server.entity.enums.converter;

import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import org.springframework.core.convert.converter.Converter;

public class CryptoNetworkEnumConverter implements Converter<String, CryptoNetwork> {

    @Override
    public CryptoNetwork convert(String source) {
        return CryptoNetwork.valueOf(source.toUpperCase());
    }
}
