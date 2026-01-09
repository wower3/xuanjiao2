<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">宣传教育平台</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item index="/asset">
          <el-icon><Picture /></el-icon>
          <span>素材管理</span>
        </el-menu-item>
        <el-menu-item index="/workflow">
          <el-icon><Setting /></el-icon>
          <span>流程管理</span>
        </el-menu-item>
        <el-menu-item index="/approval">
          <el-icon><Document /></el-icon>
          <span>审批工单</span>
        </el-menu-item>
        <el-menu-item index="/log">
          <el-icon><List /></el-icon>
          <span>使用日志</span>
        </el-menu-item>
        <el-menu-item index="/system/dept">
          <el-icon><OfficeBuilding /></el-icon>
          <span>部门管理</span>
        </el-menu-item>
        <el-menu-item index="/system/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/system/role">
          <el-icon><Key /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
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
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100%; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 18px; font-weight: bold; color: #409eff; }
.el-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.user-info { cursor: pointer; }
</style>
