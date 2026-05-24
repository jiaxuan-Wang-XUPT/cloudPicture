<template>
  <div id="userLoginPage">
    <h2 class="title">skye云图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 6, message: '密码不能小于 6 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLoginUsingPost } from '@/api/userController'
import { message } from 'ant-design-vue'

//用于接收表单输入的值
//const表示声明常量(壳不变，内容可以变)
//reactive内容通过这个核心 API，用来创建响应式数据
//也就是能动态监听前端发送过来的请求，UserLoginRequest比如这个请求。
//而API.UserLoginRequest它不是代码逻辑，它不参与运行，纯粹是为了代码提示和类型检查
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
//提交函数，也就是点击登录后需要在全局状态中记录当前登录用户信息并跳转到主页
const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
/*
异步就是不打扰用户的等待机制，不需要一直卡在这里等待后端响应再往下执行
async (...) => { ... }
这定义了一个异步函数。async 关键字告诉 JavaScript：
“这个函数里包含一些耗时的操作（比如网络请求），需要等待，请不要卡住页面

userLoginUsingPost(values)：调用封装好的登录接口，把表单数据 values 发给后端
* */
const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
