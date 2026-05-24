

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/access'

// import App from './App.vue'
// import router from './router'
//
// const app = createApp(App)
//
// app.use(createPinia())
// app.use(router)
//
// app.mount('#app')

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import VueCropper from 'vue-cropper'
import 'vue-cropper/dist/index.css'



const app = createApp(App)
app.use(Antd)
app.use(createPinia())
app.use(router)
app.use(VueCropper)
app.mount('#app')


