package br.com.synge.academico.repositories;

import br.com.synge.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnoLetivoCloneRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(AnoLetivoCloneRepository.class);

    public void clonarAnoLetivo(UUID tenantId, UUID idOrigem, UUID idDestino) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Clonar Series e guardar mapeamento de IDs: map(idAntigo, idNovo)
                Map<UUID, UUID> mapSeries = new HashMap<>();
                String selSeries = "SELECT id, nome, etapa_ensino FROM serie WHERE tenant_id = ? AND id_ano_letivo = ?";
                String insSerie = "INSERT INTO serie (id, tenant_id, id_ano_letivo, nome, etapa_ensino) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psSel = conn.prepareStatement(selSeries);
                     PreparedStatement psIns = conn.prepareStatement(insSerie)) {
                    psSel.setObject(1, tenantId);
                    psSel.setObject(2, idOrigem);
                    try (ResultSet rs = psSel.executeQuery()) {
                        while (rs.next()) {
                            UUID idAntigo = rs.getObject("id", UUID.class);
                            UUID idNovo = UUID.randomUUID();
                            mapSeries.put(idAntigo, idNovo);

                            psIns.setObject(1, idNovo);
                            psIns.setObject(2, tenantId);
                            psIns.setObject(3, idDestino);
                            psIns.setString(4, rs.getString("nome"));
                            psIns.setString(5, rs.getString("etapa_ensino"));
                            psIns.addBatch();
                        }
                    }
                    psIns.executeBatch();
                }

                if (mapSeries.isEmpty()) {
                    conn.commit();
                    return; // nada a clonar
                }

                // 2. Clonar serie_disciplina (Matriz Curricular)
                String selSD = "SELECT id_serie, id_disciplina, carga_horaria_anual FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ?";
                String insSD = "INSERT INTO serie_disciplina (tenant_id, id_serie, id_disciplina, carga_horaria_anual) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psSel = conn.prepareStatement(selSD);
                     PreparedStatement psIns = conn.prepareStatement(insSD)) {
                    for (Map.Entry<UUID, UUID> entry : mapSeries.entrySet()) {
                        psSel.setObject(1, tenantId);
                        psSel.setObject(2, entry.getKey());
                        try (ResultSet rs = psSel.executeQuery()) {
                            while (rs.next()) {
                                psIns.setObject(1, tenantId);
                                psIns.setObject(2, entry.getValue());
                                psIns.setObject(3, rs.getObject("id_disciplina", UUID.class));
                                psIns.setInt(4, rs.getInt("carga_horaria_anual"));
                                psIns.addBatch();
                            }
                        }
                    }
                    psIns.executeBatch();
                }

                // 3. Clonar Turmas e Avaliações
                Map<UUID, UUID> mapTurmas = new HashMap<>();
                String selTurmas = "SELECT id, id_serie, nome, turno, sala, capacidade FROM turma WHERE tenant_id = ? AND id_ano_letivo = ?";
                String insTurma = "INSERT INTO turma (id, tenant_id, id_ano_letivo, id_serie, nome, turno, sala, capacidade, encerrada) VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)";
                try (PreparedStatement psSel = conn.prepareStatement(selTurmas);
                     PreparedStatement psIns = conn.prepareStatement(insTurma)) {
                    psSel.setObject(1, tenantId);
                    psSel.setObject(2, idOrigem);
                    try (ResultSet rs = psSel.executeQuery()) {
                        while (rs.next()) {
                            UUID idAntigo = rs.getObject("id", UUID.class);
                            UUID idNovo = UUID.randomUUID();
                            mapTurmas.put(idAntigo, idNovo);

                            UUID idSerieNova = mapSeries.get(rs.getObject("id_serie", UUID.class));
                            if (idSerieNova == null) continue;

                            psIns.setObject(1, idNovo);
                            psIns.setObject(2, tenantId);
                            psIns.setObject(3, idDestino);
                            psIns.setObject(4, idSerieNova);
                            psIns.setString(5, rs.getString("nome"));
                            psIns.setString(6, rs.getString("turno"));
                            psIns.setString(7, rs.getString("sala"));
                            psIns.setInt(8, rs.getInt("capacidade"));
                            psIns.addBatch();
                        }
                    }
                    psIns.executeBatch();
                }

                // Clonar Avaliações das turmas mapeadas
                if (!mapTurmas.isEmpty()) {
                    String selAv = "SELECT id_turma, id_disciplina, nome, peso FROM avaliacao WHERE tenant_id = ? AND id_turma = ?";
                    String insAv = "INSERT INTO avaliacao (id, tenant_id, id_turma, id_disciplina, nome, peso) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psSel = conn.prepareStatement(selAv);
                         PreparedStatement psIns = conn.prepareStatement(insAv)) {
                        for (Map.Entry<UUID, UUID> entry : mapTurmas.entrySet()) {
                            psSel.setObject(1, tenantId);
                            psSel.setObject(2, entry.getKey());
                            try (ResultSet rs = psSel.executeQuery()) {
                                while (rs.next()) {
                                    psIns.setObject(1, UUID.randomUUID());
                                    psIns.setObject(2, tenantId);
                                    psIns.setObject(3, entry.getValue());
                                    psIns.setObject(4, rs.getObject("id_disciplina", UUID.class));
                                    psIns.setString(5, rs.getString("nome"));
                                    psIns.setBigDecimal(6, rs.getBigDecimal("peso"));
                                    psIns.addBatch();
                                }
                            }
                        }
                        psIns.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Erro ao clonar ano letivo {} -> {}: {}", idOrigem, idDestino, e.getMessage(), e);
                throw e;
            }
        }
    }
}
