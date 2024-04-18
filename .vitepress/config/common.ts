import type { DefaultTheme } from 'vitepress/theme'

import fs from 'fs'
import path from 'path'

export function getItem(dir: string, text: string): DefaultTheme.SidebarItem {
  return {
    text,
    link: `/posts/${dir}/${text}`,
  }
}

export function genSidebar(dir: string) {
  const filePath = path.resolve(process.cwd(), `docs/posts/${dir}`)
  const directorys = readDirectory(filePath).directories
  const result = [] as DefaultTheme.SidebarItem[]

  directorys.forEach((text) => {
    const fileNames = readDirectory(path.resolve(filePath, text)).files
    const filteredFiles = fileNames.filter((file) => isMarkDownFile(file))

    result.push({ text, items: [] })
    filteredFiles.forEach((file) => {
      const item = getItem(`${dir}/${text}`, file.slice(0, -3))

      result.at(-1)?.items?.push(item)
    })
  })

  return result
}

interface DirectoryContent {
  files: string[]
  directories: string[]
}

function isMarkDownFile(file: string) {
  return path.extname(file).toLowerCase() === '.md'
}

function readDirectory(dirPath: string): DirectoryContent {
  try {
    const files = fs.readdirSync(dirPath)
    const result: DirectoryContent = { files: [], directories: [] }

    files.forEach((file) => {
      const stat = fs.statSync(`${dirPath}/${file}`)

      if (stat.isFile()) {
        result.files.push(file)
      } else if (stat.isDirectory()) {
        result.directories.push(file)
      }
    })

    return result
  } catch (error) {
    console.error('Error reading directory:', error)
    return { files: [], directories: [] }
  }
}
