package br.com.fiap.cidadesesg.exception;

/** Os dados são válidos sintaticamente, mas violam uma regra de negócio (HTTP 422). */
public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
