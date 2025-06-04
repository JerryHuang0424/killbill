package org.wit.killbill.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.permissionx.guolindev.PermissionX
import org.wit.killbill.R
import org.wit.killbill.backGroundService.BackGroundService
import org.wit.killbill.databinding.MainActivityBinding
import org.wit.killbill.fragment.DailyFragment
import org.wit.killbill.fragment.NotifyListFragment
import org.wit.killbill.fragment.StatisticFragment
import org.wit.killbill.notifyServer.NotifyService


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var notifyService: NotifyService
    companion object {
        private const val REQUEST_CODE = 9527
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !PermissionX.isGranted(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
            PermissionX.init(this)
                .permissions(android.Manifest.permission.POST_NOTIFICATIONS)
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        val intent = Intent(this, BackGroundService::class.java)
                        startForegroundService(intent)
                    }
                }
        } else {
            val intent = Intent(this, BackGroundService::class.java)
            startForegroundService(intent)
        }

        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        setupBottomNavigation()
        if (savedInstanceState == null) {
            // 默认加载DailyFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DailyFragment.newInstance())
                .commit()
        }
    }



    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_today -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, DailyFragment.newInstance())
                        .commit()
                    true
                }

                R.id.navigation_stats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, StatisticFragment.newInstance())
                        .commit()
                    true
                }

                R.id.navigation_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NotifyListFragment.newInstance())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menuInflater.inflate(R.menu.menu_setting, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_add -> {
                val launchIntent = Intent(this, PageMainActivity::class.java)
                startActivity(launchIntent)
            }
        }
        when(item.itemId){
            R.id.item_setting -> {
                requestPermission()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 请求通知监听权限
     */
    //用户点击按钮触发 requestPermission(), binding with the button in the layout:
    private fun requestPermission() {
        // 方法1：直接跳转通知监听权限设置页（推荐）
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)

        // 方法2：检查并提示用户手动开启（可选）
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
            Toast.makeText(this, "请找到本应用并开启通知监听权限", Toast.LENGTH_LONG).show()
        }
    }

    private fun showMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (notifyService.isNLServiceEnabled()) {
                showMsg("通知服务已开启")
                notifyService.toggleNotificationListenerService(true)
            } else {
                showMsg("通知服务未开启")
                notifyService.toggleNotificationListenerService(false)
            }
        }
    }
}