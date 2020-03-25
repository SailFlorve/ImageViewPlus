# ImageViewPlus
ImageView加强版，支持拖动、缩放、绘制和贴纸。
### 特性
自由拖动缩放，且拖动到边界外可以回弹，缩放过大或过小也可以回弹；   
支持自由绘制和精确坐标绘制；  
支持拖动添加一个贴纸和精确坐标添加贴纸。 

### 功能演示
拖动和缩放，绘图（自由绘图、指定坐标），贴纸（自由贴纸，指定坐标）：  

![拖动缩放演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/drag%26move.gif)
![绘图演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/draw.gif)
![添加贴纸演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/bubble.gif)
### 说明
此自定义图片View最初为个人学习自定义View的相关实现以及个人使用，较为简单，没有经过复杂的测试。我会尽量完善，也可以自己下载代码进行修改（一共就两个Java文件，和一个资源文件）。
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
implementation 'com.github.SailFlorve:ImageViewPlus:1.1.0'
```
##### 2.创建布局和对象
```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.sailflorve.imageviewplus.view.ImageViewPlus
        android:id="@+id/ivp"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
        
<RelativeLayout/>
```
```
private ImageViewPlus mImageViewPlus;
```
```
mImageViewPlus = findViewById(R.id.ivp);
```
#### 3.设置图片
```
mImageViewPlus.setImage(R.drawable.image);
```
更多详细使用方法见sample中的代码！

### 公开方法说明
##### 初始化和图片

| 方法名称 | 说明 |
|--|--|
|ImageViewPlus(Context)<br>ImageViewPlus(Context, AttributeSet) | 构造方法|
|void setImage(Bitmap)<br>void setImage(@DrawableRes int)<br> void setImage(Uri)| 使用Bitmap、Drawable资源、Uri设置图片|
|void setBubbleBitmap(Bitmap) | 设置贴纸的Bitmap|
|Bitmap getImageBitmap() | 获得图像Bitmap|
|float getImageViewWidth()<br>float getImageViewHeight() | 获取图片屏幕显示的宽高(不是Bitmap的宽高)|
|ImageView getImageView() | 获取ImageView|

##### 拖动和缩放

| 方法名称 | 说明
-|-
void setScalable(boolean)<br>boolean isScalable() | 设置是否允许缩放
void setDraggable(boolean)<br>boolean isDraggable() | 设置是否允许拖动
void setScaleMax(float) | 设置最大缩放倍数

##### 绘制和贴纸
方法名称 | 说明
-|-
void setDrawMode(boolean)<br>boolean getDrawMode() | DrawMode设置为true可以在图片上任意画线；为false时保存
void setBubbleMode(boolean)<br>boolean getBubbleMode() | BubbleMode设置为true会显示贴纸，此时可以任意拖动；设置为false时保存
void clearMode() | 等同于调用setDrawMode(false)和setBubbleMode(false)
void addLine(PointF, PointF) | 以两点为端点绘制一条直线，坐标基于图片显示的宽高
void addBubble(float, float) | 在坐标位置贴一个贴纸，坐标基于图片显示的宽高
void setLineWidth(float) | 设置画线的宽度
void setLineColor(@ColorInt int) | 设置画线的颜色

### 使用自定义ImageView
例如，在某些情况下需要加载图片URL，或者进行共享元素动画，想把内部的ImageView替换为Fresco的SimpleDraweeView或其他的自定义ImageView。
```
SimpleDraweeView sdv = findViewById(R.id.sdv);
mImageViewPlus.replaceImageView(sdv);

sdv.setImageUri(...)
```
需要注意的是，这种情况下设置图片时请使用SimpleDraweeView实例，因为SimpleDraweeView不推荐使用ImageView的方法设置图片。这会导致内部Bitmap为空，所以使用SimpleDraweeView后绘制和贴图功能也将失效。如果是其他自定义ImageView，可以使用setImage()方法设置图片。

### 结合ViewPager一次浏览多张图片
直接使用ViewPager嵌套即可，但是要把isDraggable设置为false，否则产生滑动冲突。<br>
另外提供ImageViewPlusViewPager类（它并非ViewPager子类）快速实现，
添加多个ImageViewPlus对象即可。<br>
```
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.sailflorve.imageviewplus.view.ImageViewPlusViewPager
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clickable="false" />

</androidx.constraintlayout.widget.ConstraintLayout>

val list: List<ImageViewPlus> = listOf(
    ImageViewPlus(this).apply {
        setImage(R.drawable.bg_example1)
    },

    ImageViewPlus(this).apply {
        setImage(R.drawable.timg)
    }
)

viewPager.setImageViews(list)

```