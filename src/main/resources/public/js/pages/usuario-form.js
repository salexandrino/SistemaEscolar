(function () {
    function parseErrorMessage(response) {
        return response.json()
            .then((payload) => payload.message || 'Não foi possível salvar o usuário.')
            .catch(() => 'Não foi possível salvar o usuário.');
    }

    function setSubmitting(form, submitting) {
        const button = form.querySelector('button[type="submit"]');
        if (!button) return;
        button.disabled = submitting;
        button.setAttribute('aria-busy', String(submitting));
    }

    async function updateProfileIfNeeded(form, formData) {
        if (!form.dataset.profileEndpoint || !formData.get('perfil')) return;

        const response = await fetch(form.dataset.profileEndpoint, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ perfil: formData.get('perfil') })
        });

        if (!response.ok) {
            const message = await parseErrorMessage(response);
            throw new Error(message);
        }
    }

    async function submitForm(form) {
        setSubmitting(form, true);
        try {
            const formData = new FormData(form);
            const body = new URLSearchParams(formData);
            const response = await fetch(form.action, {
                method: form.dataset.method || (form.method || 'POST').toUpperCase(),
                credentials: 'include',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body
            });

            if (response.ok) {
                await updateProfileIfNeeded(form, formData);
                const param = form.dataset.successParam || 'atualizado';
                window.location.href = `/dashboard/usuarios?${param}=1`;
                return;
            }

            const message = await parseErrorMessage(response);
            window.showToast(message, 'error');
        } catch (error) {
            window.showToast(error.message || 'Não foi possível salvar o usuário.', 'error');
        } finally {
            setSubmitting(form, false);
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('[data-usuario-form]').forEach((form) => {
            form.addEventListener('submit', (event) => {
                event.preventDefault();
                submitForm(form);
            });
        });
    });
})();
