document.addEventListener('DOMContentLoaded', function() {

    // Função para aplicar máscara de CPF
    function applyCpfMask(input) {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, ''); // Remove tudo que não é dígito
            value = value.replace(/(\d{3})(\d)/, '$1.$2'); // Adiciona o primeiro ponto
            value = value.replace(/(\d{3})(\d)/, '$1.$2'); // Adiciona o segundo ponto
            value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2'); // Adiciona o hífen
            e.target.value = value;
        });
    }

    // Função para validar CPF
    function validateCpf(cpf) {
        cpf = cpf.replace(/\D/g, ''); // Remove caracteres não numéricos
        if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
            return false; // CPF deve ter 11 dígitos e não pode ser sequência de dígitos iguais
        }

        let sum = 0;
        let remainder;

        for (let i = 1; i <= 9; i++) {
            sum = sum + parseInt(cpf.substring(i - 1, i)) * (11 - i);
        }
        remainder = (sum * 10) % 11;

        if ((remainder === 10) || (remainder === 11)) {
            remainder = 0;
        }
        if (remainder !== parseInt(cpf.substring(9, 10))) {
            return false;
        }

        sum = 0;
        for (let i = 1; i <= 10; i++) {
            sum = sum + parseInt(cpf.substring(i - 1, i)) * (12 - i);
        }
        remainder = (sum * 10) % 11;

        if ((remainder === 10) || (remainder === 11)) {
            remainder = 0;
        }
        if (remainder !== parseInt(cpf.substring(10, 11))) {
            return false;
        }

        return true;
    }

    // Aplica a máscara a todos os inputs com a classe 'cpf-mask'
    document.querySelectorAll('.cpf-mask').forEach(function(input) {
        applyCpfMask(input);
    });

    // Exemplo de validação em um formulário (pode ser adaptado para o seu caso)
    const cpfInput = document.getElementById('cpf'); // Assumindo um input com id 'cpf'
    if (cpfInput) {
        cpfInput.addEventListener('blur', function() {
            if (!validateCpf(this.value)) {
                this.setCustomValidity('CPF inválido.');
                this.reportValidity();
            } else {
                this.setCustomValidity('');
            }
        });
    }

    // Função para aplicar máscara de telefone
    function applyPhoneMask(input) {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 10) {
                value = value.replace(/^(\d\d)(\d{5})(\d{4}).*/, '($1) $2-$3');
            } else if (value.length > 5) {
                value = value.replace(/^(\d\d)(\d{4})(\d{0,4}).*/, '($1) $2-$3');
            } else if (value.length > 2) {
                value = value.replace(/^(\d\d)(\d{0,5}).*/, '($1) $2');
            } else {
                value = value.replace(/^(\d*)/, '($1');
            }
            e.target.value = value;
        });
    }

    document.querySelectorAll('.phone-mask').forEach(function(input) {
        applyPhoneMask(input);
    });

    // Função para aplicar máscara de CEP
    function applyCepMask(input) {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = value.replace(/^(\d{5})(\d)/, '$1-$2');
            e.target.value = value;
        });
    }

    document.querySelectorAll('.cep-mask').forEach(function(input) {
        applyCepMask(input);
    });

    // Função para aplicar máscara de dinheiro (BRL)
    function applyMoneyMask(input) {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = (value / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
            e.target.value = value;
        });
    }

    document.querySelectorAll('.money-mask').forEach(function(input) {
        applyMoneyMask(input);
    });

    const toggleSenhaElements = document.querySelectorAll('[data-toggle-senha]');
    toggleSenhaElements.forEach(function(btn) {
        btn.addEventListener('click', function() {
            const targetId = btn.getAttribute('data-target');
            const input = document.getElementById(targetId);
            const icon = btn.querySelector('i');

            if (input && input.type === 'password') {
                input.type = 'text';
                icon.classList.replace('bi-eye-slash', 'bi-eye');

                setTimeout(function() {
                    input.type = 'password';
                    icon.classList.replace('bi-eye', 'bi-eye-slash');
                }, 3000);
            } else if (input) {
                input.type = 'password';
                icon.replace('bi-eye', 'bi-eye-slash');
            }
        });
    });

});
