# Guía de Contribución para CityAlerta

¡Gracias por tu interés en contribuir a CityAlerta! Este documento establece las pautas para colaborar de manera efectiva y mantener un alto estándar de calidad en la ingeniería del proyecto.

## Flujo de Trabajo (Git Workflow)

Utilizamos un enfoque basado en **Feature Branching**:

1. Haz un *Fork* del repositorio y clónalo localmente.
2. Crea una rama para tu contribución desde `main`:
   ```bash
   git checkout -b feature/nombre-de-tu-funcionalidad
   # o para correcciones
   git checkout -b bugfix/nombre-del-error
   ```
3. Desarrolla tu funcionalidad, asegurándote de seguir los principios de Clean Architecture y TDD descritos en el README.
4. Escribe y ejecuta los tests:
   ```bash
   ./gradlew test
   ```
5. Haz *Commit* de tus cambios siguiendo la convención de commits (ver más abajo).
6. Sube los cambios a tu fork y abre un **Pull Request (PR)** hacia la rama `main` de este repositorio.

## Convenciones de Commits

Utilizamos [Conventional Commits](https://www.conventionalcommits.org/). Esto nos permite autogenerar changelogs y mantener el historial limpio.

Formato:
```
<tipo>(<alcance opcional>): <descripción corta>
```

**Tipos permitidos:**
- `feat`: Nueva funcionalidad.
- `fix`: Corrección de un error.
- `docs`: Cambios exclusivos en la documentación.
- `style`: Cambios de formato (espacios, comas, etc.) que no afectan el código.
- `refactor`: Refactorización del código (sin añadir funcionalidades ni corregir errores).
- `test`: Añadir o corregir pruebas.
- `chore`: Tareas de mantenimiento, actualización de dependencias, etc.

**Ejemplo:**
`feat(reportes): agregar validación de IA para imágenes explícitas`

## Estándares de Código (Kotlin & Android)

- **Arquitectura:** Respeta las capas establecidas (UI/Presentación, Domain, Data). Evita acoplar la UI con la lógica de negocio o fuentes de datos.
- **Inmutabilidad:** Prefiere `val` sobre `var` y colecciones inmutables siempre que sea posible.
- **Corrutinas:** Utiliza `suspend functions` y `Flow` / `StateFlow` para el manejo asíncrono.
- **TDD:** Si añades un nuevo Use Case o ViewModel, incluye los tests unitarios correspondientes.

## Proceso de Code Review

Todo Pull Request será revisado bajo los siguientes criterios:
1. **Cobertura de tests:** ¿El nuevo código incluye tests? ¿Pasaron los tests existentes?
2. **Claridad y Mantenibilidad:** ¿El código es legible? ¿Cumple con principios SOLID?
3. **Documentación:** Si hay cambios estructurales, ¿el README ha sido actualizado?

¡Esperamos con entusiasmo tus contribuciones!
