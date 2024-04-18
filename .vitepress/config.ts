import { defineConfig } from 'vitepress'
import sidebar from './config/sidebar'
import nav from './config/nav'

export default defineConfig({
  title: 'Coding Yuan',
  base: '/TechShare/',
  titleTemplate: "YuanCode's study notes",
  description: 'This is my study notes recording website.',
  cleanUrls: true,
  lastUpdated: true,

  head: [
    [
      'link',
      {
        rel: 'icon',
        type: 'image/x-icon',
        href: './logo.jpg'
      }
    ]
  ],

  themeConfig: {
    nav,
    sidebar,
    outlineTitle: '目录',
    socialLinks: [
      { icon: 'github', link: 'https://github.com/yuan686' }
    ],
    lastUpdated: {
      text: 'Updated at',
      formatOptions: {
        dateStyle: 'full',
        timeStyle: 'medium',
      },
    },
  }
})
