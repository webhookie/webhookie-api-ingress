package com.webhookie.apiingress

import com.webhookie.common.WebhookieBanner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebhookieApiIngressApplication

fun main(args: Array<String>) {
  runApplication<WebhookieApiIngressApplication>(*args) {
    this.setBanner(WebhookieBanner(this, "api-ingress"))
  }
}
