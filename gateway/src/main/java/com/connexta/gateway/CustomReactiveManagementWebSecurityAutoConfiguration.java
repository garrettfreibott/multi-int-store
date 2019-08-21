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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;

/**
 * NOTE: This code duplicates ReactiveManagementWebSecurityAutoConfiguration, adds oauth2Login(),
 * and disables CSRF token checking at the gateway as this will be handled by end-apps. This is
 * needed to be able modify the web security defaults to due to this bug
 * https://github.com/spring-projects/spring-security/issues/6314 Consider using a WebSecurityConfig
 * in the future when this bug is resolved.
 */
@Configuration
//@EnableResourceServer
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
public class CustomReactiveManagementWebSecurityAutoConfiguration {

//  @Bean
//  public ResourceServerConfigurer resourceServerConfigurer() {
//    return new ResourceServerConfigurer() {
//      @Override
//      public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
//
//        // This is used by the oauth2 library as a unique identifier of your microservice.
//        // It is used to verify that your service is the intended audience of a given JWT access
//        // token.
//        resources.resourceId("users");
//      }
//
//      @Override
//      public void configure(HttpSecurity http) throws Exception {
//
//        // The scopes specified here should begin with your resourceId from above.
//        //        http.authorizeRequests()
//        //            .antMatchers(HttpMethod.GET,
//        // "/api/v1/person").access("#oauth2.hasScope('users.read')")
//        //            .antMatchers(HttpMethod.POST,
//        // "/api/v1/person").access("#oauth2.hasScope('users.write')");
//      }
//    };
//  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
      throws Exception {
    // @formatter:off
    return http
        .authorizeExchange()
        .matchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
        .permitAll()
        .anyExchange()
        .authenticated()
//        .and()
//        .oauth2ResourceServer()
        .and()
        .oauth2Login()
        .and()
        .exceptionHandling()
        // auto-redirect to keycloak for auth
        .authenticationEntryPoint(
            new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/master"))
        .and()
        .csrf()
        .disable() // CSRF tokens will be handled at the app level, not at the gateway
        .build();
    // @formatter:on
  }
}
