package br.com.fiap.cidadesesg.exception;

/** Recurso solicitado não existe (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
