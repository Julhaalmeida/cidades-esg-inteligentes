package br.com.fiap.cidadesesg.exception;

/** Já existe um recurso com os mesmos dados de identificação (HTTP 409). */
public class RecursoDuplicadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
