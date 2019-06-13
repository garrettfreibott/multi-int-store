/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
public class ApplicationConfiguration {

  @Value("${logging.maximumRequestPayloadBytes}")
  private Integer maximumPayloadBytes;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    // Set buffering so the HTTP message body can be read twice.
    builder.requestFactory(BufferingClientHttpRequestFactory.class);
    builder.additionalInterceptors(new RequestResponseLoggingInterceptor());
    return builder.build();
  }

  //  @Bean
  //  public CommonsRequestLoggingFilter requestLoggingFilter() {
  //    final CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
  //    loggingFilter.setIncludeClientInfo(true);
  //    loggingFilter.setIncludeQueryString(true);
  //    loggingFilter.setIncludePayload(true);
  //    loggingFilter.setIncludeHeaders(true);
  //    loggingFilter.setAfterMessagePrefix("Inbound Request: ");
  //    loggingFilter.setMaxPayloadLength(maximumPayloadBytes);
  //    return loggingFilter;
  //  }
}
