create index if not exists reporte_usuario_id_estado_idx
on public.reporte using btree (usuario_id, estado);

create or replace view public.perfil_resumen
with (security_invoker = true)
as
select
    p.id,
    p.nombre_completo,
    p.rol_slug,
    p.activo,
    p.ciudad_id,
    coalesce(count(r.id), 0)::integer as total_reportes,
    coalesce(count(r.id) filter (where r.estado = 'RESUELTO'::reporte_estado), 0)::integer as reportes_resueltos
from public.perfil p
left join public.reporte r
    on r.usuario_id = p.id
where p.id = auth.uid()
group by
    p.id,
    p.nombre_completo,
    p.rol_slug,
    p.activo,
    p.ciudad_id;

grant select on public.perfil_resumen to authenticated;