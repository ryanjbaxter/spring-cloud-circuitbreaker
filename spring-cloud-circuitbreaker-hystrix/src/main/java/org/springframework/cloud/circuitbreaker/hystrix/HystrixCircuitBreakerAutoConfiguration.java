/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.circuitbreaker.hystrix;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.circuitbreaker.commons.CircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.commons.Customizer;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.netflix.hystrix.Hystrix;

/**
 * @author Ryan Baxter
 */
@Configuration
@ConditionalOnClass({ Hystrix.class })
public class HystrixCircuitBreakerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(CircuitBreakerFactory.class)
	public CircuitBreakerFactory hystrixCircuitBreakerFactory() {
		return new HystrixCircuitBreakerFactory();
	}

	@Bean
	@ConditionalOnMissingBean(ReactiveCircuitBreakerFactory.class)
	@ConditionalOnClass(name = {"reactor.core.publisher.Mono", "reactor.core.publisher.Flux"})
	public ReactiveHystrixCircuitBreakerFactory reactiveHystrixCircuitBreakerFactory() {
		return new ReactiveHystrixCircuitBreakerFactory();
	}

	@Configuration
	protected static class HystrixCircuitBreakerCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<CircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired
		private CircuitBreakerFactory factory;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
		}
	}

	@Configuration
	protected static class ReactiveHystrixCircuitBreakerCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<ReactiveCircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired
		private ReactiveCircuitBreakerFactory factory;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
		}
	}
}
