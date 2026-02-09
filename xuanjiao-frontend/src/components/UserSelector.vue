<!-- 用户选择器组件 - 提供用户的多选功能，支持按角色和部门筛选 -->
<template>
  <div class="user-selector">
    <!-- 筛选区域 - 独立在外部 -->
    <div class="filter-section" v-if="showFilters">
      <div class="filter-row">
        <el-checkbox v-model="filters.useRole" @change="handleFilterChange">按角色</el-checkbox>
        <el-select
          v-if="filters.useRole"
          v-model="filters.roleId"
          size="small"
          placeholder="选择角色"
          clearable
          @change="handleFilterChange"
          style="width: 140px"
        >
          <el-option label="全部角色" :value="null" />
          <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
        </el-select>
      </div>
      <div class="filter-row">
        <el-checkbox v-model="filters.useDept" @change="handleFilterChange">按部门</el-checkbox>
        <el-select
          v-if="filters.useDept"
          v-model="filters.deptId"
          size="small"
          placeholder="选择部门"
          clearable
          filterable
          @change="handleFilterChange"
          style="width: 140px"
        >
          <el-option label="全部部门" :value="null" />
          <el-option
            v-for="dept in deptList"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </div>
    </div>

    <!-- 用户选择下拉框 - 使用本地筛选模式 -->
    <el-select
      v-model="selectedUsers"
      multiple
      filterable
      reserve-keyword
      :placeholder="placeholder"
      @change="handleChange"
      collapse-tags
      collapse-tags-tooltip
      style="width: 100%"
    >
      <el-option
        v-for="user in filteredUserList"
        :key="user.id"
        :label="user.displayName"
        :value="user.id"
      >
        <div class="user-option">
          <span class="user-name">{{ user.realName }}</span>
          <span class="user-username">({{ user.username }})</span>
          <span v-if="user.deptName" class="user-dept">{{ user.deptName }}</span>
        </div>
      </el-option>
      <template #footer>
        <div class="select-footer">
          <span v-if="filteredUserList.length === 0">暂无符合条件的用户</span>
          <span v-else>显示 {{ filteredUserList.length }} 位用户</span>
        </div>
      </template>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { searchUsers } from '@/api/user'
import { getRoleList } from '@/api/role'
import { getDeptTree } from '@/api/dept'

const props = withDefaults(
  defineProps<{
    modelValue: number[]
    showFilters?: boolean
    placeholder?: string
  }>(),
  {
    showFilters: true,
    placeholder: '搜索用户（姓名/用户名）'
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[]): void
}>()

const selectedUsers = ref<number[]>([])
const loading = ref(false)
const allUsers = ref<any[]>([])

const filters = reactive({
  useRole: false,
  roleId: null as number | null,
  useDept: false,
  deptId: null as number | null
})

// 角色列表
const roles = ref<any[]>([])

// 部门列表
const deptList = ref<any[]>([])

// 监听 props 变化
watch(() => props.modelValue, (newVal) => {
  selectedUsers.value = newVal || []
}, { immediate: true })

function handleChange() {
  emit('update:modelValue', selectedUsers.value)
}

// 计算属性：根据筛选条件过滤用户
const filteredUserList = computed(() => {
  let filtered = [...allUsers.value]

  // 角色筛选
  if (filters.useRole && filters.roleId) {
    filtered = filtered.filter(user => user.roleId === filters.roleId)
  }

  // 部门筛选
  if (filters.useDept && filters.deptId) {
    filtered = filtered.filter(user => user.deptId === filters.deptId)
  }

  return filtered
})

// 筛选条件变化时无需操作，计算属性会自动更新
function handleFilterChange() {
  // 计算属性会自动更新 filteredUserList
}

// 加载所有可选用户
async function loadAllUsers() {
  loading.value = true
  try {
    const res = await searchUsers({
      pageNum: 1,
      pageSize: 100
    })
    const users = res.data?.list || []
    // 添加 displayName 用于搜索显示
    allUsers.value = users.map((user: any) => ({
      ...user,
      displayName: `${user.realName} (${user.username}) - ${user.deptName || '无部门'}`
    }))
  } catch (e: any) {
    ElMessage.error(e.message || '加载用户失败')
  } finally {
    loading.value = false
  }
}

// 加载角色列表
async function loadRoles() {
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      roles.value = res.data || []
    }
  } catch (e: any) {
    console.error('加载角色列表失败:', e)
  }
}

// 加载部门树
async function loadDepts() {
  try {
    const res = await getDeptTree()
    if (res.code === 200) {
      const tree = res.data || []
      deptList.value = flattenDeptTree(tree)
    }
  } catch (e: any) {
    console.error('加载部门列表失败:', e)
  }
}

// 扁平化部门树
function flattenDeptTree(tree: any[], list: any[] = []): any[] {
  if (!tree || tree.length === 0) return list
  for (const dept of tree) {
    list.push(dept)
    if (dept.children && dept.children.length > 0) {
      flattenDeptTree(dept.children, list)
    }
  }
  return list
}

// 初始加载
onMounted(async () => {
  await Promise.all([
    loadRoles(),
    loadDepts(),
    loadAllUsers()
  ])
})
</script>

<style scoped>
.user-selector {
  width: 100%;
}

.filter-section {
  margin-bottom: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
}

.user-username {
  color: #909399;
  font-size: 12px;
}

.user-dept {
  color: #409eff;
  font-size: 12px;
}

.select-footer {
  padding: 8px 12px;
  text-align: center;
  color: #909399;
  font-size: 12px;
  border-top: 1px solid #e4e7ed;
}
</style>
