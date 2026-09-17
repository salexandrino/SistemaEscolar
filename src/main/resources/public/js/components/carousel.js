document.querySelectorAll('[data-k-carousel]').forEach((carousel) => {
    const slides = [...carousel.querySelectorAll('.k-carousel__slide')];
    const track = carousel.querySelector('.k-carousel__track');
    if (!track || slides.length < 2) return;
    let index = 0;
    const show = (next) => { index = (next + slides.length) % slides.length; track.style.transform = `translateX(-${index * 100}%)`; };
    carousel.querySelector('[data-k-carousel-prev]')?.addEventListener('click', () => show(index - 1));
    carousel.querySelector('[data-k-carousel-next]')?.addEventListener('click', () => show(index + 1));
});
