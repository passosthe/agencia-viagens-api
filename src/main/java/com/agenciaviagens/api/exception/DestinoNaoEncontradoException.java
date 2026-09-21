package com.agenciaviagens.api.exception;

/**
 * Lancada quando se tenta buscar, atualizar, avaliar ou excluir um destino
 * cujo id nao existe no banco de dados.
 */
public class DestinoNaoEncontradoException extends RuntimeException {

    public DestinoNaoEncontradoException(Long id) {
        super("Destino com id " + id + " nao foi encontrado");
    }
}
