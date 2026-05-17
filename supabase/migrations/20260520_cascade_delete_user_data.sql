-- Al borrar perfil, borrar reportes del usuario (y en cascada reporte_imagen + reporte_ubicacion).
alter table public.reporte
drop constraint if exists reporte_usuario_id_fkey;

alter table public.reporte
add constraint reporte_usuario_id_fkey
foreign key (usuario_id)
references public.perfil(id)
on delete cascade;

-- El usuario autenticado puede borrar su propio perfil (edge function o cliente).
drop policy if exists "perfil_usuarios_delete" on public.perfil;

create policy "perfil_usuarios_delete"
on public.perfil
for delete
to authenticated
using (auth.uid() = id);
