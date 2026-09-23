package br.com.sicredi.votacao_api.integracao.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

@TestConfiguration(proxyBeanMethods = false)
public class ControlledClockConfig {

    @Bean
    @Primary
    public MutableClock testClock() {
        return new MutableClock(
                new AtomicReference<>(
                        Instant.parse("2026-09-23T10:00:00Z")),
                        ZoneOffset.UTC);
    }
}
