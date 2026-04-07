import react from '@vitejs/plugin-react'
import type { IncomingMessage, ServerResponse } from 'node:http'
import type { Plugin } from 'vite'
import { defineConfig } from 'vite'

const proxyPrefix = '/__mystarnow_api'
const targetHeader = 'x-mystarnow-target'

async function readBody(req: IncomingMessage) {
  const chunks: Uint8Array[] = []
  for await (const chunk of req) {
    chunks.push(typeof chunk === 'string' ? Buffer.from(chunk) : chunk)
  }
  return chunks.length > 0 ? Buffer.concat(chunks) : undefined
}

function createDynamicProxyPlugin(): Plugin {
  async function proxyRequest(req: IncomingMessage, res: ServerResponse) {
    if (!req.url?.startsWith(proxyPrefix)) {
      return false
    }

    const targetBase =
      typeof req.headers[targetHeader] === 'string' && req.headers[targetHeader]
        ? req.headers[targetHeader]
        : 'http://localhost:8080'

    let targetUrl: URL
    try {
      targetUrl = new URL(req.url.slice(proxyPrefix.length) || '/', targetBase)
    } catch {
      res.statusCode = 400
      res.setHeader('Content-Type', 'application/json')
      res.end(JSON.stringify({ message: 'Invalid backend base URL.' }))
      return true
    }

    const headers = new Headers()
    for (const [key, value] of Object.entries(req.headers)) {
      if (!value || key.toLowerCase() === targetHeader || key.toLowerCase() === 'host') {
        continue
      }
      if (Array.isArray(value)) {
        value.forEach((entry) => headers.append(key, entry))
      } else {
        headers.set(key, value)
      }
    }

    const body = await readBody(req)

    const response = await fetch(targetUrl, {
      method: req.method,
      headers,
      body,
      duplex: body ? 'half' : undefined,
    } as RequestInit).catch((error) => {
      res.statusCode = 502
      res.setHeader('Content-Type', 'application/json')
      res.end(
        JSON.stringify({
          message:
            error instanceof Error ? error.message : 'Failed to proxy request.',
        }),
      )
      return null
    })

    if (!response) {
      return true
    }

    res.statusCode = response.status
    response.headers.forEach((value, key) => {
      if (key.toLowerCase() === 'content-encoding') return
      res.setHeader(key, value)
    })
    const arrayBuffer = await response.arrayBuffer()
    res.end(Buffer.from(arrayBuffer))
    return true
  }

  return {
    name: 'mystarnow-dynamic-proxy',
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        const handled = await proxyRequest(req, res)
        if (!handled) next()
      })
    },
    configurePreviewServer(server) {
      server.middlewares.use(async (req, res, next) => {
        const handled = await proxyRequest(req, res)
        if (!handled) next()
      })
    },
  }
}

export default defineConfig({
  plugins: [react(), createDynamicProxyPlugin()],
  server: {
    port: 5174,
    strictPort: true,
  },
  preview: {
    port: 4174,
    strictPort: true,
  },
})
