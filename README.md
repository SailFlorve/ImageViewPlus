# ImageViewPlus
ImageView加强版，支持拖动、缩放、绘制和贴纸。
### 特性
自由拖动，且拖动到边界外可以回弹；  
缩放过大或过小可以回弹；  
支持缩放时拖动到边界外回弹，回弹边界为原图边界；  
支持自由绘制和精确坐标绘制；  
支持拖动添加一个贴纸和精确坐标添加贴纸；  
目前，图片分辨率如果小于屏幕，会缩放到一边占满屏幕宽度或高度。  

### 功能演示
拖动和缩放，绘图（自由绘图、指定坐标），贴纸（自由贴纸，指定坐标）：  

![拖动缩放演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/drag%26move.gif)
![绘图演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/draw.gif)
![添加贴纸演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/bubble.gif)
### 说明
此自定义图片View主要为个人使用用途，主要是学习交流，练习自定义View的相关实现，较为简单比较不成熟，没有经过较为复杂的测试。如有幸对你有帮助但需求无法满足，我会尽量完善，也可以自己下载代码进行修改（一共就两个Java文件，和一个资源文件）。
### 使用方法
##### 1.在build.gradle中导入
Project的build.gradle，在repositories中添加：
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
Module的build.gradle，在dependencies中添加：：
```
implementation 'com.github.SailFlorve:ImageViewPlus:1.0.0'
```
##### 2.创建布局和对象
```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.sailflorve.turboimageview.view.ImageViewPlus
        android:id="@+id/ivp"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
        
<RelativeLayout/>
```
```
private ImageViewPlus mImageViewPlus;
mImageViewPlus = findViewById(R.id.ivp);
```
#### 3.设置图片
```
mImageViewPlus.setImage(R.drawable.image);
```
更多详细使用方法详见sample中的代码！

### 公开方法说明
##### 初始化相关

| 方法名称 | 说明 |
|--|--|
|ImageViewPlus(Context)<br>ImageViewPlus(Context, AttributeSet) | 构造方法|
|void setImage(Bitmap)<br>void setImage(@DrawableRes int)<br> void setImage(Uri)| 使用Bitmap、Drawable资源、Uri设置图片|
|void setBubbleBitmap(Bitmap) | 设置贴纸的Bitmap|

##### 拖动和缩放

| 方法名称 | 说明
-|-
void setCanScale(boolean)<br>boolean isCanScale() | 设置是否允许缩放
void setCanDrag(boolean)<br>boolean isCanDrag() | 设置是否允许拖动
void setScaleMax(float) | 设置最大缩放倍数

##### 绘制和贴纸
方法名称 | 说明
-|-
void setDrawMode(boolean)<br>boolean getDrawMode() | DrawMode设置为true可以在图片上任意画线；为false时保存
void setBubbleMode(boolean)<br>boolean getBubbleMode() | BubbleMode设置为true会显示贴纸，此时可以任意拖动；设置为false时保存
void clearMode() | 等同于调用setDrawMode(false)和setBubbleMode(false)
void addLine(PointF, PointF) | 以两点为端点绘制一条直线
void addBubble(float, float) | 在坐标位置贴一个贴纸
void setLineWidth(float) | 设置画线的宽度
void setLineColor(@ColorInt int) | 设置画线的颜色

##### 图片
方法名称 | 说明
-|-
Bitmap getImgBitmap() | 获得图像Bitmap
float getImageViewWidth()<br>float getImageViewHeight() | 获取图片屏幕显示的宽高(不是Bitmap的宽高)


