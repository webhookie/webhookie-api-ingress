package com.webhookie.apiingress.config

import com.webhookie.common.Constants
import com.webhookie.apiingress.web.IngressAPIDocs
import com.webhookie.apiingress.web.PublisherController
import com.webhookie.security.model.WebhookiePathMatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 01:35
 */
@Configuration
class IngressAuthorizationConfig {
  @Bean
  fun ingressAuthorizations(): List<WebhookiePathMatcher> {
    return listOf(
      WebhookiePathMatcher.Builder()
        .pattern("${IngressAPIDocs.REQUEST_MAPPING_INGRESS}/${PublisherController.REQUEST_MAPPING_CONSUMER_EVENT}/**")
        .hasAuthority(Constants.Security.Roles.ROLE_CONSUMER)
        .build(),
    )
  }
}
