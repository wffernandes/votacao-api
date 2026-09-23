package br.com.sicredi.votacao_api.integracao.util;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MutableClock extends Clock {

    private final AtomicReference<Instant> instante;
    private final ZoneId zona;

    public MutableClock(AtomicReference<Instant> instante, ZoneId zona) {
        this.instante = instante;
        this.zona = zona;
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instante, zone);
    }

    @Override
    public Instant instant() {
        return instante.get();
    }

    public void definirInstante(Instant novoInstante) {
        instante.set(Objects.requireNonNull(novoInstante));
    }

    public void avancar(Duration duracao) {
        instante.updateAndGet(atual -> atual.plus(duracao));
    }
}
