# ImageViewPlus
ImageView加强版，支持拖动、缩放、绘制和贴纸。  
### 功能演示
拖动和缩放，绘图（自由绘图、指定坐标），贴纸（自由贴纸，指定坐标）：  

![拖动缩放演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/drag%26move.gif)
![绘图演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/draw.gif)
![添加贴纸演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/bubble.gif)
### 说明
此自定义图片View主要为学习交流使用，实现较为简单比较不成熟，没有经过较为复杂的测试。如有幸对你有帮助，可以自己下载代码进行修改（一共就两个Java文件，和一个资源文件）。也可以提出意见共同交流学习。
### 使用方法
##### 1.在build.gradle中导入
##### 2.创建布局和对象
```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.sailflorve.turboimageview.view.ImageViewPlus
        android:id="@+id/cus_iv"
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

### 接口说明
##### 初始化相关
方法名称 | 说明
-|-|-
ImageViewPlus(Context)<br>ImageViewPlus(Context, AttributeSet) | 构造方法
void setImage(Bitmap)<br>void setImage(@DrawableRes int)<br> void setImage(Uri)| 使用Bitmap、Drawable资源、Uri设置图片
void setBubbleBitmap(Bitmap) | 设置贴纸的Bitmap

##### 拖动和缩放
方法名称 | 说明
-|-|-
void setCanScale(boolean)<br>boolean isCanScale() | 设置是否允许缩放
void setCanDrag(boolean)<br>boolean isCanDrag() | 设置是否允许拖动
void setScaleMax(float) | 设置最大缩放倍数

##### 绘制和贴纸
方法名称 | 说明
-|-|-
void setDrawMode(boolean)<br>boolean getDrawMode() | DrawMode设置为true可以在图片上任意画线；为false时保存
void setBubbleMode(boolean)<br>boolean getBubbleMode() | BubbleMode设置为true会显示贴纸，此时可以任意拖动；设置为false时保存
void clearMode() | 等同于调用setDrawMode(false)和setBubbleMode(false)
void addLine(PointF, PointF) | 以两点为端点绘制一条直线
void addBubble(float, float) | 在坐标位置贴一个贴纸
void setLineWidth(float) | 设置画线的宽度
void setLineColor(@ColorInt int) | 设置画线的颜色

##### 图片
方法名称 | 说明
-|-|-
Bitmap getImgBitmap() | 获得图像Bitmap
float getImageViewWidth()<br>float getImageViewHeight() | 获取图片在屏幕上的宽高(不是Bitmap的)


