# Design System Kutuar

O design system é uma camada incremental sobre Bootstrap 5. Todos os componentes próprios usam o prefixo `k-`; telas ainda não migradas continuam apoiadas por `style.css` e pelas classes Bootstrap.

## Estrutura

- `templates/components/`: fragments Thymeleaf de button, card, input, checkbox, dropdown, navbar e carousel.
- `public/css/tokens.css`: cores, tipografia, espaços, raios, sombras e transição.
- `public/css/base.css` e `layout.css`: base, AppShell e grid de 12 colunas.
- `public/css/components/`: estilos isolados por componente.
- `public/css/pages/dashboard.css`: estilos exclusivos do dashboard.
- `public/js/components/`: comportamento pequeno e independente.
- `templates/dev/components.html`: catálogo visual sem rota pública.

`style.css` permanece como agregador e contém o legado ainda necessário.

## Tokens

As cores principais são `--kutuar-primary` (`#004B1C`), `--kutuar-accent` (`#0DB04B`), `--kutuar-background`, `--kutuar-surface`, `--kutuar-text`, `--kutuar-muted`, `--kutuar-border`, `--kutuar-success`, `--kutuar-warning` e `--kutuar-danger`. Use os tokens de `--kutuar-space-1` a `-6`, `--kutuar-radius-sm/md/lg`, `--kutuar-shadow-sm/md`, `--kutuar-font-xs/sm/md/lg/xl` e `--kutuar-transition`; não repita cores Kutuar nos componentes.

## Componentes

### Button

Variantes: `primary`, `secondary`, `outline`, `danger`, `ghost`. Tamanhos: `sm`, `md`, `lg`. Aceita link ou botão, ícone, disabled e loading.

```html
<div th:replace="~{components/button :: button(
  'Salvar', 'bi-check-lg', 'primary', 'md', false, false, 'submit', null
)}"></div>
```

Para link, informe o último argumento: `'/dashboard/escolas/nova'`.

### Input e Checkbox

O input aceita label, name, id, type, value, placeholder, required, disabled, readonly, error, helpText e ícones. Em formulários com `th:field`, use a mesma estrutura e classes diretamente para preservar o binding.

```html
<div th:replace="~{components/input :: input('Nome', 'nome', 'nome', 'text', null,
  'Nome completo', true, false, false, null, null, 'bi-person', null)}"></div>
<div th:replace="~{components/checkbox :: checkbox('ativo', 'ativo', 'Ativo', true, false, 'true')}"></div>
```

### Dropdown

Use `.k-select` em seleções de formulário. Menus de ação combinam `.k-dropdown`, `.k-dropdown__trigger`, `.k-dropdown__menu` e `.k-dropdown__item`; Bootstrap mantém abertura e posicionamento.

### Card

Variantes base: `default`, `bordered`, `flat`, `interactive`. Use `k-stat-card`, `k-content-card`, `k-table-card`, `k-chart-card` e `k-action-card` como especializações leves. As regiões são `__header`, `__icon`, `__title`, `__subtitle`, `__actions`, `__body` e `__footer`.

### AppShell e grid

O layout administrativo usa `k-app-shell`, `k-sidebar`, `k-topbar` e `k-page`. O menu mobile preserva foco, Escape, backdrop e `aria-expanded`. O grid tem 12 colunas:

```html
<div class="k-grid k-grid--dashboard">
  <section class="k-col-8">...</section>
  <aside class="k-col-4">...</aside>
</div>
```

No dashboard, cada `.k-widget` pode declarar `data-widget-id`, `data-widget-type` e `data-widget-order`; a classe `k-col-*` representa `columnSpan`. Isso prepara reordenação futura sem persistência.

### Carousel

Use para avisos, novidades, onboarding e comunicados. Cada item é `.k-carousel__slide`; os controles usam os atributos `data-k-carousel-prev/next`.

## Estado da migração

Migrados: AppShell administrativo, sidebar, topbar, dashboard e lista de escolas. Os outros layouts, páginas CRUD, autenticação, portal, modais e seus scripts continuam legados de propósito e serão migrados gradualmente. Bootstrap 5 permanece disponível para grid utilitário, collapse, dropdown e modal durante a transição.
