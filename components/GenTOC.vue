<script setup lang="ts">
import { useData, withBase } from 'vitepress'

const { theme } = useData()
const sidebarMap = new Map()
const { dir } = defineProps({
  dir: String
})

Object.entries(theme.value.sidebar).forEach(([key, value]) =>
  sidebarMap.set(key, value)
)

</script>

<template>
  <div v-for="sidebar in sidebarMap.get(dir)">
    <h2 :id="sidebar.text" tabindex="-1">
      {{ sidebar.text }}
      <a
        class="header-anchor"
        :href="`#${sidebar.text}`"
        :aria-label="`Permalink to ${sidebar.text}`"
      ></a>
    </h2>
    <ul>
      <li v-for="item in sidebar.items">
        <a :href="withBase(item.link)">{{ item.text }}</a>
      </li>
    </ul>
  </div>
</template>
