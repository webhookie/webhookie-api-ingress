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

package com.webhookie.apiingress.config

import com.webhookie.common.Constants
import com.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.webhookie.common.message.ConsumerMessage
import com.webhookie.common.service.IdGenerator
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.core.GenericTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.SubscribableChannel
import java.util.concurrent.Executors

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 03:11
 */
@Configuration
class IngressConfig(private val idGenerator: IdGenerator) {
  @Bean
  fun internalIngressChannel(): SubscribableChannel {
    return MessageChannels
      .publishSubscribe(Executors.newCachedThreadPool())
      .getObject()
  }

  @Bean
  fun incomingMessageEndpoint(amqpTemplate: AmqpTemplate): AmqpOutboundEndpoint = Amqp
    .outboundAdapter(amqpTemplate)
    .exchangeName("ingress")
    .routingKey("incoming-message")
    .getObject()

  @Bean
  fun toConsumerMessageTransformer(): GenericTransformer<Message<ByteArray>, ConsumerMessage> {
    return GenericTransformer { message ->
      val topic = message.headers[Constants.Queue.Headers.WH_HEADER_TOPIC] as String
      val contentType = message.headers[Constants.Queue.Headers.HEADER_CONTENT_TYPE] as String

      @Suppress("UNCHECKED_CAST")
      val authorizedSubscribers: Collection<String> =
        message.headers[Constants.Queue.Headers.WH_HEADER_AUTHORIZED_SUBSCRIBER] as? Collection<String> ?: emptySet()

      val headerTraceId = message.headers[WH_HEADER_TRACE_ID] as String?
      val traceId = headerTraceId ?: idGenerator.generate()
      ConsumerMessage(
        traceId,
        topic,
        contentType,
        authorizedSubscribers.toSet(),
        message.payload,
        message.headers
      )
    }
  }
}
