<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1 class="register-title">注册账号</h1>
        <p class="register-subtitle">创建您的企业征信管理平台账号</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="enterpriseName">
          <el-input
            v-model="form.enterpriseName"
            placeholder="请输入企业名称"
            size="large"
            prefix-icon="OfficeBuilding"
          />
        </el-form-item>
        <el-form-item prop="creditCode">
          <el-input
            v-model="form.creditCode"
            placeholder="请输入统一社会信用代码"
            size="large"
            prefix-icon="Document"
            maxlength="18"
          />
        </el-form-item>
        <el-form-item prop="legalPerson">
          <el-input
            v-model="form.legalPerson"
            placeholder="请输入法定代表人"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item prop="smsCode">
          <div class="sms-code-wrapper">
            <el-input
              v-model="form.smsCode"
              placeholder="请输入验证码"
              size="large"
              prefix-icon="Key"
              maxlength="6"
            />
            <el-button
              class="sms-code-btn"
              :disabled="smsCountdown > 0"
              @click="handleSendSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（8-20位，包含字母和数字）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.agreeTerms">
            我已阅读并同意<a href="#" class="terms-link">《用户协议》</a>和<a href="#" class="terms-link">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-button"
            native-type="submit"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span class="login-hint">已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { register, sendSmsCode } from '@/api/auth'
import { validatePhone, validateCreditCode, validatePassword } from '@/utils/common'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  enterpriseName: '',
  creditCode: '',
  legalPerson: '',
  phone: '',
  smsCode: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 2, max: 100, message: '企业名称长度为2-100个字符', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (!validateCreditCode(value)) {
        callback(new Error('请输入正确的统一社会信用代码'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  legalPerson: [
    { required: true, message: '请输入法定代表人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (!validatePhone(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (!validatePassword(value)) {
        callback(new Error('密码需为8-20位，包含字母和数字'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSendSms = async () => {
  if (!validatePhone(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    await sendSmsCode(form.phone, 'register')
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0 && smsTimer) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch (error) {
    console.error('Send SMS failed:', error)
  }
}

const handleRegister = async () => {
  if (!formRef.value) return

  if (!form.agreeTerms) {
    ElMessage.warning('请阅读并同意用户协议和隐私政策')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await register({
        enterpriseName: form.enterpriseName,
        creditCode: form.creditCode,
        legalPerson: form.legalPerson,
        phone: form.phone,
        password: form.password,
        smsCode: form.smsCode
      })

      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      console.error('Register failed:', error)
    } finally {
      loading.value = false
    }
  })
}

onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
  }
})
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;
}

.register-box {
  width: 440px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #909399;
}

.register-form {
  margin-top: 20px;
}

.sms-code-wrapper {
  display: flex;
  gap: 10px;

  .sms-code-btn {
    width: 120px;
    flex-shrink: 0;
  }
}

.terms-link {
  color: #409eff;
  margin: 0 3px;
}

.register-button {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.login-hint {
  color: #909399;
}

.login-link {
  color: #409eff;
  margin-left: 5px;
}
</style>
