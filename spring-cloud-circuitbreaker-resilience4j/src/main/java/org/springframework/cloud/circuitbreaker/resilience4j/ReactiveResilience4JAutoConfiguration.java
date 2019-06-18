/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.circuitbreaker.resilience4j;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.circuitbreaker.commons.Customizer;
import org.springframework.cloud.circuitbreaker.commons.ReactorCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ryan Baxter
 */
@Configuration
@ConditionalOnClass(
		name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux" })
public class ReactiveResilience4JAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ReactorCircuitBreakerFactory.class)
	public ReactorCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory() {
		return new ReactorResilience4JCircuitBreakerFactory();
	}

	@Configuration
	@ConditionalOnClass(
			name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux" })
	public static class ReactiveResilience4JCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<ReactorResilience4JCircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired(required = false)
		private ReactorResilience4JCircuitBreakerFactory factory;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
		}

	}

}
