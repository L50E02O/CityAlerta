-- Tabla perfil_imagen (similar a reporte_imagen)
create table public.perfil_imagen (
    id uuid primary key default extensions.uuid_generate_v4(),
    perfil_id uuid references public.perfil(id) on delete cascade,
    storage_uuid uuid not null,
    url_path text not null,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

create unique index perfil_imagen_perfil_id_key on public.perfil_imagen (perfil_id);

alter table public.perfil_imagen enable row level security;

create policy "Allow insert perfil_imagen"
on public.perfil_imagen
for insert
to authenticated
with check (perfil_id = auth.uid());

create policy "Allow select perfil_imagen"
on public.perfil_imagen
for select
to authenticated
using (auth.uid() is not null);

create policy "perfil_imagen_usuarios_update"
on public.perfil_imagen
for update
to authenticated
using (perfil_id = auth.uid())
with check (perfil_id = auth.uid());

create policy "perfil_imagen_usuarios_delete"
on public.perfil_imagen
for delete
to authenticated
using (perfil_id = auth.uid());

-- Bucket privado para fotos de perfil
insert into storage.buckets (id, name, public)
values ('perfil_imagen', 'perfil_imagen', false)
on conflict (id) do nothing;

create policy "Allow insert perfil images"
on storage.objects
for insert
to authenticated
with check (bucket_id = 'perfil_imagen');

create policy "Allow select perfil images"
on storage.objects
for select
to authenticated
using (bucket_id = 'perfil_imagen' and auth.uid() is not null);

create policy "Allow update perfil images"
on storage.objects
for update
to authenticated
using (bucket_id = 'perfil_imagen')
with check (bucket_id = 'perfil_imagen');

create policy "Allow delete perfil images"
on storage.objects
for delete
to authenticated
using (bucket_id = 'perfil_imagen');
