import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    proxy: {
      // Proxy /api calls to the remote PHP backend — eliminates CORS issues
      '/api-proxy': {
        target: 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/index.php',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api-proxy/, ''),
        timeout: 30000,
      },
    },
  },
})
