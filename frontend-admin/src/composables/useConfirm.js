import { reactive } from 'vue'

export const confirmState = reactive({
  visible: false,
  title: '提示',
  message: '',
  confirmText: '确认',    // 新增：确认按钮文字，默认"确认"
  cancelText: '取消',     // 新增：取消按钮文字，默认"取消"
  resolve: null
})

// 增加 confirmText 和 cancelText 参数（带默认值，向后兼容）
export function showConfirm(title, message, confirmText = '确认', cancelText = '取消') {
  return new Promise((resolve) => {
    confirmState.title = title
    confirmState.message = message
    confirmState.confirmText = confirmText  
    confirmState.cancelText = cancelText    
    confirmState.visible = true
    confirmState.resolve = resolve
  })
}

export function handleConfirmOk() {
  confirmState.resolve?.(true)
  confirmState.visible = false
}

export function handleConfirmCancel() {
  confirmState.resolve?.(false)
  confirmState.visible = false
}