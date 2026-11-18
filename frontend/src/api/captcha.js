import request from '@/utils/request'

export const captchaApi = {
  isEnabled() {
    return request.get('/captcha/enabled')
  },

  generate(type) {
    return request.get('/captcha/generate', { params: { type } })
  },

  verify(data) {
    return request.post('/captcha/verify', data)
  },

  getConfig() {
    return request.get('/captcha/config')
  },

  updateConfig(data) {
    return request.put('/captcha/config', data)
  }
}
