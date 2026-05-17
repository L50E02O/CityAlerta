create policy "perfil_usuarios_update"
on public.perfil
for update
to authenticated
using (auth.uid() = id)
with check (auth.uid() = id);
