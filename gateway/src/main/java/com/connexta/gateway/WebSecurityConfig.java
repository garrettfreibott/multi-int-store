/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.gateway;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;

@Configuration
@ConditionalOnClass({EnableWebFluxSecurity.class, WebFilterChainProxy.class})
@ConditionalOnMissingBean({SecurityWebFilterChain.class, WebFilterChainProxy.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@AutoConfigureBefore(ReactiveSecurityAutoConfiguration.class)
@AutoConfigureAfter({
  HealthEndpointAutoConfiguration.class,
  InfoEndpointAutoConfiguration.class,
  WebEndpointAutoConfiguration.class,
  ReactiveOAuth2ClientAutoConfiguration.class
})
@EnableWebFluxSecurity // BREAKING LINE
class WebSecurityConfig {

  // NOTE: This code duplicates ReactiveManagementWebSecurityAutoConfiguration and enhances with
  // oauth2Login() specific configuration

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    // @formatter:off
    return http.csrf()
        .disable()
        .authorizeExchange()
        .matchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .httpBasic()
        .and()
        .formLogin()
        .and()
        // START BREAKING SECTION
        .oauth2ResourceServer()
        .jwt()
        .and()
        .and()
        // END BREAKING SECTION
        .oauth2Login()
        .and()
        //        .exceptionHandling()
        //        // NOTE:
        //        // This configuration is needed to perform the auto-redirect to UAA for
        // authentication.
        //        // Leaving this out will result in a default login page with option for
        // formLogin() and link
        //        // for UAA for oauth2Login()
        //        .authenticationEntryPoint(
        //            new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/master"))
        //        .and()
        .build();
    // @formatter:on
  }
}
