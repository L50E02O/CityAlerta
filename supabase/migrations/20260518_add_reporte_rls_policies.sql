-- Habilitar Row Level Security (RLS) en la tabla reporte si no está habilitado
alter table public.reporte enable row level security;

-- Política para permitir que usuarios vean solo sus propios reportes
create policy "reporte_usuarios_select"
on public.reporte
for select
using (
    usuario_id = auth.uid()
    or exists (
        select 1 from public.perfil
        where id = auth.uid()
        and rol_slug in ('admin', 'moderador')
    )
);

-- Política para permitir que usuarios creen sus propios reportes
create policy "reporte_usuarios_insert"
on public.reporte
for insert
with check (usuario_id = auth.uid());

-- Política para permitir que usuarios actualicen solo sus propios reportes
create policy "reporte_usuarios_update"
on public.reporte
for update
using (usuario_id = auth.uid())
with check (usuario_id = auth.uid());

-- Política para permitir que usuarios eliminen solo sus propios reportes
create policy "reporte_usuarios_delete"
on public.reporte
for delete
using (usuario_id = auth.uid());

-- Política adicional: permitir que admins y moderadores vean todos los reportes
create policy "reporte_admin_moderador_select"
on public.reporte
for select
using (
    exists (
        select 1 from public.perfil
        where id = auth.uid()
        and rol_slug in ('admin', 'moderador')
    )
);

-- Política adicional: permitir que admins y moderadores actualicen estado de reportes
create policy "reporte_admin_moderador_update_estado"
on public.reporte
for update
using (
    exists (
        select 1 from public.perfil
        where id = auth.uid()
        and rol_slug in ('admin', 'moderador')
    )
)
with check (
    exists (
        select 1 from public.perfil
        where id = auth.uid()
        and rol_slug in ('admin', 'moderador')
    )
);
