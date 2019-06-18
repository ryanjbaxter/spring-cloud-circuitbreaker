/*
 * Copyright 2013-2018 the original author or authors.
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

package org.springframework.cloud.circuitbreaker.hystrix;

import java.util.function.Function;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;

import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactorCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactorCircuitBreakerFactory;
import org.springframework.util.Assert;

/**
 * @author Ryan Baxter
 */
public class ReactorHystrixCircuitBreakerFactory extends
		ReactorCircuitBreakerFactory<HystrixObservableCommand.Setter, ReactorHystrixCircuitBreakerFactory.ReactiveHystrixConfigBuilder> {

	private Function<String, HystrixObservableCommand.Setter> defaultConfiguration = id -> HystrixObservableCommand.Setter
			.withGroupKey(HystrixCommandGroupKey.Factory.asKey(id));

	@Override
	protected ReactiveHystrixConfigBuilder configBuilder(String id) {
		return new ReactiveHystrixConfigBuilder(id);
	}

	@Override
	public void configureDefault(
			Function<String, HystrixObservableCommand.Setter> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	@Override
	public ReactorCircuitBreaker createReactor(String id) {
		return createReactorHystrixCircuitBreaker(id);
	}

	private ReactorHystrixCircuitBreaker createReactorHystrixCircuitBreaker(String id) {
		Assert.hasText(id, "A CircuitBreaker must have an id.");
		HystrixObservableCommand.Setter setter = getConfigurations().computeIfAbsent(id,
				defaultConfiguration);
		return new ReactorHystrixCircuitBreaker(setter);
	}

	@Override
	public ReactiveCircuitBreaker createReactive(String id) {
		return createReactorHystrixCircuitBreaker(id);
	}

	public static class ReactiveHystrixConfigBuilder
			extends AbstractHystrixConfigBuilder<HystrixObservableCommand.Setter> {

		public ReactiveHystrixConfigBuilder(String id) {
			super(id);
		}

		@Override
		public HystrixObservableCommand.Setter build() {
			return HystrixObservableCommand.Setter.withGroupKey(getGroupKey())
					.andCommandKey(getCommandKey())
					.andCommandPropertiesDefaults(getCommandPropertiesSetter());
		}

	}

}
