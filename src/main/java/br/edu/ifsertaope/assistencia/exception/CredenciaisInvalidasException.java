package br.edu.ifsertaope.assistencia.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos.");
    }
}
