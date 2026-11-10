# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概况

Vite 8 + Vue 3.5 的纯前端 SPA。目前处于项目初始阶段：已接入 Vue Router（当前只有 `/back` 一个后端布局路由）和 Element Plus 2.x（全量注册，中文语言包），尚未实现"心理健康助手"的业务功能；没有状态管理、请求库，也没有 TypeScript、ESLint/Prettier 或测试框架。

## 常用命令

- `npm install` — 安装依赖
- `npm run dev` — 启动开发服务器（默认 http://localhost:5173，支持 HMR）
- `npm run build` — 生产构建，输出到 `dist/`
- `npm run preview` — 本地预览构建产物

`package.json` 中只有上述三个脚本，没有 lint / test 命令，也没有配置任何测试运行器；不要假设存在可运行的检查或测试。

## 代码结构

入口链路：`index.html` → `src/main.js`（`createApp`，注册 Element Plus 与 router）→ `src/App.vue`（`<router-view>`）→ `src/router/index.js` → 布局组件（如 `src/components/BackendLayout.vue`）。

- `src/style.css` — 唯一的样式文件，在 `main.js` 中全局导入。所有组件都不带 `<style>` 块，样式全部集中于此且为全局作用域；新增组件沿用此约定。
- `src/assets/` — 通过 `import` 引入、由 Vite 处理的资源（图片、SVG）。
- `public/` — 原样拷贝到根路径的静态资源，模板中以绝对路径引用，例如 `/icons.svg#documentation-icon`。
- `src/router/index.js` — 路由表，使用 `createWebHistory`；新增页面在此注册。
- `vite.config.js` — 注册 `@vitejs/plugin-vue`，并把 `@` 别名指向 `src`（`fileURLToPath(new URL('./src', import.meta.url))`）；项目内模块可用 `@/...` 或相对路径导入。注意项目是 ESM（`"type": "module"`），别名路径不要写成 `resolve(__dirname, '/src')` 这类带前导斜杠的绝对路径。

## 技术约定

- 组件一律使用 Vue 3 `<script setup>` 单文件组件，纯 JavaScript（无 TS）。
- Element Plus 在 `main.js` 中通过 `app.use(ElementPlus, { locale: zhCn })` 全量注册，模板中可直接使用 `<el-button>` 等组件，无需逐个导入；样式已引入 `element-plus/dist/index.css`。全量引入会使产物偏大（构建时约 1 MB JS），若后续对包体积敏感，可改为 `unplugin-vue-components` + `unplugin-auto-import` 按需引入。
- 样式基于 CSS 自定义属性（`:root` 中定义颜色、字体等变量），并通过 `@media (prefers-color-scheme: dark)` 切换暗色主题。
- `style.css` 使用了 CSS 原生嵌套（`&:hover`、`:root` 内嵌 `@media` 等），由 Vite 处理；新增样式可直接沿用嵌套写法。

## 注意事项

- 目录结构仅反映模板初始状态，不要据此推断存在业务分层（如 `views/`、`stores/`、`api/`）——目前都不存在。
- 引入 Pinia、Axios 等依赖时，需先安装并在 `main.js` 中注册；当前除 `/back` 外没有其他路由和页面。
