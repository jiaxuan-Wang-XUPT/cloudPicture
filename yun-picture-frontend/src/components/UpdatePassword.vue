<!-- src/components/UpdatePasswordPage.vue -->
<template>
  <div id="UpdatePassword">
    <a-modal
      v-model:open="visible"
      title="修改密码"
      :maskClosable="false"
      @cancel="handleCancel"
      @ok="handleOk"
      okText="确认"
      cancelText="取消"
    >
      <a-form
        :model="form"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="旧密码" name="userPassword">
          <a-input-password v-model:value="form.userPassword" placeholder="请输入旧密码" />
        </a-form-item>

        <a-form-item label="新密码" name="newPassword">
          <a-input-password v-model:value="form.newPassword" placeholder="请输入新密码" />
        </a-form-item>

        <a-form-item label="确认新密码" name="checkPassword">
          <a-input-password v-model:value="form.checkPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import { updatePasswordUsingPost } from '@/api/userController' // 你需要根据你的 api 代码生成工具配置这个接口

const visible = defineModel<boolean>('visible')

// 定义 Emits，用于通知父组件关闭模态框或更新状态
const emit = defineEmits<{
  (e: 'close'): void
}>()

// 表单数据
const form = reactive({
  userPassword: '',
  newPassword: '',
  checkPassword: '',
})

// 表单验证规则
const rules = {
  userPassword: [{ required: true, message: '请输入旧密码' }],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 6, message: '密码长度不能少于6位' },
  ],
  checkPassword: [
    { required: true, message: '请确认新密码' },
    ({ getFieldValue }: any) => ({
      validator(_: any, value: string) {
        if (!value || getFieldValue('newPassword') === value) {
          return Promise.resolve()
        }
        return Promise.reject(new Error('两次输入的密码不一致!'))
      },
    }),
  ],
}

// 处理确认按钮
const handleOk = async () => {
  try {
    const res = await updatePasswordUsingPost({
      userPassword: form.userPassword,
      newPassword: form.newPassword,
      checkPassword: form.checkPassword,
    })

    if (res.data.code === 0) {
      message.success('密码修改成功！')
      handleCancel() // 关闭弹窗
      // 可以在这里触发登出，强制用户重新登录
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error: any) {
    message.error('操作失败：' + error.message)
  }
}

// 处理取消/关闭
const handleCancel = () => {
  // 重置表单
  form.userPassword = ''
  form.newPassword = ''
  form.checkPassword = ''
  visible.value = false
  emit('close')
}
</script>

<style scoped>
/* 样式可以根据需要添加 */
</style>
