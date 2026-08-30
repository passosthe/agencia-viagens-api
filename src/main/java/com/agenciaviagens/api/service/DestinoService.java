package com.agenciaviagens.api.service;

import com.agenciaviagens.api.dto.AvaliacaoRequestDTO;
import com.agenciaviagens.api.dto.DestinoRequestDTO;
import com.agenciaviagens.api.exception.DestinoNaoEncontradoException;
import com.agenciaviagens.api.model.Destino;
import com.agenciaviagens.api.repository.DestinoRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DestinoService {

    private final DestinoRepository destinoRepository;

    public DestinoService(DestinoRepository destinoRepository) {
        this.destinoRepository = destinoRepository;
    }

    public Destino cadastrar(DestinoRequestDTO dto) {
        Destino destino = new Destino();
        preencherDestinoComDto(destino, dto);
        return destinoRepository.salvar(destino);
    }

    public List<Destino> listarTodos() {
        return destinoRepository.listarTodos();
    }

    public Destino buscarPorId(Long id) {
        return destinoRepository.buscarPorId(id)
                .orElseThrow(() -> new DestinoNaoEncontradoException(id));
    }

    public List<Destino> pesquisar(String nome, String localizacao) {
        return destinoRepository.pesquisar(nome, localizacao);
    }

    public Destino atualizar(Long id, DestinoRequestDTO dto) {
        Destino destinoExistente = buscarPorId(id); // já lança exceção se não existir
        preencherDestinoComDto(destinoExistente, dto);
        return destinoRepository.salvar(destinoExistente);
    }

    public Destino registrarAvaliacao(Long id, AvaliacaoRequestDTO dto) {
        Destino destino = buscarPorId(id);
        destino.registrarNovaAvaliacao(dto.getNota());
        return destinoRepository.salvar(destino);
    }

    public void excluir(Long id) {
        if (!destinoRepository.existePorId(id)) {
            throw new DestinoNaoEncontradoException(id);
        }
        destinoRepository.excluir(id);
    }

    /**
     * Método auxiliar privado: evita repetir código entre cadastrar() e atualizar().
     */
    private void preencherDestinoComDto(Destino destino, DestinoRequestDTO dto) {
        destino.setNome(dto.getNome());
        destino.setLocalizacao(dto.getLocalizacao());
        destino.setDescricao(dto.getDescricao());
        destino.setPrecoPacote(dto.getPrecoPacote());
        destino.setHoteisDisponiveis(dto.getHoteisDisponiveis());
        if (dto.getAtividadesTuristicas() != null) {
            destino.setAtividadesTuristicas(dto.getAtividadesTuristicas());
        }
    }
}
