/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.webhookie.apiingress.web

import com.webhookie.apiingress.web.IngressAPIDocs.Companion.REQUEST_MAPPING_INGRESS
import com.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_AUTHORIZED_SUBSCRIBER
import com.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.webhookie.common.service.IdGenerator
import com.webhookie.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import com.webhookie.common.extension.log
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:47
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_INGRESS)
class PublisherController(
  private val idGenerator: IdGenerator,
  private val internalIngressChannel: SubscribableChannel,
) {
  @PostMapping(REQUEST_MAPPING_CONSUMER_EVENT, produces = [MediaType.TEXT_PLAIN_VALUE])
  fun publishEvent(
    @RequestBody body: ByteArray,
    @RequestHeader(WH_HEADER_TOPIC, required = true) topic: String,
    @RequestHeader(WH_HEADER_TRACE_ID, required = false) traceId: String?,
    @RequestHeader(HttpHeaders.CONTENT_TYPE, required = true) contentType: String,
    @RequestHeader(
      WH_HEADER_AUTHORIZED_SUBSCRIBER,
      required = false,
      defaultValue = ""
    ) authorizedSubscribers: List<String>
  ): Mono<String> {
    return Mono.justOrEmpty(traceId)
      .switchIfEmpty { idGenerator.generate().toMono() }
      .map {
        log.info("Publishing a message to event queue....")

        val messageBuilder = MessageBuilder
          .withPayload(body)
          .setHeader(WH_HEADER_TOPIC, topic)
          .setHeader(WH_HEADER_TRACE_ID, it)
          .setHeader(HEADER_CONTENT_TYPE, contentType)
        if (authorizedSubscribers.isNotEmpty()) {
          messageBuilder.setHeader(WH_HEADER_AUTHORIZED_SUBSCRIBER, authorizedSubscribers)
        }
        val message = messageBuilder.build()

        internalIngressChannel.send(message)
      }
      .doOnNext { log.debug("Message with traceId: '{}' is being processed", traceId) }
      .map { "OK" }
  }

  companion object {
    const val REQUEST_MAPPING_CONSUMER_EVENT = "/event"
  }
}
