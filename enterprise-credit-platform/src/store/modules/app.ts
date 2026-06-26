import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setSidebarCollapsed = (collapsed: boolean) => {
    sidebarCollapsed.value = collapsed
  }

  const setLoading = (isLoading: boolean) => {
    loading.value = isLoading
  }

  return {
    sidebarCollapsed,
    loading,
    toggleSidebar,
    setSidebarCollapsed,
    setLoading
  }
})
