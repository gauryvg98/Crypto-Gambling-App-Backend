package com.cryptoclyx.server.config;

import com.cryptoclyx.server.entity.enums.converter.CryptoNetworkEnumConverter;
import com.cryptoclyx.server.entity.enums.converter.OperationTypeEnumConverter;
import com.cryptoclyx.server.entity.enums.converter.TransactionStatusEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private static final String[] CORS_ALLOWED_METHODS = {"POST", "GET", "PUT", "HEAD", "DELETE"};

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods(CORS_ALLOWED_METHODS);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new CryptoNetworkEnumConverter());
    registry.addConverter(new OperationTypeEnumConverter());
    registry.addConverter(new TransactionStatusEnumConverter());
  }
}
