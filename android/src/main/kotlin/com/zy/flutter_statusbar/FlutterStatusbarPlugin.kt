package com.zy.flutter_statusbar

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import android.view.View.SYSTEM_UI_FLAG_VISIBLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowManager
import android.view.ViewGroup
import android.R
import android.graphics.Rect


class FlutterStatusbarPlugin: MethodCallHandler {

  var activity: Activity
  //默认透明度
  val DEFAULT_STATUS_BAR_ALPHA = 112
//  private val FAKE_STATUS_BAR_VIEW_ID = R.id.statusbarutil_fake_status_bar_view

  constructor(registrar: Registrar){
    activity = registrar.activity()
  }

  companion object {

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_statusbar")
      channel.setMethodCallHandler(FlutterStatusbarPlugin(registrar))
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method){
      "statusBarLightMode" -> statusBarLightMode()
      else -> result.notImplemented()
    }
  }


  fun setColor(color: Int){

  }

  /**
   * 设置状态栏颜色
   *
   * @param activity       需要设置的activity
   * @param color          状态栏颜色值
   * @param statusBarAlpha 状态栏透明度
   */

//  fun setColor(activity: Activity, color: Int, statusBarAlpha: Int) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//      activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//      activity.window.statusBarColor = calculateStatusColor(color, statusBarAlpha)
//    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//      activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//      val decorView = activity.window.decorView as ViewGroup
//      val fakeStatusBarView = decorView.findViewById<View>(FAKE_STATUS_BAR_VIEW_ID)
//      if (fakeStatusBarView != null) {
//        if (fakeStatusBarView.visibility == View.GONE) {
//          fakeStatusBarView.visibility = View.VISIBLE
//        }
//        fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha))
//      } else {
//        decorView.addView(createStatusBarView(activity, color, statusBarAlpha))
//      }
//      setRootView(activity)
//    }
//  }

  /**
   * 计算状态栏颜色
   *
   * @param color color值
   * @param alpha alpha值
   * @return 最终的状态栏颜色
   */
  private fun calculateStatusColor(color: Int, alpha: Int): Int {
    if (alpha == 0) {
      return color
    }
    val a = 1 - alpha / 255f
    var red = color shr 16 and 0xff
    var green = color shr 8 and 0xff
    var blue = color and 0xff
    red = (red * a + 0.5).toInt()
    green = (green * a + 0.5).toInt()
    blue = (blue * a + 0.5).toInt()
    return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
  }

  /**
   * 生成一个和状态栏大小相同的半透明矩形条
   *
   * @param activity 需要设置的activity
   * @param color    状态栏颜色值
   * @param alpha    透明值
   * @return 状态栏矩形条
   */
//  private fun createStatusBarView(activity: Activity, color: Int, alpha: Int): View {
//    // 绘制一个和状态栏一样高的矩形
//    val statusBarView = View(activity)
//    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusHeight(activity))
//    statusBarView.layoutParams = params
//    statusBarView.setBackgroundColor(calculateStatusColor(color, alpha))
//    statusBarView.id = FAKE_STATUS_BAR_VIEW_ID
//    return statusBarView
//  }

  /**
   * 高亮模式，字体图标为黑色
   * 1:MIUUI 2:Flyme 3:android6.0
   */
  fun statusBarLightMode() : Int{
    var result = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
      if (MIUISetStatusBarLightMode(activity, true)) {
        result = 1
      } else if (FlymeSetStatusBarLightMode(activity.getWindow(), true)) {
        result = 2
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        activity.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        result = 3
      }
    }
    return result
  }

  /**
   * 设置根布局参数
   */
  private fun setRootView(activity: Activity) {
    val parent = activity.findViewById<View>(R.id.content) as ViewGroup
    var i = 0
    val count = parent.childCount
    while (i < count) {
      val childView = parent.getChildAt(i)
      if (childView is ViewGroup) {
        childView.setFitsSystemWindows(true)
        childView.clipToPadding = true
      }
      i++
    }
  }

  fun MIUISetStatusBarLightMode(activity: Activity, dark: Boolean) : Boolean{
    var result : Boolean = false
    var window : Window = activity.window
    if (window != null){
      var clazz = window.javaClass
      try {
        var darkModeFlag = 0
        val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
        val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
        darkModeFlag = field.getInt(layoutParams)
        val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        if (dark) {
          extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
        } else {
          extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
        }
        result = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
          if (dark) {
            activity.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
          } else {
            activity.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE
          }
        }
      } catch (e: Exception) {

      }

    }
    return result
  }

  fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
    var result = false
    if (window != null) {
      try {
        val lp = window.attributes
        val darkFlag = WindowManager.LayoutParams::class.java
                .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
        val meizuFlags = WindowManager.LayoutParams::class.java
                .getDeclaredField("meizuFlags")
        darkFlag.isAccessible = true
        meizuFlags.isAccessible = true
        val bit = darkFlag.getInt(null)
        var value = meizuFlags.getInt(lp)
        if (dark) {
          value = value or bit
        } else {
          value = value and bit.inv()
        }
        meizuFlags.setInt(lp, value)
        window.attributes = lp
        result = true
      } catch (e: Exception) {

      }

    }
    return result
  }

  fun getStatusHeight(activity: Activity): Int {
    var statusHeight = 0
    val localRect = Rect()
    activity.window.decorView
            .getWindowVisibleDisplayFrame(localRect)
    statusHeight = localRect.top
    if (0 == statusHeight) {
      val localClass: Class<*>
      try {
        localClass = Class.forName("com.android.internal.R\$dimen")
        val localObject = localClass.newInstance()
        val i5 = Integer.parseInt(localClass
                .getField("status_bar_height").get(localObject)
                .toString())
        statusHeight = activity.resources
                .getDimensionPixelSize(i5)
      } catch (e: ClassNotFoundException) {
        e.printStackTrace()
      } catch (e: IllegalAccessException) {
        e.printStackTrace()
      } catch (e: InstantiationException) {
        e.printStackTrace()
      } catch (e: NumberFormatException) {
        e.printStackTrace()
      } catch (e: IllegalArgumentException) {
        e.printStackTrace()
      } catch (e: SecurityException) {
        e.printStackTrace()
      } catch (e: NoSuchFieldException) {
        e.printStackTrace()
      }

    }
    return statusHeight
  }

}
