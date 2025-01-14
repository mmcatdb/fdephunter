import { fileURLToPath, URL } from 'url';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { createHtmlPlugin } from 'vite-plugin-html';

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
    const env = loadEnv(mode, process.cwd(), '');

    return {
        plugins: [
            react(),
            createHtmlPlugin({
                inject: {
                    data: {
                        app_version: process.env.npm_package_version,
                    },
                },
            }),
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
            },
        },
        server: {
            port: env.VITE_DEV_SERVER_PORT,
        },
    };
});
