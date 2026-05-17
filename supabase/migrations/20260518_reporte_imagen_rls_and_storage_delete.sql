-- Permite al dueño del reporte actualizar/eliminar filas de reporte_imagen
create policy "reporte_imagen_usuarios_update"
on public.reporte_imagen
for update
using (
  exists (
    select 1 from public.reporte r
    where r.id = reporte_imagen.reporte_id
      and r.usuario_id = auth.uid()
  )
)
with check (
  exists (
    select 1 from public.reporte r
    where r.id = reporte_imagen.reporte_id
      and r.usuario_id = auth.uid()
  )
);

create policy "reporte_imagen_usuarios_delete"
on public.reporte_imagen
for delete
using (
  exists (
    select 1 from public.reporte r
    where r.id = reporte_imagen.reporte_id
      and r.usuario_id = auth.uid()
  )
);

-- Permite eliminar objetos del bucket de imágenes de reportes
create policy "Allow delete report images"
on storage.objects
for delete
to authenticated
using (bucket_id = 'report_imagen');
