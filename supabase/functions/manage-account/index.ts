import { createClient } from 'npm:@supabase/supabase-js@2'

type ManageAccountBody = {
  mode?: 'update_email' | 'delete_account'
  email?: string
}

function json(status: number, body: Record<string, unknown>) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'authorization, apikey, content-type',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
    },
  })
}

function createUserClient(req: Request, supabaseUrl: string, anonKey: string) {
  const authorization = req.headers.get('Authorization') ?? ''
  return createClient(supabaseUrl, anonKey, {
    global: { headers: { Authorization: authorization } },
    auth: { persistSession: false, autoRefreshToken: false },
  })
}

async function collectStoragePaths(
  admin: ReturnType<typeof createClient>,
  userId: string
): Promise<{ reportePaths: string[]; perfilPaths: string[] }> {
  const reportePaths: string[] = []
  const perfilPaths: string[] = []

  const { data: reportes, error: reportesError } = await admin
    .from('reporte')
    .select('id')
    .eq('usuario_id', userId)

  if (reportesError) {
    throw new Error(reportesError.message)
  }

  const reporteIds = (reportes ?? []).map((row) => row.id as string)
  if (reporteIds.length > 0) {
    const { data: imagenes, error: imagenesError } = await admin
      .from('reporte_imagen')
      .select('storage_uuid')
      .in('reporte_id', reporteIds)

    if (imagenesError) {
      throw new Error(imagenesError.message)
    }

    for (const imagen of imagenes ?? []) {
      const storageUuid = (imagen.storage_uuid as string | null)?.trim()
      if (storageUuid) {
        reportePaths.push(storageUuid)
      }
    }
  }

  const { data: perfilImagen, error: perfilImagenError } = await admin
    .from('perfil_imagen')
    .select('storage_uuid')
    .eq('perfil_id', userId)
    .maybeSingle()

  if (perfilImagenError) {
    throw new Error(perfilImagenError.message)
  }

  const perfilStorageUuid = (perfilImagen?.storage_uuid as string | null)?.trim()
  if (perfilStorageUuid) {
    perfilPaths.push(perfilStorageUuid)
  }

  return { reportePaths, perfilPaths }
}

async function removeStorageObjects(
  admin: ReturnType<typeof createClient>,
  bucket: string,
  paths: string[]
) {
  if (paths.length === 0) {
    return
  }

  const chunkSize = 100
  for (let index = 0; index < paths.length; index += chunkSize) {
    const chunk = paths.slice(index, index + chunkSize)
    const { error } = await admin.storage.from(bucket).remove(chunk)
    if (error) {
      throw new Error(`No se pudo borrar archivos en ${bucket}: ${error.message}`)
    }
  }
}

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return json(200, { ok: true })
  }

  if (req.method !== 'POST') {
    return json(405, { error: 'Method not allowed' })
  }

  const supabaseUrl = Deno.env.get('SUPABASE_URL')
  const anonKey = Deno.env.get('SUPABASE_ANON_KEY')
  const serviceRoleKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

  if (!supabaseUrl || !anonKey || !serviceRoleKey) {
    return json(500, { error: 'Missing Supabase environment variables' })
  }

  let body: ManageAccountBody
  try {
    body = await req.json()
  } catch {
    return json(400, { error: 'Invalid JSON body' })
  }

  const mode = body.mode
  if (mode !== 'update_email' && mode !== 'delete_account') {
    return json(400, { error: 'mode must be update_email or delete_account' })
  }

  const userClient = createUserClient(req, supabaseUrl, anonKey)
  const {
    data: { user },
    error: userError,
  } = await userClient.auth.getUser()

  if (userError || !user) {
    return json(401, { error: 'Sesion invalida o expirada' })
  }

  const admin = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  })

  if (mode === 'update_email') {
    const email = body.email?.trim().toLowerCase() ?? ''
    if (!email || !email.includes('@')) {
      return json(400, { error: 'email is required' })
    }

    const { error: updateError } = await admin.auth.admin.updateUserById(user.id, {
      email,
      email_confirm: true,
    })

    if (updateError) {
      return json(400, { error: updateError.message })
    }

    return json(200, {
      ok: true,
      email,
      message: 'Correo actualizado correctamente',
    })
  }

  try {
    const { reportePaths, perfilPaths } = await collectStoragePaths(admin, user.id)
    await removeStorageObjects(admin, 'reporte_imagen', reportePaths)
    await removeStorageObjects(admin, 'perfil_imagen', perfilPaths)

    const { error: deletePerfilError } = await admin.from('perfil').delete().eq('id', user.id)
    if (deletePerfilError) {
      return json(500, { error: deletePerfilError.message })
    }

    const { error: deleteUserError } = await admin.auth.admin.deleteUser(user.id)
    if (deleteUserError) {
      return json(500, { error: deleteUserError.message })
    }

    return json(200, {
      ok: true,
      message: 'Cuenta eliminada correctamente',
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : 'No se pudo eliminar la cuenta'
    return json(500, { error: message })
  }
})
