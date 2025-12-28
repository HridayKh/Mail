import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        host: '127.0.0.1', // Change 'localhost' or empty to '127.0.0.1'
        hmr: {
            // This ensures Vite knows it's being proxied through Quarkus
            clientPort: 8080
        }
    }
})
