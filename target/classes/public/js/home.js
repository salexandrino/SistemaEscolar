const themeToggle = document.getElementById('themeToggle');
const themeIcon = themeToggle?.querySelector('i');
const savedTheme = localStorage.getItem('synge-theme');

function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;
    if (themeIcon) {
        themeIcon.className = theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
    }
}

applyTheme(savedTheme || 'light');

themeToggle?.addEventListener('click', () => {
    const nextTheme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
    applyTheme(nextTheme);
    localStorage.setItem('synge-theme', nextTheme);
});

const counters = document.querySelectorAll('.counter');
const counterObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
        if (!entry.isIntersecting) {
            return;
        }

        const counter = entry.target;
        const target = Number(counter.dataset.target);
        const suffix = counter.dataset.suffix || '';
        const hasDecimal = target % 1 !== 0;
        const steps = 70;
        const increment = target / steps;
        let current = 0;

        const timer = setInterval(() => {
            current += increment;
            const finished = current >= target;
            const value = finished ? target : current;
            const formatted = hasDecimal
                ? value.toFixed(1)
                : `+${Math.round(value).toLocaleString('pt-BR')}`;

            counter.textContent = `${formatted}${suffix}`;

            if (finished) {
                clearInterval(timer);
            }
        }, 18);

        observer.unobserve(counter);
    });
}, { threshold: 0.35 });

counters.forEach(counter => counterObserver.observe(counter));
