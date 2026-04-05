import { defineConfig } from "vite";
import { resolve } from "path";

export default defineConfig({
  build: {
    outDir: "dist",
    rollupOptions: {
      input: {
        main: resolve(__dirname, "js/main.js"),
      },
      output: {
        entryFileNames: "bundle.js",
        format: "iife",
        name: "BlogApp",
      },
    },
    minify: "terser",
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
  },
  resolve: {
    alias: {
      "@": resolve(__dirname, "js"),
    },
  },
});
