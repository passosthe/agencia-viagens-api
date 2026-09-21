package com.agenciaviagens.api.repository;

import com.agenciaviagens.api.model.Destino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository de Destino.
 *
 * Diferenca central em relacao a versao 1: antes esta era uma CLASSE que
 * manipulava um Map na mao. Agora e apenas uma INTERFACE — ao herdar de
 * JpaRepository, o Spring Data gera automaticamente a implementacao com
 * save, findAll, findById, deleteById, existsById e muitos outros metodos,
 * todos convertidos em comandos SQL contra o PostgreSQL.
 */
@Repository
public interface DestinoRepository extends JpaRepository<Destino, Long> {

    /**
     * Query derivada do nome do metodo: o Spring Data interpreta
     * "findByNomeContainingIgnoreCase" e monta sozinho o SQL equivalente a
     * WHERE LOWER(nome) LIKE LOWER('%valor%').
     */
    List<Destino> findByNomeContainingIgnoreCase(String nome);

    List<Destino> findByLocalizacaoContainingIgnoreCase(String localizacao);

    /**
     * Pesquisa combinada: filtra por nome e/ou localizacao, aceitando que
     * qualquer um dos dois venha nulo (nesse caso aquele filtro e ignorado).
     * Usamos JPQL explicita porque a regra do "parametro opcional" nao e
     * expressavel por nome de metodo.
     *
     * CAST(:nome AS string): necessario especificamente para o PostgreSQL.
     * Quando o mesmo parametro aparece em duas situacoes diferentes na
     * query (comparado com IS NULL e usado dentro de uma funcao de texto
     * como LOWER), o driver JDBC do Postgres nao consegue inferir seu tipo
     * sozinho e assume "bytea" (binario) por padrao — causando o erro
     * "function lower(bytea) does not exist". O CAST explicito resolve
     * essa ambiguidade, informando que o parametro e sempre texto.
     */
    @Query("""
            SELECT d FROM Destino d
            WHERE (:nome IS NULL OR LOWER(d.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))
              AND (:localizacao IS NULL OR LOWER(d.localizacao) LIKE LOWER(CONCAT('%', CAST(:localizacao AS string), '%')))
            """)
    List<Destino> pesquisar(@Param("nome") String nome,
                            @Param("localizacao") String localizacao);
}
