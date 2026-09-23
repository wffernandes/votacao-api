package br.com.sicredi.votacao_api.voto.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

@Component
public class VotoDuplicadoConstraintDetector {

    private static final String CONSTRAINT = "uk_voto_sessao_associado";

    public boolean isVotoDuplicado(Throwable exception) {

        Throwable causa = exception;

        while (causa != null) {
            if (causa instanceof ConstraintViolationException violation) {
                return CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
            }
            causa = causa.getCause();
        }
        return false;
    }
}
