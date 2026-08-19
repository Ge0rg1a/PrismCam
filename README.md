# PrismCam

基于 OpenGL ES 的实时滤镜相机应用，提供 Android 端 GPU 渲染管线、GLSL Shader 与图像处理基础算法的完整实现。

## 特性

- CameraX + GLSurfaceView 自渲染管线（OES 纹理零拷贝接相机）
- 9 种滤镜，覆盖点运算 / 卷积 / 可分离卷积
- FBO 离屏串联 + GL 拍照回读（glReadPixels）
- buffer aspect 校正（center-crop，画面不变形）

## 滤镜

| 滤镜 | 算法类别 |
| --- | --- |
| 原图 | passthrough |
| 灰度 | 点运算（亮度系数加权） |
| 反相 | 点运算 |
| 亮度/对比度 | 点运算 + 饱和度混合 |
| 伽马 | 点运算（pow） |
| 高斯模糊 | 可分离卷积（水平+垂直两趟，FBO ping-pong） |
| 锐化 | 3x3 卷积 |
| 边缘 | Sobel 梯度 |
| 色调分离 | 点运算（量化） |

## GL 管线

```
CameraX Preview
  → SurfaceProvider → SurfaceTexture(OES 纹理)
  → OesInputFilter(uStm + uQuadScale) → texA(FBO 离屏)
  → 用户滤镜 → 屏幕
拍照: 滤镜 → photoFbo → glReadPixels → Bitmap → MediaStore
```

纹理变换矩阵 `SurfaceTexture.getTransformMatrix()` 已包含画面朝向校正（含 sensor rotation），顶点仅做 buffer aspect 缩放，不再额外旋转。

## 构建

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

运行时需授予相机权限。

## 关键技术点

- `GL_TEXTURE_EXTERNAL_OES` 外部纹理零拷贝接相机
- GLSL ES 顶点 / 片元 shader、uniform 调参
- FBO 离屏渲染串联（OES→texA→滤镜→屏幕 / photoFbo）
- 卷积 / 可分离滤波（高斯模糊拆水平+垂直两趟）
- `glReadPixels` 回读保存
- 按 buffer 分辨率 + 旋转方向计算 center-crop 缩放，校正画面比例

## 环境

AGP 9.2.1 / Gradle 9.4.1 / CameraX 1.4.2 / minSdk 24 / targetSdk 36 / Java 11
