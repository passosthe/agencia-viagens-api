package com.agenciaviagens.api.repository;

import com.agenciaviagens.api.model.Destino;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Repository
public class DestinoRepository {

    private final Map<Long, Destino> banco = new ConcurrentHashMap<>();
    private final AtomicLong contadorId = new AtomicLong(0);

    public Destino salvar(Destino destino) {
        if (destino.getId() == null) {
            destino.setId(contadorId.incrementAndGet());
        }
        banco.put(destino.getId(), destino);
        return destino;
    }

    public List<Destino> listarTodos() {
        return new java.util.ArrayList<>(banco.values());
    }

    public Optional<Destino> buscarPorId(Long id) {
        return Optional.ofNullable(banco.get(id));
    }

    public List<Destino> pesquisar(String nome, String localizacao) {
        return banco.values().stream()
                .filter(d -> nome == null || d.getNome().toLowerCase().contains(nome.toLowerCase()))
                .filter(d -> localizacao == null || d.getLocalizacao().toLowerCase().contains(localizacao.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void excluir(Long id) {
        banco.remove(id);
    }

    public boolean existePorId(Long id) {
        return banco.containsKey(id);
    }
}
