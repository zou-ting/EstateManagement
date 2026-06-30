import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Login from '../views/Login.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import Dashboard from '../views/Dashboard.vue'
import Buildings from '../views/Buildings.vue'
import Units from '../views/Units.vue'
import Owners from '../views/Owners.vue'
import Repairs from '../views/Repairs.vue'
import Profile from '../views/Profile.vue'
import PropertyFees from '../views/PropertyFees.vue'
import Announcements from '../views/Announcements.vue'
import Visitors from '../views/Visitors.vue'
import InspectionRecords from '../views/InspectionRecords.vue'
// ✅ 新增四个页面
import ParkingSpaces from '../views/ParkingSpaces.vue'
import Complaints from '../views/Complaints.vue'
import Facilities from '../views/Facilities.vue'
import Decorations from '../views/Decorations.vue'
import HousePurchases from '../views/HousePurchases.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: Dashboard },
        { path: 'buildings', component: Buildings },
        { path: 'units', component: Units },
        { path: 'owners', component: Owners },
        { path: 'repairs', component: Repairs },
        { path: 'inspection-records', component: InspectionRecords },
        { path: 'visitors', component: Visitors },
        { path: 'announcements', component: Announcements },
        { path: 'property-fees', component: PropertyFees },
        // ✅ 新增四个路由
        { path: 'parking', component: ParkingSpaces },
        { path: 'complaints', component: Complaints },
        { path: 'facilities', component: Facilities },
        { path: 'decoration', component: Decorations },
        { path: 'house-purchases', component: HousePurchases },
        { path: 'profile', component: Profile }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 如果访问登录页
  if (to.path === '/login') {
    if (userStore.user) return '/admin/dashboard'
    return true
  }

  // 未登录跳转登录
  if (!userStore.user) return '/login'

  // 加载菜单
  if (!userStore.menus.length) {
    await userStore.fetchMenus(userStore.user.role)
  }

  // 权限校验：获取带 /admin 前缀的菜单路径
  const allowed = userStore.getAdminMenuPaths()

  // 如果是 /admin 本身，放行
  if (to.path === '/admin') return true

  // 如果菜单还没加载完或没有菜单，放行（防止死循环）
  if (!allowed.length) return true

  // 检查当前路径是否在菜单中
  if (!allowed.includes(to.path)) {
    return allowed[0] || '/admin/dashboard'
  }
})

export default router