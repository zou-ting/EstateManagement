import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ClientLayout from '../layout/ClientLayout.vue'
import Home from '../views/Home.vue'
import RepairApply from '../views/RepairApply.vue'
import MyProperty from '../views/MyProperty.vue'
import Profile from '../views/Profile.vue'
import Announcements from '../views/Announcements.vue'
import VisitorApply from '../views/VisitorApply.vue'
import PayOnline from '../views/PayOnline.vue'
import PropertyFees from '../views/PropertyFees.vue'
import ComplaintApply from '../views/ComplaintApply.vue'
import MyParking from '../views/MyParking.vue'
import CommunityHouses from '../views/CommunityHouses.vue'
import MyHousePurchases from '../views/MyHousePurchases.vue'

const publicPaths = ['/login', '/register']

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    {
      path: '/',
      component: ClientLayout,
      children: [
        { path: 'home', component: Home },
        { path: 'repair', component: RepairApply },
        { path: 'my-property', component: MyProperty },
        { path: 'pay-online', component: PayOnline },
        { path: 'property-fees', component: PropertyFees },
        { path: 'complaint', component: ComplaintApply },
        { path: 'my-parking', component: MyParking },
        { path: 'community-houses', component: CommunityHouses },
        { path: 'my-house-purchases', component: MyHousePurchases },
        { path: 'profile', component: Profile },
        { path: 'announcements', component: Announcements },
        { path: 'visitor-apply', component: VisitorApply }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (publicPaths.includes(to.path)) {
    if (userStore.user) return '/home'
    return true
  }
  if (!userStore.user) return '/login'
  return true
})

export default router
