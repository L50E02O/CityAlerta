# Technical Debt

[MAD Architecture] ExploreViewModel, ReporteViewModel, ReportDetailViewModel y SearchReportViewModel aún no migran eventos de un solo disparo a SharedFlow — Impacto: Medio

[UI] Varias pantallas de perfil y explore siguen pasando ViewModel a composables hijos (p. ej. ExploreScreen, ReportDetailScreen) — Impacto: Medio

[Supabase Auth] No existe herramienta MCP `update_auth_config`; la confirmación de email debe verificarse manualmente en Authentication → Settings → Email del panel — Impacto: Alto

[Cobertura] Kover exige ≥90% pero gran parte del código legacy (repositorios remotos, pantallas) permanece sin tests de integración — Impacto: Medio

[SonorQube] Calificación A en Reliability/Security/Maintainability depende de ejecución en CI con `SONAR_TOKEN`; no verificada en este entorno local — Impacto: Bajo
