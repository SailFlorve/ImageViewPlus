# ImageViewPlus
ImageView加强版，支持拖动、缩放、绘制和贴纸。  
### 功能演示
拖动和缩放，绘图（自由绘图、指定坐标），贴纸（自由贴纸，指定坐标）：  

![拖动缩放演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/drag%26move.gif)
![绘图演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/draw.gif)
![添加贴纸演示](https://github.com/SailFlorve/ImageViewPlus/raw/master/img/bubble.gif)
### 使用方法  
##### 1.在build.gradle中导入  
##### 2.创建布局  
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
