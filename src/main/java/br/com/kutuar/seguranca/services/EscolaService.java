package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AtualizarEscolaDTO;
import br.com.kutuar.seguranca.dtos.PaginaEscolasDTO;
import br.com.kutuar.seguranca.enums.EscolaStatus;
import br.com.kutuar.seguranca.dtos.CriarEscolaDTO;
import br.com.kutuar.seguranca.dtos.CriarEscolaResponseDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.services.observers.EscolaCadastradaObserver;
import br.com.kutuar.seguranca.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EscolaService {

    private static final Logger logger = LoggerFactory.getLogger(EscolaService.class);
    private final EscolaRepository escolaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;
    private final List<EscolaCadastradaObserver> observers = new java.util.ArrayList<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    public EscolaService(EscolaRepository escolaRepository, UsuarioRepository usuarioRepository, PasswordService passwordService) {
        this.escolaRepository = escolaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    /**
     * Padrão Observer (mesmo molde do AlunoService.adicionarObserver): registra
     * quem deve ser notificado sempre que uma escola nova for cadastrada.
     */
    public void adicionarObserver(EscolaCadastradaObserver observer) {
        observers.add(observer);
    }

    /**
     * Verifica se o usuário autenticado é SUPER_ADMIN.
     * Lança AuthorizationException se não for.
     */
    private void verificarPermissaoMaster(AuthUser authUser) {
        if (authUser == null || authUser.getPerfil() != Perfil.SUPER_ADMIN) {
            logger.warn("Tentativa de acesso a CRUD de Escola sem permissão SUPER_ADMIN. Perfil: {}",
                    authUser != null ? authUser.getPerfil() : "NULL");
            throw new AuthorizationException("Acesso negado. Apenas o Administrador Master pode gerenciar escolas.");
        }
    }

    /**
     * Valida os dados obrigatórios da atualização de escola.
     */
    private void validarDadosObrigatoriosAtualizacao(AtualizarEscolaDTO dto) {
        if (dto.getNome() != null && dto.getNome().isBlank()) {
            throw new ValidationException("Nome da escola não pode estar vazio.");
        }

        if (dto.getCnpj() != null && dto.getCnpj().isBlank()) {
            throw new ValidationException("CNPJ não pode estar vazio.");
        }
        if (dto.getCnpj() != null && !dto.getCnpj().isBlank()) {
            ValidationUtil.validarCnpjComStrategy(dto.getCnpj());
        }

        if (dto.getEmailInstitucional() != null && dto.getEmailInstitucional().isBlank()) {
            throw new ValidationException("E-mail institucional não pode estar vazio.");
        }

        if (dto.getTelefone() != null && dto.getTelefone().isBlank()) {
            throw new ValidationException("Telefone não pode estar vazio.");
        }

        if (dto.getCidade() != null && dto.getCidade().isBlank()) {
            throw new ValidationException("Cidade não pode estar vazia.");
        }

        if (dto.getEstado() != null && dto.getEstado().isBlank()) {
            throw new ValidationException("Estado não pode estar vazio.");
        }

        if (dto.getCep() != null && dto.getCep().isBlank()) {
            throw new ValidationException("CEP não pode estar vazio.");
        }

        if (dto.getNomeResponsavel() != null && dto.getNomeResponsavel().isBlank()) {
            throw new ValidationException("Nome do responsável não pode estar vazio.");
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank() &&
                !dto.getStatus().equals("ATIVA") && !dto.getStatus().equals("INATIVA")) {
            throw new ValidationException("Status deve ser ATIVA ou INATIVA.");
        }

        if (dto.getCodigoInep() != null && !dto.getCodigoInep().isBlank() && dto.getCodigoInep().length() != 8) {
            throw new ValidationException("Código INEP deve conter exatamente 8 caracteres.");
        }
        
        if ((dto.getLatitude() != null && !dto.getLatitude().isBlank()) || (dto.getLongitude() != null && !dto.getLongitude().isBlank())) {
            if (dto.getLatitude() == null || dto.getLatitude().isBlank() || dto.getLongitude() == null || dto.getLongitude().isBlank()) {
                throw new ValidationException("Latitude e Longitude devem ser informados juntos.");
            }
            try {
                double lat = Double.parseDouble(dto.getLatitude());
                double lon = Double.parseDouble(dto.getLongitude());
                if (lat < -90 || lat > 90) throw new ValidationException("Latitude deve estar entre -90 e 90.");
                if (lon < -180 || lon > 180) throw new ValidationException("Longitude deve estar entre -180 e 180.");
            } catch (NumberFormatException e) {
                throw new ValidationException("Latitude e Longitude devem ser numéricos válidos.");
            }
        }
        
        if (dto.getDataInicioAnoLetivo() != null && dto.getDataTerminoAnoLetivo() != null) {
            if (dto.getDataTerminoAnoLetivo().isBefore(dto.getDataInicioAnoLetivo())) {
                throw new ValidationException("Data de término do ano letivo não pode ser anterior à data de início.");
            }
        }
        
        List<String> situacoes = List.of("EM_ATIVIDADE", "PARALISADA", "EXTINTA");
        if (dto.getSituacaoFuncionamento() != null && !dto.getSituacaoFuncionamento().isBlank() && !situacoes.contains(dto.getSituacaoFuncionamento())) {
            throw new ValidationException("Situação de funcionamento inválida.");
        }
        
        List<String> dependencias = List.of("FEDERAL", "ESTADUAL", "MUNICIPAL", "PRIVADA");
        if (dto.getDependenciaAdministrativa() != null && !dto.getDependenciaAdministrativa().isBlank() && !dependencias.contains(dto.getDependenciaAdministrativa())) {
            throw new ValidationException("Dependência administrativa inválida.");
        }
        
        List<String> zonas = List.of("URBANA", "RURAL");
        if (dto.getZona() != null && !dto.getZona().isBlank() && !zonas.contains(dto.getZona())) {
            throw new ValidationException("Zona inválida.");
        }
        
        List<String> linguas = List.of("PORTUGUESA", "INDIGENA", "BILINGUE");
        if (dto.getLinguaMinistrada() != null && !dto.getLinguaMinistrada().isBlank() && !linguas.contains(dto.getLinguaMinistrada())) {
            throw new ValidationException("Língua ministrada inválida.");
        }

    }

    /**
     * Cadastra uma nova escola.
     */
    public CriarEscolaResponseDTO cadastrarEscola(CriarEscolaDTO dto, AuthUser authUser) {

        verificarPermissaoMaster(authUser);

        validarDados(dto);

        if (dto.getCpfResponsavel() == null || dto.getCpfResponsavel().isBlank()) {
            throw new ValidationException("CPF do responsável (futuro Gestor) é obrigatório.");
        }
        ValidationUtil.validarCpfComStrategy(dto.getCpfResponsavel());

        if (escolaRepository.findByCnpj(dto.getCnpj()).isPresent()) {
            throw new ConflictException("CNPJ já cadastrado.");
        }
        if (usuarioRepository.existsByEmail(dto.getEmailResponsavel())) {
            throw new ConflictException("Já existe um usuário cadastrado com o e-mail do responsável.");
        }

        Escola escola = new Escola();
        escola.setNome(dto.getNome());
        escola.setCnpj(dto.getCnpj());
        escola.setEmailInstitucional(dto.getEmailInstitucional());
        escola.setTelefone(dto.getTelefone());
        escola.setEndereco(dto.getEndereco());
        escola.setNumero(dto.getNumero());
        escola.setComplemento(dto.getComplemento());
        escola.setBairro(dto.getBairro());
        escola.setCep(dto.getCep());
        escola.setCidade(dto.getCidade());
        escola.setEstado(dto.getEstado());
        escola.setNomeResponsavel(dto.getNomeResponsavel());
        escola.setTelefoneResponsavel(dto.getTelefoneResponsavel());
        escola.setEmailResponsavel(dto.getEmailResponsavel());
        escola.setStatus("ATIVA");
        escola.setCodigoInep(dto.getCodigoInep());
        escola.setSituacaoFuncionamento(dto.getSituacaoFuncionamento());
        escola.setDataInicioAnoLetivo(dto.getDataInicioAnoLetivo());
        escola.setDataTerminoAnoLetivo(dto.getDataTerminoAnoLetivo());
        escola.setLatitude(dto.getLatitude());
        escola.setLongitude(dto.getLongitude());
        escola.setZona(dto.getZona());
        escola.setLocalizacaoDiferenciada(dto.getLocalizacaoDiferenciada());
        escola.setDependenciaAdministrativa(dto.getDependenciaAdministrativa());
        escola.setRegulamentacaoNumero(dto.getRegulamentacaoNumero());
        escola.setRegulamentacaoData(dto.getRegulamentacaoData());
        escola.setInfraAgua(dto.getInfraAgua());
        escola.setInfraEnergia(dto.getInfraEnergia());
        escola.setInfraEsgoto(dto.getInfraEsgoto());
        escola.setInfraLixo(dto.getInfraLixo());
        escola.setQtdComputadores(dto.getQtdComputadores());
        escola.setTemInternet(dto.isTemInternet());
        escola.setTipoBandaLarga(dto.getTipoBandaLarga());
        escola.setLinguaMinistrada(dto.getLinguaMinistrada());


        Escola escolaSalva = escolaRepository.save(escola);

        // Cria o primeiro Gestor da escola já ativo — é o Super Admin quem está
        // autorizando isso ao criar a escola, então não passa pelo fluxo de
        // auto-cadastro público (que nunca oferece o perfil GESTOR).
        String senhaGerada = gerarSenhaTemporaria();

        Usuario gestor = new Usuario();
        gestor.setNomeCompleto(dto.getNomeResponsavel());
        gestor.setCpf(dto.getCpfResponsavel());
        gestor.setEmail(dto.getEmailResponsavel().trim().toLowerCase());
        gestor.setTelefone(dto.getTelefoneResponsavel());
        gestor.setSenhaHash(passwordService.hash(senhaGerada));
        gestor.setPerfil(Perfil.GESTOR);
        gestor.setTenantId(escolaSalva.getId());
        gestor.setEscolaId(escolaSalva.getId());
        gestor.setAtivo(true);
        gestor.setBloqueado(false);
        gestor.setTentativasLogin(0);
        gestor.setCriadoEm(LocalDateTime.now());
        gestor.setAtualizadoEm(LocalDateTime.now());

        usuarioRepository.save(gestor, escolaSalva.getId());

        logger.info("Escola '{}' cadastrada com Gestor inicial '{}' (id: {}).",
                escolaSalva.getNome(), gestor.getEmail(), gestor.getId());

        // Padrão Observer: notifica quem estiver registrado (auditoria,
        // e-mail com credenciais, etc.) sem o EscolaService precisar
        // conhecer os detalhes de cada reação.
        for (EscolaCadastradaObserver observer : observers) {
            try {
                observer.aoCadastrarEscola(escolaSalva, gestor, senhaGerada);
            } catch (Exception e) {
                // Um observador falhar não pode derrubar o cadastro da escola,
                // que já foi salvo no banco antes desta notificação.
                logger.error("Observador {} falhou ao processar cadastro de escola: {}",
                        observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        return new CriarEscolaResponseDTO(escolaSalva, gestor.getEmail(), gestor.getCpf(), senhaGerada);
    }

    /**
     * Gera uma senha temporária que já atende a política de complexidade
     * (maiúscula, minúscula, número, caractere especial, 12 caracteres).
     * O Super Admin repassa essa senha ao Gestor, que deve trocá-la no
     * primeiro acesso (ver observação no controller/response).
     */
    private String gerarSenhaTemporaria() {
        String maiusculas = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String minusculas = "abcdefghijkmnpqrstuvwxyz";
        String numeros = "23456789";
        String especiais = "!@#$%&*";
        String todos = maiusculas + minusculas + numeros + especiais;

        StringBuilder senha = new StringBuilder();
        senha.append(maiusculas.charAt(RANDOM.nextInt(maiusculas.length())));
        senha.append(minusculas.charAt(RANDOM.nextInt(minusculas.length())));
        senha.append(numeros.charAt(RANDOM.nextInt(numeros.length())));
        senha.append(especiais.charAt(RANDOM.nextInt(especiais.length())));
        for (int i = 0; i < 8; i++) {
            senha.append(todos.charAt(RANDOM.nextInt(todos.length())));
        }

        // embaralha pra não ficar previsível (maiúscula sempre na posição 0, etc.)
        List<Character> caracteres = new java.util.ArrayList<>();
        for (char c : senha.toString().toCharArray()) caracteres.add(c);
        java.util.Collections.shuffle(caracteres, RANDOM);
        StringBuilder embaralhada = new StringBuilder();
        caracteres.forEach(embaralhada::append);
        return embaralhada.toString();
    }

    /**
     * Atualiza uma escola existente.
     */
    public Escola atualizarEscola(UUID escolaId, AtualizarEscolaDTO dto, AuthUser authUser) {
        verificarPermissaoMaster(authUser);
        validarDadosObrigatoriosAtualizacao(dto);

        Escola escola = escolaRepository.findById(escolaId)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada."));

        // Se CNPJ foi alterado, verifica se já existe outro com o mesmo CNPJ
        if (dto.getCnpj() != null && !dto.getCnpj().equals(escola.getCnpj())) {
            Optional<Escola> escolaComMesmoCnpj = escolaRepository.findByCnpj(dto.getCnpj());
            if (escolaComMesmoCnpj.isPresent()) {
                logger.warn("Tentativa de atualizar CNPJ duplicado. Novo CNPJ: {}", dto.getCnpj());
                throw new ConflictException("Já existe uma escola cadastrada com este CNPJ.");
            }
        }

        // Atualiza apenas os campos fornecidos
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            escola.setNome(dto.getNome());
        }
        if (dto.getCnpj() != null && !dto.getCnpj().isBlank()) {
            escola.setCnpj(dto.getCnpj());
        }
        if (dto.getEmailInstitucional() != null && !dto.getEmailInstitucional().isBlank()) {
            escola.setEmailInstitucional(dto.getEmailInstitucional());
        }
        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            escola.setTelefone(dto.getTelefone());
        }
        if (dto.getEndereco() != null && !dto.getEndereco().isBlank()) {
            escola.setEndereco(dto.getEndereco());
        }
        if (dto.getNumero() != null && !dto.getNumero().isBlank()) {
            escola.setNumero(dto.getNumero());
        }
        if (dto.getComplemento() != null) {
            escola.setComplemento(dto.getComplemento());
        }
        if (dto.getBairro() != null && !dto.getBairro().isBlank()) {
            escola.setBairro(dto.getBairro());
        }
        if (dto.getCidade() != null && !dto.getCidade().isBlank()) {
            escola.setCidade(dto.getCidade());
        }
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            escola.setEstado(dto.getEstado());
        }
        if (dto.getCep() != null && !dto.getCep().isBlank()) {
            escola.setCep(dto.getCep());
        }
        if (dto.getNomeResponsavel() != null && !dto.getNomeResponsavel().isBlank()) {
            escola.setNomeResponsavel(dto.getNomeResponsavel());
        }
        if (dto.getTelefoneResponsavel() != null) {
            escola.setTelefoneResponsavel(dto.getTelefoneResponsavel());
        }
        if (dto.getEmailResponsavel() != null) {
            escola.setEmailResponsavel(dto.getEmailResponsavel());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            escola.setStatus(dto.getStatus());
        }
        if (dto.getCodigoInep() != null) escola.setCodigoInep(dto.getCodigoInep());
        if (dto.getSituacaoFuncionamento() != null) escola.setSituacaoFuncionamento(dto.getSituacaoFuncionamento());
        if (dto.getDataInicioAnoLetivo() != null) escola.setDataInicioAnoLetivo(dto.getDataInicioAnoLetivo());
        if (dto.getDataTerminoAnoLetivo() != null) escola.setDataTerminoAnoLetivo(dto.getDataTerminoAnoLetivo());
        if (dto.getLatitude() != null) escola.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) escola.setLongitude(dto.getLongitude());
        if (dto.getZona() != null) escola.setZona(dto.getZona());
        if (dto.getLocalizacaoDiferenciada() != null) escola.setLocalizacaoDiferenciada(dto.getLocalizacaoDiferenciada());
        if (dto.getDependenciaAdministrativa() != null) escola.setDependenciaAdministrativa(dto.getDependenciaAdministrativa());
        if (dto.getRegulamentacaoNumero() != null) escola.setRegulamentacaoNumero(dto.getRegulamentacaoNumero());
        if (dto.getRegulamentacaoData() != null) escola.setRegulamentacaoData(dto.getRegulamentacaoData());
        if (dto.getInfraAgua() != null) escola.setInfraAgua(dto.getInfraAgua());
        if (dto.getInfraEnergia() != null) escola.setInfraEnergia(dto.getInfraEnergia());
        if (dto.getInfraEsgoto() != null) escola.setInfraEsgoto(dto.getInfraEsgoto());
        if (dto.getInfraLixo() != null) escola.setInfraLixo(dto.getInfraLixo());
        if (dto.getQtdComputadores() >= 0) escola.setQtdComputadores(dto.getQtdComputadores());
        escola.setTemInternet(dto.isTemInternet());
        if (dto.getTipoBandaLarga() != null) escola.setTipoBandaLarga(dto.getTipoBandaLarga());
        if (dto.getLinguaMinistrada() != null) escola.setLinguaMinistrada(dto.getLinguaMinistrada());


        escolaRepository.update(escola);

        logger.info("Escola atualizada com sucesso: {} (ID: {})", escola.getNome(), escola.getId());

        return escola;
    }

    /**
     * Busca uma escola por ID.
     */
    public Escola buscarEscolaPorId(UUID id, AuthUser authUser) {
        verificarPermissaoMaster(authUser);
        return escolaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada."));
    }

    /**
     * Lista todas as escolas.
     */
    public PaginaEscolasDTO listarPaginadas(String search, EscolaStatus status, int page, int size,
                                            AuthUser authUser) {
        verificarPermissaoMaster(authUser);
        size = Math.max(1, Math.min(size, 100));
        // Limita a página ao maior offset representável pelo contrato JDBC do repository.
        page = (int) Math.min(Math.max(1L, page), Integer.MAX_VALUE / (long) size + 1);
        int offset = (int) ((page - 1L) * size);
        String filtro = search == null || search.isBlank() ? null : search.trim();
        long total = escolaRepository.countFiltered(filtro, status);
        long totalPages = total / size + (total % size == 0 ? 0 : 1);
        List<Escola> itens = escolaRepository.findAllPaginated(filtro, status, size, offset);
        return new PaginaEscolasDTO(itens, page, size, total, totalPages,
                page > 1, page < totalPages);
    }

    public List<Escola> listarTodas(AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        return escolaRepository.findAll();
    }

    /**
     * Lista apenas as escolas ativas.
     */
    public List<Escola> listarAtivas(AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        return escolaRepository.findAllAtivas();
    }

    /**
     * Lista apenas as escolas inativas.
     */
    public List<Escola> listarInativas(AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        return escolaRepository.findAllInativas();
    }

    /**
     * Busca uma escola por CNPJ.
     */
    public Escola buscarEscolaPorCnpj(String cnpj, AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        return escolaRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada com o CNPJ informado."));
    }

    /**
     * Ativa uma escola.
     */
    public Escola ativarEscola(UUID id, AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada."));

        if ("ATIVA".equals(escola.getStatus())) {
            throw new BusinessException("Essa escola já está ativa.");
        }

        escola.setStatus("ATIVA");
        escolaRepository.update(escola);

        logger.info("Escola ativada com sucesso: {} (ID: {})", escola.getNome(), escola.getId());

        return escola;
    }

    /**
     * Inativa uma escola.
     */
    public Escola inativarEscola(UUID id, AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada."));

        if ("INATIVA".equals(escola.getStatus())) {
            throw new BusinessException("Essa escola já está inativa.");
        }

        escola.setStatus("INATIVA");
        escolaRepository.update(escola);

        logger.info("Escola inativada com sucesso: {} (ID: {})", escola.getNome(), escola.getId());

        return escola;
    }

    /**
     * Exclui de fato a escola (hard delete) — diferente de inativarEscola(),
     * que só marca status = INATIVA. Trava de segurança dupla:
     * 1) só permite excluir se a escola já estiver INATIVA;
     * 2) bloqueia se houver aluno/turma/mensalidade vinculados, para não
     *    apagar silenciosamente dados acadêmicos/financeiros reais.
     * Remove primeiro os usuários do tenant (FK usuario.tenant_id -> escola.id)
     * e só então a escola.
     */
    public void excluirEscola(UUID id, AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada."));

        if (!"INATIVA".equals(escola.getStatus())) {
            throw new BusinessException("Só é possível excluir definitivamente uma escola que já esteja inativa. Inative-a primeiro.");
        }

        if (escolaRepository.possuiDadosVinculados(escola.getId())) {
            throw new BusinessException("Não é possível excluir: existem alunos, turmas ou mensalidades vinculados a esta escola. " +
                    "Excluir apagaria esses dados de forma definitiva e irreversível.");
        }

        usuarioRepository.deleteAllByTenant(escola.getId());
        escolaRepository.delete(escola.getId());

        logger.warn("Escola excluida DEFINITIVAMENTE: {} (ID: {}) por {}", escola.getNome(), escola.getId(), authUser.getCpf());
    }
    /**
     * Exclui uma escola (soft delete: status → EXCLUIDA).
     * Apenas SUPER_ADMIN pode executar esta operação.
     */
    public void deletarEscola(UUID id, AuthUser authUser) {
        verificarPermissaoMaster(authUser);

        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada."));

        if ("EXCLUIDA".equals(escola.getStatus())) {
            throw new BusinessException("Esta escola já foi excluída.");
        }

        // Inativa todos os usuários vinculados antes de excluir a escola
        List<Usuario> usuarios = usuarioRepository.findAll()
                .stream()
                .filter(u -> id.equals(u.getEscolaId()))
                .toList();

        for (Usuario u : usuarios) {
            if (u.isAtivo()) {
                usuarioRepository.inactivate(u.getId(), u.getTenantId());
            }
        }

        escolaRepository.delete(id);
        logger.info("Escola excluída: {} (ID: {}) por {}", escola.getNome(), id, authUser.getCpf());
    }

    private void validarDados(CriarEscolaDTO dto) {

        ValidationUtil.validateTamanho(dto.getNome(), 3, 150, "Nome da escola");

        if (dto.getCnpj() == null || dto.getCnpj().isBlank()) {
            throw new ValidationException("CNPJ é obrigatório.");
        }
        ValidationUtil.validarCnpjComStrategy(dto.getCnpj());

        ValidationUtil.validateEmail(dto.getEmailInstitucional());

        // Endereço completo — antes só existia checagem de "não vazio" para
        // alguns desses campos, e "endereco"/"bairro" nem eram exigidos.
        ValidationUtil.validateTamanho(dto.getEndereco(), 5, 200, "Endereço");
        ValidationUtil.validateTamanho(dto.getBairro(), 2, 100, "Bairro");
        ValidationUtil.validateTamanho(dto.getCidade(), 2, 100, "Cidade");
        ValidationUtil.validateUf(dto.getEstado());
        ValidationUtil.validateCep(dto.getCep());
        ValidationUtil.validateTamanho(dto.getNomeResponsavel(), 5, 150, "Nome do responsável");

        if (dto.getCodigoInep() != null && !dto.getCodigoInep().isBlank() && dto.getCodigoInep().length() != 8) {
            throw new ValidationException("Código INEP deve conter exatamente 8 caracteres.");
        }
        
        if ((dto.getLatitude() != null && !dto.getLatitude().isBlank()) || (dto.getLongitude() != null && !dto.getLongitude().isBlank())) {
            if (dto.getLatitude() == null || dto.getLatitude().isBlank() || dto.getLongitude() == null || dto.getLongitude().isBlank()) {
                throw new ValidationException("Latitude e Longitude devem ser informados juntos.");
            }
            try {
                double lat = Double.parseDouble(dto.getLatitude());
                double lon = Double.parseDouble(dto.getLongitude());
                if (lat < -90 || lat > 90) throw new ValidationException("Latitude deve estar entre -90 e 90.");
                if (lon < -180 || lon > 180) throw new ValidationException("Longitude deve estar entre -180 e 180.");
            } catch (NumberFormatException e) {
                throw new ValidationException("Latitude e Longitude devem ser numéricos válidos.");
            }
        }
        
        if (dto.getDataInicioAnoLetivo() != null && dto.getDataTerminoAnoLetivo() != null) {
            if (dto.getDataTerminoAnoLetivo().isBefore(dto.getDataInicioAnoLetivo())) {
                throw new ValidationException("Data de término do ano letivo não pode ser anterior à data de início.");
            }
        }
        
        List<String> situacoes = List.of("EM_ATIVIDADE", "PARALISADA", "EXTINTA");
        if (dto.getSituacaoFuncionamento() != null && !dto.getSituacaoFuncionamento().isBlank() && !situacoes.contains(dto.getSituacaoFuncionamento())) {
            throw new ValidationException("Situação de funcionamento inválida.");
        }
        
        List<String> dependencias = List.of("FEDERAL", "ESTADUAL", "MUNICIPAL", "PRIVADA");
        if (dto.getDependenciaAdministrativa() != null && !dto.getDependenciaAdministrativa().isBlank() && !dependencias.contains(dto.getDependenciaAdministrativa())) {
            throw new ValidationException("Dependência administrativa inválida.");
        }
        
        List<String> zonas = List.of("URBANA", "RURAL");
        if (dto.getZona() != null && !dto.getZona().isBlank() && !zonas.contains(dto.getZona())) {
            throw new ValidationException("Zona inválida.");
        }
        
        List<String> linguas = List.of("PORTUGUESA", "INDIGENA", "BILINGUE");
        if (dto.getLinguaMinistrada() != null && !dto.getLinguaMinistrada().isBlank() && !linguas.contains(dto.getLinguaMinistrada())) {
            throw new ValidationException("Língua ministrada inválida.");
        }


        if (dto.getNumero() != null && dto.getNumero().length() > 10) {
            throw new ValidationException("Número do endereço deve ter no máximo 10 caracteres.");
        }
        if (dto.getComplemento() != null && dto.getComplemento().length() > 100) {
            throw new ValidationException("Complemento deve ter no máximo 100 caracteres.");
        }
    }
}
