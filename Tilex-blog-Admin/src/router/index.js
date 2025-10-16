import { createRouter, createWebHistory } from 'vue-router'
import Upload from '../views/Upload.vue'

const routes = [
  { path: '/', redirect: '/upload' },
  { path: '/upload', component: Upload },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
