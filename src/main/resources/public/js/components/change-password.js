(function () {
    function parseMessage(payload, fallback) {
        return payload && payload.message ? payload.message : fallback;
    }

    async function readPayload(response) {
        try {
            return await response.json();
        } catch (error) {
            return {};
        }
    }

    function clearFields(fields) {
        fields.forEach((field) => {
            field.value = '';
            field.type = 'password';
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        const openButton = document.querySelector('[data-change-password-open]');
        const modalElement = document.querySelector('[data-change-password-modal]');
        const form = document.querySelector('[data-change-password-form]');
        if (!openButton || !modalElement || !form) return;

        const modal = window.bootstrap ? new window.bootstrap.Modal(modalElement) : null;
        const senhaAtual = form.querySelector('[name="senhaAtual"]');
        const novaSenha = form.querySelector('[name="novaSenha"]');
        const confirmacaoNovaSenha = form.querySelector('[name="confirmacaoNovaSenha"]');
        const submit = form.querySelector('[data-change-password-submit]');
        const label = form.querySelector('[data-submit-label]');
        const feedback = form.querySelector('[data-change-password-feedback]');
        const fields = [senhaAtual, novaSenha, confirmacaoNovaSenha];

        function setFeedback(message) {
            feedback.textContent = message || '';
            feedback.hidden = !message;
        }

        function validate() {
            const requiredFilled = fields.every((field) => field.value.length > 0);
            const matches = novaSenha.value === confirmacaoNovaSenha.value;
            submit.disabled = !requiredFilled || !matches;
            setFeedback(requiredFilled && !matches ? 'A confirmação precisa ser igual à nova senha.' : '');
        }

        openButton.addEventListener('click', () => {
            setFeedback('');
            validate();
            if (modal) {
                modal.show();
            }
        });

        modalElement.addEventListener('hidden.bs.modal', () => {
            clearFields(fields);
            setFeedback('');
            validate();
        });

        fields.forEach((field) => {
            field.addEventListener('input', validate);
        });

        form.querySelectorAll('[data-password-toggle]').forEach((button) => {
            button.addEventListener('click', () => {
                const target = document.getElementById(button.dataset.target);
                if (!target) return;
                const visible = target.type === 'text';
                target.type = visible ? 'password' : 'text';
                const icon = button.querySelector('i');
                if (icon) {
                    icon.classList.toggle('bi-eye', visible);
                    icon.classList.toggle('bi-eye-slash', !visible);
                }
            });
        });

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            validate();
            if (submit.disabled) return;

            submit.disabled = true;
            label.textContent = 'Salvando...';

            try {
                const response = await fetch('/auth/change-password', {
                    method: 'PATCH',
                    credentials: 'include',
                    headers: {
                        Accept: 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        senhaAtual: senhaAtual.value,
                        novaSenha: novaSenha.value,
                        confirmacaoNovaSenha: confirmacaoNovaSenha.value
                    })
                });
                const payload = await readPayload(response);

                if (!response.ok) {
                    const fallback = response.status >= 500
                        ? 'Não foi possível completar a ação.'
                        : 'Não foi possível alterar a senha.';
                    throw {
                        status: response.status,
                        message: parseMessage(payload, fallback)
                    };
                }

                window.showToast(parseMessage(payload, 'Senha alterada com sucesso.'), 'success');
                clearFields(fields);
                setFeedback('');
                if (modal) {
                    modal.hide();
                }
            } catch (error) {
                const status = error && error.status;
                const message = status === 500
                    ? 'Não foi possível completar a ação.'
                    : (error.message || 'Não foi possível alterar a senha.');
                clearFields(fields);
                setFeedback('');
                window.showToast(message, 'error');

                if (status === 401) {
                    window.setTimeout(() => {
                        window.location.href = '/super-admin/login';
                    }, 1200);
                }
            } finally {
                label.textContent = 'Salvar';
                validate();
            }
        });
    });
})();
