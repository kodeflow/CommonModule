# CommonModule
此module为android端通用代码封装
# Useage
使用前必须先在Application中注册
```
class App: Application() {
  override fun onCreate() {
    super.onCreate()
    // 在此注册后方可在应用内使用
    CommonModule.regist(this)
  }
}
```

### package bean
封装了网络请求相关的数据模型，与后台返回接口对应
### package crypt
封装了AES&Base64 加密解密（AES有默认加密密钥）
### package compat
封装了调用系统相册和相机获取图片功能
### package delegate
封住了SharedPreference的操作和单例非空验证的类
### package extensions
封装了KProgressHUD和dip转换工具
### package http
Retrofit封装
