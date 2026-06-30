/**
 * 确认弹窗
 * @param {string} title - 标题
 * @param {string} message - 提示内容
 * @returns {Promise<boolean>} - 返回用户是否确认
 */
export function showConfirm(title, message) {
  return new Promise((resolve) => {
    const result = confirm(`${title}\n\n${message}`)
    resolve(result)
  })
}

/**
 * 带更多样式的确认（后续可升级为模态框组件）
 * 目前与 showConfirm 相同，为后续升级预留
 */
export function useConfirm() {
  const confirm = (title, message) => {
    return new Promise((resolve) => {
      const result = confirm(`${title}\n\n${message}`)
      resolve(result)
    })
  }
  return { confirm }
}