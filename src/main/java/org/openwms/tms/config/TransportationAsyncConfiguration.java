/*
 * Copyright 2018 Heiko Scherrer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.tms.config;

import org.ameba.mapping.BeanMapper;
import org.openwms.common.transport.TransportUnitEventPropagator;
import org.openwms.core.SpringProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.ameba.LoggingCategories.BOOT;

/**
 * A TransportationAsyncConfiguration.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Configuration
@EnableRabbit
class TransportationAsyncConfiguration {

    private static final Logger BOOT_LOGGER = LoggerFactory.getLogger(BOOT);

    @ConditionalOnExpression("'${owms.transportation.serialization}'=='json'")
    @Bean
    MessageConverter messageConverter() {
        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();
        BOOT_LOGGER.info("Using JSON serialization over AMQP");
        return messageConverter;
    }

    @ConditionalOnExpression("'${owms.transportation.serialization}'=='barray'")
    @Bean
    MessageConverter serializerMessageConverter() {
        SerializerMessageConverter messageConverter = new SerializerMessageConverter();
        BOOT_LOGGER.info("Using byte array serialization over AMQP");
        return messageConverter;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(15000);
        backOffPolicy.setInitialInterval(500);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    TransportUnitEventPropagator transportUnitEventPropagator(AmqpTemplate amqpTemplate, @Value("${owms.events.common.tu.exchange-name}") String exchangeName, BeanMapper beanMapper) {
        return new TransportUnitEventPropagator(amqpTemplate, exchangeName, beanMapper);
    }

    @Bean
    TopicExchange commonCommandsExchange(@Value("${owms.commands.common.tu.exchange-name}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Queue commonCommandsQueue(@Value("${owms.commands.common.tu.queue-name}") String queueName) {
        return new Queue(queueName);
    }

    @Bean
    Binding commonCommandsBinding(TopicExchange commonCommandsExchange, Queue commonCommandsQueue, @Value("${owms.commands.common.tu.routing-key}") String routingKey) {
        return BindingBuilder.bind(commonCommandsQueue)
                .to(commonCommandsExchange)
                .with(routingKey);
    }

    @Bean
    TopicExchange tmsCommandsExchange(@Value("${owms.commands.tms.to.exchange-name}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Queue tmsCommandsQueue(@Value("${owms.commands.tms.to.queue-name}") String queueName) {
        return new Queue(queueName);
    }

    @Bean
    Binding tmsCommandsBinding(TopicExchange tmsCommandsExchange, Queue tmsCommandsQueue, @Value("${owms.commands.tms.to.routing-key}") String routingKey) {
        return BindingBuilder.bind(tmsCommandsQueue)
                .to(tmsCommandsExchange)
                .with(routingKey);
    }
}
