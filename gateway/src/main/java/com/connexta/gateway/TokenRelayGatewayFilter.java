/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenRelayGatewayFilter implements GatewayFilter {

  private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

  public TokenRelayGatewayFilter(
      ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
    this.authorizedClientRepository = authorizedClientRepository;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return exchange
        .getPrincipal()
        .filter(principal -> principal instanceof OAuth2AuthenticationToken)
        .cast(OAuth2AuthenticationToken.class)
        .flatMap(authentication -> authorizedClient(exchange, authentication))
        .map(OAuth2AuthorizedClient::getAccessToken)
        .map(token -> withBearerAuth(exchange, token))
        .defaultIfEmpty(exchange)
        .flatMap(chain::filter);
  }

  private Mono<OAuth2AuthorizedClient> authorizedClient(
      ServerWebExchange exchange, OAuth2AuthenticationToken oauth2Authentication) {
    return this.authorizedClientRepository.loadAuthorizedClient(
        oauth2Authentication.getAuthorizedClientRegistrationId(), oauth2Authentication, exchange);
  }

  private ServerWebExchange withBearerAuth(
      ServerWebExchange exchange, OAuth2AccessToken accessToken) {
    return exchange
        .mutate()
        .request(r -> r.headers(headers -> headers.setBearerAuth(accessToken.getTokenValue())))
        .build();
  }
}
