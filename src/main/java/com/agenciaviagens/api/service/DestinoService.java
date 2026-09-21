package com.agenciaviagens.api.service;

import com.agenciaviagens.api.dto.AvaliacaoRequestDTO;
import com.agenciaviagens.api.dto.DestinoRequestDTO;
import com.agenciaviagens.api.exception.DestinoNaoEncontradoException;
import com.agenciaviagens.api.model.Avaliacao;
import com.agenciaviagens.api.model.Destino;
import com.agenciaviagens.api.repository.AvaliacaoRepository;
import com.agenciaviagens.api.repository.DestinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Regra de negocio dos destinos, agora operando sobre dados persistidos.
 *
 * @Transactional(readOnly = true) na classe: toda consulta roda em uma
 * transacao somente leitura (mais leve e segura). Os metodos que alteram
 * dados sobrescrevem isso com @Transactional comum, garantindo que a
 * operacao inteira seja confirmada (commit) ou desfeita (rollback) junta —
 * algo que simplesmente nao existia no armazenamento em memoria.
 */
@Service
@Transactional(readOnly = true)
public class DestinoService {

    private final DestinoRepository destinoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public DestinoService(DestinoRepository destinoRepository,
                          AvaliacaoRepository avaliacaoRepository) {
        this.destinoRepository = destinoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional
    public Destino cadastrar(DestinoRequestDTO dto) {
        Destino destino = new Destino();
        preencherDestinoComDto(destino, dto);
        return destinoRepository.save(destino);
    }

    public List<Destino> listarTodos() {
        return destinoRepository.findAll();
    }

    public Destino buscarPorId(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new DestinoNaoEncontradoException(id));
    }

    public List<Destino> pesquisar(String nome, String localizacao) {
        String nomeFiltro = normalizar(nome);
        String localizacaoFiltro = normalizar(localizacao);
        return destinoRepository.pesquisar(nomeFiltro, localizacaoFiltro);
    }

    @Transactional
    public Destino atualizar(Long id, DestinoRequestDTO dto) {
        Destino destinoExistente = buscarPorId(id);
        preencherDestinoComDto(destinoExistente, dto);
        return destinoRepository.save(destinoExistente);
    }

    /**
     * Registra uma avaliacao e recalcula a media do destino.
     * O login de quem avaliou vem do usuario autenticado, repassado pelo controller.
     *
     * A avaliacao e salva explicitamente pelo seu proprio repository, em vez
     * de depender apenas do cascade configurado no lado "Destino" do
     * relacionamento — isso evita ambiguidades do Hibernate ao cascatear
     * insercoes em colecoes do tipo List e deixa o codigo mais explicito.
     */
    @Transactional
    public Destino registrarAvaliacao(Long id, AvaliacaoRequestDTO dto, String usuarioLogin) {
        Destino destino = buscarPorId(id);

        Avaliacao avaliacao = new Avaliacao(dto.getNota(), dto.getComentario(), usuarioLogin);
        destino.adicionarAvaliacao(avaliacao); // vincula a avaliacao ao destino e recalcula a media

        avaliacaoRepository.save(avaliacao);   // persiste a avaliacao explicitamente
        return destinoRepository.save(destino); // persiste a media/quantidade atualizadas
    }

    @Transactional
    public void excluir(Long id) {
        if (!destinoRepository.existsById(id)) {
            throw new DestinoNaoEncontradoException(id);
        }
        destinoRepository.deleteById(id);
    }

    private void preencherDestinoComDto(Destino destino, DestinoRequestDTO dto) {
        destino.setNome(dto.getNome());
        destino.setLocalizacao(dto.getLocalizacao());
        destino.setDescricao(dto.getDescricao());
        destino.setPrecoPacote(dto.getPrecoPacote());
        destino.setHoteisDisponiveis(
                dto.getHoteisDisponiveis() != null ? dto.getHoteisDisponiveis() : false);
        destino.setAtividadesTuristicas(
                dto.getAtividadesTuristicas() != null
                        ? new ArrayList<>(dto.getAtividadesTuristicas())
                        : new ArrayList<>());
    }

    /**
     * Converte texto vazio ou em branco para null, para que a consulta
     * trate o filtro como "nao informado" em vez de procurar por vazio.
     */
    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
