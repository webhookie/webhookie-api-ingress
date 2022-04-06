package com.webhookie.apiingress

import com.webhookie.common.message.ConsumerMessage
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 08:37
 */
@Configuration
class IngressFlows {
  @Bean
  fun internalConsumerFlow(
    incomingMessageEndpoint: AmqpOutboundEndpoint,
    internalIngressChannel: SubscribableChannel,
    toConsumerMessageTransformer: GenericTransformer<Message<ByteArray>, ConsumerMessage>
  ): IntegrationFlow {
    return integrationFlow {
      channel(internalIngressChannel)
      transform<Message<ByteArray>> { toConsumerMessageTransformer.transform(it) }
      handle(incomingMessageEndpoint)
    }
  }
}
