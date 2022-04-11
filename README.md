# StereoscopicVsionAndroid
---

## 计划要做的
1、测距模式 参照苹果的测距仪 //两个摄像头测距取平均值 <br>
2、立体模式 红蓝3D //有2D渲染3D的技术 但是会造成亮度下降 <br>
3、景深合成 利用深度图对图片不同距离进行线性卷积 <br>
---

### 引入OpenCV
1.下载好的OpenCV（Android Release） 解压后将文件夹里的java文件夹复制到本文件夹内 <br>
2.File—>New—>Import Module… <br>
> 位置->   ./java
> 名字->   OpenCVLibrary3416//OpenCV的版本号3.4.16

3.OpenCVLibrary3416的build.gradle，使其和StereoscopicVsionAndroid的build.gradle对应项一致 <br>
4.File-Project Structure
> Modules里找到项目
> > 点击右边的+
> > 选择Module dependency
> 选择openCVLibrary340，导入

5.import org.opencv.android.*; <br>
6.拼接opencv manager <br>
> 把OpenCV sdk for Android文件下./OpenCV-android-sdk/sdk/native下的libs文件夹拷贝到\src\main下面 重命名该lib
> 将OpenCV-android-sdk\samples\image-manipulations\res\layout下的xml文件拷贝到自己的项目\src\main\res下面
> 将.\src\org\opencv\samples\imagemanipulations下的java文件拷到自己的项目\src\main\java\你MainActivity所在的包名
> > arm64-v8a: 64位支持，目前主流的版本

7.导入完成后自己弄个跳转看看ImageManipulationsActivity能不能正常使用，layout文件为image_manipulations_surface_view.xml <br>
## **如果还是看不懂** [教程](https://blog.csdn.net/qq_33198758/article/details/82984216)
---

