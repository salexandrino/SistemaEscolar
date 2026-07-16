package br.com.synge.seguranca.services;

import br.com.synge.seguranca.dtos.AtualizarEscolaDTO;
import br.com.synge.seguranca.dtos.CriarEscolaDTO;
import br.com.synge.seguranca.dtos.CriarEscolaResponseDTO;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.exceptions.BusinessException;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.EscolaRepository;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.utils.ValidationUtil;
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
    private static final SecureRandom RANDOM = new SecureRandom();

    public EscolaService(EscolaRepository escolaRepository, UsuarioRepository usuarioRepository, PasswordService passwordService) {
        this.escolaRepository = escolaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
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
        escola.setCep(dto.getCep());
        escola.setCidade(dto.getCidade());
        escola.setEstado(dto.getEstado());
        escola.setNomeResponsavel(dto.getNomeResponsavel());
        escola.setTelefoneResponsavel(dto.getTelefoneResponsavel());
        escola.setEmailResponsavel(dto.getEmailResponsavel());
        escola.setStatus("ATIVA");

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

        if (dto.getNumero() != null && dto.getNumero().length() > 10) {
            throw new ValidationException("Número do endereço deve ter no máximo 10 caracteres.");
        }
        if (dto.getComplemento() != null && dto.getComplemento().length() > 100) {
            throw new ValidationException("Complemento deve ter no máximo 100 caracteres.");
        }
    }
}