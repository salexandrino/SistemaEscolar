package br.com.kutuar.academico.services;

import br.com.kutuar.academico.dtos.*;
import br.com.kutuar.academico.models.Aluno;
import br.com.kutuar.academico.models.DocumentoAluno;
import br.com.kutuar.academico.models.HistoricoSituacaoAluno;
import br.com.kutuar.academico.models.Matricula;
import br.com.kutuar.academico.repositories.AlunoRepository;
import br.com.kutuar.academico.repositories.DocumentoAlunoRepository;
import br.com.kutuar.academico.repositories.HistoricoSituacaoAlunoRepository;
import br.com.kutuar.academico.repositories.MatriculaRepository;
import br.com.kutuar.academico.services.estados.SituacaoAluno;
import br.com.kutuar.academico.services.observers.AlunoSituacaoObserver;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.strategies.ValidadorCpf;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final HistoricoSituacaoAlunoRepository historicoRepository;
    private final MatriculaRepository matriculaRepository;
    private final DocumentoAlunoRepository documentoRepository;
    private final ValidadorCpf validadorCpf;
    private final TurmaService turmaService;
    private final List<AlunoSituacaoObserver> observers = new ArrayList<>();


    public AlunoService(AlunoRepository alunoRepository,
                        HistoricoSituacaoAlunoRepository historicoRepository,
                        MatriculaRepository matriculaRepository,
                        DocumentoAlunoRepository documentoRepository,
                        ValidadorCpf validadorCpf,
                        TurmaService turmaService) {
        this.alunoRepository = alunoRepository;
        this.historicoRepository = historicoRepository;
        this.matriculaRepository = matriculaRepository;
        this.documentoRepository = documentoRepository;
        this.validadorCpf = validadorCpf;
        this.turmaService = turmaService;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public AlunoResponseDTO criar(CriarAlunoDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        if (dto.getDataNascimento() != null && dto.getDataNascimento().isAfter(LocalDate.now())) {
            throw new ValidationException("A data de nascimento não pode ser no futuro.");
        }
        if (dto.isUsaTransporteEscolar() && (dto.getResponsavelTransporte() == null
                || "NENHUM".equalsIgnoreCase(dto.getResponsavelTransporte()))) {
            throw new ValidationException("Se o aluno usa transporte escolar, informe o responsável (ESTADUAL ou MUNICIPAL).");
        }

        UUID tenantId = tenant();

        if (dto.getCpf() != null && !dto.getCpf().isBlank()) {
            validadorCpf.validar(dto.getCpf());
            if (alunoRepository.existsByCpf(tenantId, dto.getCpf())) {
                throw new ConflictException("CPF já cadastrado para este tenant.");
            }
        }

        Aluno a = new Aluno();
        a.setId(UUID.randomUUID());
        a.setTenantId(tenantId);
        a.setNome(dto.getNome().trim());
        a.setCpf(dto.getCpf() != null && !dto.getCpf().isBlank() ? dto.getCpf().trim() : null);
        a.setDataNascimento(dto.getDataNascimento());
        a.setEmail(dto.getEmail());
        a.setTelefone(dto.getTelefone());
        a.setSituacao(SituacaoAluno.ATIVO.name());
        a.setCriadoEm(LocalDateTime.now());
        a.setAtualizadoEm(LocalDateTime.now());

        aplicarCamposCenso(a, dto.getCodigoInep(), dto.getNomePai(), dto.getNomeMae(), dto.getSexo(),
                dto.getCorRaca(), dto.getNacionalidade(), dto.getUfNascimento(), dto.getMunicipioNascimento(),
                dto.getCertidaoNascimento(), dto.getNis(), dto.getCep(), dto.getEndereco(), dto.getNumero(),
                dto.getComplemento(), dto.getBairro(), dto.getCidade(), dto.getEstado(), dto.getZona(),
                dto.getLocalizacaoDiferenciada(), dto.isUsaTransporteEscolar(), dto.getResponsavelTransporte(),
                dto.getTipoCondicao(), dto.getRecursosAcessibilidade());

        Aluno criado = alunoRepository.criar(a);

        HistoricoSituacaoAluno h = new HistoricoSituacaoAluno();
        h.setId(UUID.randomUUID());
        h.setTenantId(tenantId);
        h.setIdAluno(criado.getId());
        h.setSituacaoAnterior(null);
        h.setSituacaoNova(SituacaoAluno.ATIVO.name());
        h.setMotivo("Cadastro inicial");
        h.setCriadoEm(LocalDateTime.now());
        historicoRepository.criar(h);

        return toDto(criado);
    }

    public void adicionarObserver(AlunoSituacaoObserver observer) {
        observers.add(observer);
    }

    public AlunoResponseDTO atualizar(UUID id, AtualizarAlunoDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        if (dto.isUsaTransporteEscolar() && (dto.getResponsavelTransporte() == null
                || "NENHUM".equalsIgnoreCase(dto.getResponsavelTransporte()))) {
            throw new ValidationException("Se o aluno usa transporte escolar, informe o responsável (ESTADUAL ou MUNICIPAL).");
        }
        UUID tenantId = tenant();

        Aluno a = alunoRepository.buscarPorId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado."));

        a.setNome(dto.getNome().trim());
        a.setDataNascimento(dto.getDataNascimento());
        a.setEmail(dto.getEmail());
        a.setTelefone(dto.getTelefone());

        aplicarCamposCenso(a, dto.getCodigoInep(), dto.getNomePai(), dto.getNomeMae(), dto.getSexo(),
                dto.getCorRaca(), dto.getNacionalidade(), dto.getUfNascimento(), dto.getMunicipioNascimento(),
                dto.getCertidaoNascimento(), dto.getNis(), dto.getCep(), dto.getEndereco(), dto.getNumero(),
                dto.getComplemento(), dto.getBairro(), dto.getCidade(), dto.getEstado(), dto.getZona(),
                dto.getLocalizacaoDiferenciada(), dto.isUsaTransporteEscolar(), dto.getResponsavelTransporte(),
                dto.getTipoCondicao(), dto.getRecursosAcessibilidade());

        alunoRepository.atualizar(a);

        return toDto(alunoRepository.buscarPorId(tenantId, id).get());
    }

    /**
     * Aplica os campos do Censo Escolar (Educacenso) no model Aluno.
     * Centralizado aqui pra criar() e atualizar() não duplicarem a mesma
     * lista gigante de setters.
     */
    private void aplicarCamposCenso(Aluno a, String codigoInep, String nomePai, String nomeMae, String sexo,
                                    String corRaca, String nacionalidade, String ufNascimento,
                                    String municipioNascimento, String certidaoNascimento, String nis,
                                    String cep, String endereco, String numero, String complemento,
                                    String bairro, String cidade, String estado, String zona,
                                    String localizacaoDiferenciada, boolean usaTransporteEscolar,
                                    String responsavelTransporte, String tipoCondicao, String recursosAcessibilidade) {
        a.setCodigoInep(codigoInep);
        a.setNomePai(nomePai);
        a.setNomeMae(nomeMae);
        a.setSexo(sexo);
        a.setCorRaca(corRaca);
        a.setNacionalidade(nacionalidade != null && !nacionalidade.isBlank() ? nacionalidade : "Brasileira");
        a.setUfNascimento(ufNascimento);
        a.setMunicipioNascimento(municipioNascimento);
        a.setCertidaoNascimento(certidaoNascimento);
        a.setNis(nis);
        a.setCep(cep);
        a.setEndereco(endereco);
        a.setNumero(numero);
        a.setComplemento(complemento);
        a.setBairro(bairro);
        a.setCidade(cidade);
        a.setEstado(estado);
        a.setZona(zona);
        a.setLocalizacaoDiferenciada(localizacaoDiferenciada != null && !localizacaoDiferenciada.isBlank()
                ? localizacaoDiferenciada : "NAO_DIFERENCIADA");
        a.setUsaTransporteEscolar(usaTransporteEscolar);
        a.setResponsavelTransporte(usaTransporteEscolar ? responsavelTransporte : "NENHUM");
        a.setTipoCondicao(tipoCondicao);
        a.setRecursosAcessibilidade(recursosAcessibilidade);
    }

    public void alterarSituacao(UUID id, AlterarSituacaoAlunoDTO dto) {
        if (dto == null || dto.getNovaSituacao() == null) throw new ValidationException("Nova situação é obrigatória.");

        UUID tenantId = tenant();
        Aluno a = alunoRepository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Aluno não encontrado."));

        SituacaoAluno atual;
        SituacaoAluno nova;
        try {
            atual = SituacaoAluno.valueOf(a.getSituacao());
            nova = SituacaoAluno.valueOf(dto.getNovaSituacao().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Situação inválida.");
        }

        if (!atual.podeMudarPara(nova)) {
            throw new ValidationException("Transição de estado não permitida de " + atual + " para " + nova + ".");
        }

        alunoRepository.atualizarSituacao(tenantId, id, nova.name());

        String motivo = dto.getMotivo() != null ? dto.getMotivo() : "Alteração manual";
        for (AlunoSituacaoObserver observer : observers) {
            observer.aoMudarSituacao(a, atual, nova, motivo);
        }
    }

    public MatriculaResponseDTO matricular(UUID idAluno, MatricularAlunoDTO dto) {
        UUID tenantId = tenant();
        alunoRepository.buscarPorId(tenantId, idAluno).orElseThrow(() -> new NotFoundException("Aluno não encontrado."));

        if (matriculaRepository.existeAtiva(tenantId, idAluno, dto.getIdTurma())) {
            throw new ValidationException("Aluno já matriculado nesta turma.");
        }

        turmaService.validarDisponibilidadeParaMatricula(tenantId, dto.getIdTurma());

        Matricula m = new Matricula();
        m.setId(UUID.randomUUID());
        m.setTenantId(tenantId);
        m.setIdAluno(idAluno);
        m.setIdTurma(dto.getIdTurma());
        m.setDataMatricula(LocalDate.now());
        m.setStatus("ATIVA");
        m.setCriadoEm(LocalDateTime.now());
        m.setAtualizadoEm(LocalDateTime.now());

        Matricula criada = matriculaRepository.criar(m);
        return toDto(criada);
    }

    public void transferir(UUID idAluno, TransferirAlunoDTO dto) throws Exception {
        UUID tenantId = tenant();
        alunoRepository.buscarPorId(tenantId, idAluno).orElseThrow(() -> new NotFoundException("Aluno não encontrado."));

        Matricula mAtual = matriculaRepository.obterAtivaPorAluno(tenantId, idAluno)
                .orElseThrow(() -> new ValidationException("Aluno não possui matrícula ativa para transferir."));

        Matricula mNova = new Matricula();
        mNova.setId(UUID.randomUUID());
        mNova.setTenantId(tenantId);
        mNova.setIdAluno(idAluno);
        mNova.setIdTurma(dto.getIdNovaTurma());
        mNova.setDataMatricula(LocalDate.now());
        mNova.setStatus("ATIVA");
        mNova.setCriadoEm(LocalDateTime.now());
        mNova.setAtualizadoEm(LocalDateTime.now());

        HistoricoSituacaoAluno h = new HistoricoSituacaoAluno();
        h.setId(UUID.randomUUID());
        h.setTenantId(tenantId);
        h.setIdAluno(idAluno);
        h.setSituacaoAnterior("ATIVO");
        h.setSituacaoNova("ATIVO");
        h.setMotivo("Transferência de turma: " + dto.getMotivo());
        h.setCriadoEm(LocalDateTime.now());

        matriculaRepository.transferir(mAtual, mNova, h);
    }

    public DocumentoAlunoDTO adicionarDocumento(UUID idAluno, CriarDocumentoAlunoDTO dto) {
        UUID tenantId = tenant();
        alunoRepository.buscarPorId(tenantId, idAluno).orElseThrow(() -> new NotFoundException("Aluno não encontrado."));

        DocumentoAluno d = new DocumentoAluno();
        d.setId(UUID.randomUUID());
        d.setTenantId(tenantId);
        d.setIdAluno(idAluno);
        d.setTipo(dto.getTipo());
        d.setReferencia(dto.getReferencia());
        d.setCriadoEm(LocalDateTime.now());
        d.setAtualizadoEm(LocalDateTime.now());

        DocumentoAluno criado = documentoRepository.criar(d);

        DocumentoAlunoDTO response = new DocumentoAlunoDTO();
        response.setId(criado.getId());
        response.setTipo(criado.getTipo());
        response.setReferencia(criado.getReferencia());
        return response;
    }

    public List<DocumentoAlunoDTO> listarDocumentos(UUID idAluno) {
        UUID tenantId = tenant();
        return documentoRepository.listarPorAluno(tenantId, idAluno).stream().map(d -> {
            DocumentoAlunoDTO response = new DocumentoAlunoDTO();
            response.setId(d.getId());
            response.setTipo(d.getTipo());
            response.setReferencia(d.getReferencia());
            return response;
        }).collect(Collectors.toList());
    }

    public br.com.kutuar.academico.dtos.HistoricoEscolarDTO emitirHistoricoEscolar(UUID tenantId, UUID idAluno) {
        Aluno aluno = alunoRepository.buscarPorId(tenantId, idAluno)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado."));

        List<br.com.kutuar.academico.dtos.HistoricoEscolarDTO.ItemMatriculaHistorico> itens =
                matriculaRepository.buscarHistoricoMatriculas(tenantId, idAluno);

        return new br.com.kutuar.academico.dtos.HistoricoEscolarDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getCpf(),
                aluno.getSituacao(),
                itens
        );
    }

    public AlunoResponseDTO obterPorId(UUID id) {
        UUID tenantId = tenant();
        Aluno a = alunoRepository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Aluno não encontrado."));
        return toDto(a);
    }

    public List<AlunoResponseDTO> listar() {
        UUID tenantId = tenant();
        return alunoRepository.listar(tenantId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private AlunoResponseDTO toDto(Aluno a) {
        AlunoResponseDTO dto = new AlunoResponseDTO();
        dto.setId(a.getId());
        dto.setNome(a.getNome());
        dto.setCpf(a.getCpf());
        dto.setDataNascimento(a.getDataNascimento());
        dto.setEmail(a.getEmail());
        dto.setTelefone(a.getTelefone());
        dto.setSituacao(a.getSituacao());
        dto.setCriadoEm(a.getCriadoEm());

        dto.setCodigoInep(a.getCodigoInep());
        dto.setNomePai(a.getNomePai());
        dto.setNomeMae(a.getNomeMae());
        dto.setSexo(a.getSexo());
        dto.setCorRaca(a.getCorRaca());
        dto.setNacionalidade(a.getNacionalidade());
        dto.setUfNascimento(a.getUfNascimento());
        dto.setMunicipioNascimento(a.getMunicipioNascimento());
        dto.setCertidaoNascimento(a.getCertidaoNascimento());
        dto.setNis(a.getNis());

        dto.setCep(a.getCep());
        dto.setEndereco(a.getEndereco());
        dto.setNumero(a.getNumero());
        dto.setComplemento(a.getComplemento());
        dto.setBairro(a.getBairro());
        dto.setCidade(a.getCidade());
        dto.setEstado(a.getEstado());
        dto.setZona(a.getZona());
        dto.setLocalizacaoDiferenciada(a.getLocalizacaoDiferenciada());

        dto.setUsaTransporteEscolar(a.isUsaTransporteEscolar());
        dto.setResponsavelTransporte(a.getResponsavelTransporte());

        dto.setTipoCondicao(a.getTipoCondicao());
        dto.setRecursosAcessibilidade(a.getRecursosAcessibilidade());

        return dto;
    }

    private MatriculaResponseDTO toDto(Matricula m) {
        MatriculaResponseDTO dto = new MatriculaResponseDTO();
        dto.setId(m.getId());
        dto.setIdAluno(m.getIdAluno());
        dto.setIdTurma(m.getIdTurma());
        dto.setDataMatricula(m.getDataMatricula());
        dto.setStatus(m.getStatus());
        return dto;
    }
}