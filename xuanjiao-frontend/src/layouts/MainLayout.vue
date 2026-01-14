<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">宣传教育平台</div>
      <el-menu :default-active="route.path" router>
        <template v-for="menu in menuList" :key="menu.id">
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="subMenu in menu.children"
              :key="subMenu.id"
              :index="subMenu.path"
            >
              <el-icon v-if="subMenu.icon"><component :is="subMenu.icon" /></el-icon>
              <span>{{ subMenu.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span></span>
        <el-dropdown @command="handleCommand">
          <span class="user-info">{{ userStore.userInfo?.realName || '用户' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentMenus } from '@/api/menu'
import {
  Picture, Setting, Document, List,
  OfficeBuilding, User, Key, Menu as MenuIcon
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuList = ref<any[]>([])

// 图标映射
const iconMap: Record<string, any> = {
  'Picture': Picture,
  'Setting': Setting,
  'Document': Document,
  'List': List,
  'OfficeBuilding': OfficeBuilding,
  'User': User,
  'Key': Key,
  'Menu': MenuIcon
}

async function loadMenus() {
  try {
    const res = await getCurrentMenus()
    const menus = res.data || []
    // 转换图标名称为组件
    menuList.value = menus.map((menu: any) => ({
      ...menu,
      icon: iconMap[menu.icon] || MenuIcon,
      children: menu.children?.map((sub: any) => ({
        ...sub,
        icon: iconMap[sub.icon] || MenuIcon
      }))
    }))

    // 如果当前是根路径，重定向到用户有权限的第一个页面
    if (route.path === '/' && menus.length > 0) {
      const firstMenu = menus[0]
      if (firstMenu.children && firstMenu.children.length > 0) {
        router.push(firstMenu.children[0].path)
      } else {
        router.push(firstMenu.path)
      }
    }
  } catch (e) {
    console.error('加载菜单失败', e)
  }
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(loadMenus)
</script>

<style scoped>
.layout { height: 100%; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 18px; font-weight: bold; color: #409eff; }
.el-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.user-info { cursor: pointer; }
</style>
