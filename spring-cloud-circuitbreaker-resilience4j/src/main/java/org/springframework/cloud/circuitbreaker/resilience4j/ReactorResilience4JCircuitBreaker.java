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

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.circuitbreaker.commons.Customizer;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.ReactorCircuitBreaker;

/**
 * @author Ryan Baxter
 */
public class ReactorResilience4JCircuitBreaker
		implements ReactorCircuitBreaker, ReactiveCircuitBreaker {

	private String id;

	private Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config;

	private CircuitBreakerRegistry registry;

	private Optional<Customizer<CircuitBreaker>> circuitBreakerCustomizer;

	public ReactorResilience4JCircuitBreaker(String id,
			Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config,
			CircuitBreakerRegistry circuitBreakerRegistry,
			Optional<Customizer<CircuitBreaker>> circuitBreakerCustomizer) {
		this.id = id;
		this.config = config;
		this.registry = circuitBreakerRegistry;
		this.circuitBreakerCustomizer = circuitBreakerCustomizer;
	}

	@Override
	public <T> Mono<T> run(Mono<T> toRun, Function<Throwable, Mono<T>> fallback) {
		io.github.resilience4j.circuitbreaker.CircuitBreaker defaultCircuitBreaker = registry
				.circuitBreaker(id, config.getCircuitBreakerConfig());
		circuitBreakerCustomizer
				.ifPresent(customizer -> customizer.customize(defaultCircuitBreaker));
		Mono<T> toReturn = toRun
				.transform(CircuitBreakerOperator.of(defaultCircuitBreaker))
				.timeout(config.getTimeLimiterConfig().getTimeoutDuration())
				// Since we are using the Mono timeout we need to tell the circuit breaker
				// about the error
				.doOnError(TimeoutException.class, t -> defaultCircuitBreaker.onError(
						config.getTimeLimiterConfig().getTimeoutDuration().toMillis(),
						t));
		if (fallback != null) {
			toReturn = toReturn.onErrorResume(fallback);
		}
		return toReturn;
	}

	@Override
	public <T> Flux<T> run(Flux<T> toRun, Function<Throwable, Flux<T>> fallback) {
		return runFlux(toRun, fallback);
	}

	private <T> Flux<T> runFlux(Flux<T> toRun, Function<Throwable, Flux<T>> fallback) {
		io.github.resilience4j.circuitbreaker.CircuitBreaker defaultCircuitBreaker = registry
				.circuitBreaker(id, config.getCircuitBreakerConfig());
		circuitBreakerCustomizer
				.ifPresent(customizer -> customizer.customize(defaultCircuitBreaker));
		Flux<T> toReturn = toRun
				.transform(CircuitBreakerOperator.of(defaultCircuitBreaker))
				.timeout(config.getTimeLimiterConfig().getTimeoutDuration())
				// Since we are using the Flux timeout we need to tell the circuit breaker
				// about the error
				.doOnError(TimeoutException.class, t -> defaultCircuitBreaker.onError(
						config.getTimeLimiterConfig().getTimeoutDuration().toMillis(),
						t));
		if (fallback != null) {
			toReturn = toReturn.onErrorResume(fallback);
		}
		return toReturn;
	}

	@Override
	public <T> Publisher<T> run(Publisher<T> toRun,
			Function<Throwable, Publisher<T>> fallback) {
		return runFlux(Flux.from(toRun), t -> Flux.from(fallback.apply(t)));
	}

}
