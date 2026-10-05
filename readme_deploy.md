# Guía de Despliegue (Deploy)

Este documento detalla los pasos y configuraciones necesarias para desplegar el **backend** en **Railway** y el **frontend** en **Vercel**.

---

## 1. Backend - Railway

Railway es ideal para desplegar servicios backend (Node.js, Python, Go, etc.) y bases de datos.

### Requisitos previos
1. Tener una cuenta en [Railway.app](https://railway.app/).
2. Instalar la CLI de Railway (opcional, para despliegues locales) o conectar tu repositorio de GitHub.

### Pasos para el despliegue
1. Ve a tu panel de Railway y selecciona **New Project**.
2. Selecciona **Deploy from GitHub repo** y elige tu repositorio.
3. Selecciona la rama de la que deseas desplegar (se recomienda `main` o `desarrollo`).
4. Railway detectará automáticamente el entorno (por ejemplo, si hay un `package.json` o un `Dockerfile`).

### Variables de Entorno en Railway
Configura las siguientes variables de entorno en la pestaña **Variables** de tu servicio:

| Variable | Descripción | Ejemplo |
| -------- | ----------- | ------- |
| `PORT` | Puerto donde corre la app (Railway lo asigna automáticamente) | `8080` |
| `NODE_ENV` | Entorno de ejecución | `production` |
| `DATABASE_URL` | URI de conexión a la base de datos (si aplica) | `postgresql://user:pass@host:port/db` |
| `FRONTEND_URL` | URL del frontend en Vercel para configurar CORS | `https://tu-app.vercel.app` |

---

## 2. Frontend - Vercel

Vercel es la plataforma recomendada para frameworks de frontend (React, Vue, Svelte, Next.js, etc.).

### Requisitos previos
1. Tener una cuenta en [Vercel](https://vercel.com/).
2. Conectar tu cuenta de GitHub con Vercel.

### Pasos para el despliegue
1. En el dashboard de Vercel, haz clic en **Add New...** → **Project**.
2. Importa el repositorio de tu proyecto.
3. En la configuración del proyecto, define:
   - **Framework Preset**: Selecciona tu framework (ej. *Vite*, *Create React App*, *Next.js*).
   - **Root Directory**: Si el frontend está en una carpeta específica (ej. `/frontend`), indícala aquí.
   - **Build Command**: `npm run build` o `yarn build`.
   - **Output Directory**: `dist` (para Vite), `build` (para CRA) o `.next`.

### Variables de Entorno en Vercel
Configura las variables en **Project Settings** → **Environment Variables**:

| Variable | Descripción | Ejemplo |
| -------- | ----------- | ------- |
| `VITE_API_URL` o `NEXT_PUBLIC_API_URL` | URL de la API del backend en Railway | `https://tu-backend.up.railway.app` |

### Configuración para SPAs (React Router / Vue Router)
Si usas un Single Page Application (SPA) con rutas en el lado del cliente, añade un archivo `vercel.json` en la raíz de tu proyecto frontend para evitar errores `404` al recargar páginas:

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/" }
  ]
}
```

---

Con estos pasos tendrás tu backend desplegado en Railway y tu frontend en Vercel, listos para producción.