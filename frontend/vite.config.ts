import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      "/api": {
        target: "http://localhost:7000",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
        configure: (proxy) => {
          proxy.on("proxyReq", (proxyRequest) => {
            proxyRequest.removeHeader("origin");
          });
        },
      },
      "/socket.io": {
        target: "http://localhost:7001",
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
