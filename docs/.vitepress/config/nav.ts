import type { DefaultTheme } from 'vitepress/theme'

export default [
  { text: '首页', link: '/' },
  { text: '前端', link: '/posts/frontend/' },
  { text: '后端', link: '/posts/backend/' },
  { text: '其他', link: '/posts/other/' },
] as DefaultTheme.NavItem[]
