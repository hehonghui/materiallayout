MaterialLayout
===================
   MaterialLayout是一个能够在低版本的Android 系统中实现点击布局中任何视图都会产生波纹效果的布局，它继承自RelativeLayout，因此能够当做普通的RelativeLayout使用，如果需要其他布局实现该功能，参考该布局实现即可。也可以使用Proxy代理所有的操作，将各个操作封装到一个代理类中，布局类只需要调用代理类来完成相应的工作即可，这样在多种布局都实现这种效果时能够避免代码重复。
   MaterialLayout的使用示例如下:      
```xml

    <org.simple.MaterialLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:ml="http://schemas.android.com/apk/res/com.example.materialdemo"
    android:id="@+id/layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_margin="5dp"
    android:background="#f0f0f0"
    android:gravity="center"
    ml:duration="200"
    ml:alpha="200"
    ml:scale="1.2"
    ml:color="#FFD306" >

    <Button
        android:id="@+id/my_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#33CC99"
        android:padding="10dp"
        android:text="@string/click"
        android:textSize="20sp" />

    <ImageView
        android:id="@+id/my_imageview1"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_below="@id/my_button"
        android:layout_marginTop="30dp"
        android:background="#33CC99"
        android:contentDescription="@string/app_name"
        android:padding="10dp"
        android:src="@drawable/ic_launcher" />

</org.simple.MaterialLayout>	
```     

   注意不要com.example.materialdemo替换为你工程的包名。      
   运行一下就可以看到效果了。         
   <img src="http://img.blog.csdn.net/20150110214259223" width="200" height="320" />
	
