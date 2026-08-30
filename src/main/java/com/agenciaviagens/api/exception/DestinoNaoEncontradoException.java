package com.agenciaviagens.api.exception;

/**
 * Exceção lançada quando se tenta buscar/atualizar/excluir/avaliar
 * um destino com um id que não existe.
 */
public class DestinoNaoEncontradoException extends RuntimeException {

    public DestinoNaoEncontradoException(Long id) {
        super("Destino com id " + id + " não foi encontrado");
    }
}
