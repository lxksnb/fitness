/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '*.css' {
  const content: undefined
  export default content
}

declare module '*.scss' {
  const content: undefined
  export default content
}

declare module 'element-plus/dist/locale/zh-cn.mjs'
