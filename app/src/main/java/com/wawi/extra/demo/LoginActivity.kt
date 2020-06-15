package com.wawi.extra.demo

import android.Manifest
import android.Manifest.permission.READ_CONTACTS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.vector.update_app.HttpManager
import com.vector.update_app.UpdateAppBean
import com.vector.update_app.utils.AppUpdateUtils
import com.vector.update_app_kotlin.check
import com.vector.update_app_kotlin.updateApp
import com.wawi.android.wwsd.http.UpdateAppHttpUtil
import com.wawi.api.compat.PhotoCompat
import com.wawi.api.compat.PhotoCompatDelegate
import com.wawi.api.crypt.AESCrypt
import com.wawi.api.extensions.hud
import com.wawi.api.extensions.isLeapYear
import com.wawi.api.extensions.weekNameCN
import com.wawi.api.extensions.weekNameEN
import com.wawi.extra.common.compat.ToastCompat
import com.wawi.extra.common.http.download.DownloadManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.downloadManager
import java.util.*


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private var mAuthTask: UserLoginTask? = null
    private val photoCompat = PhotoCompat(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET), 1)
//
        email_sign_in_button.setOnClickListener {
            photoCompat.setPhotoCompatDelegate(object : PhotoCompatDelegate {
                override fun onPhoto(bitmap: Bitmap?) {
                    //TODO("Not yet implemented")
                    print("照相获取的图片：")
                    print(bitmap)
                    pic.setImageBitmap(bitmap)
                    ToastCompat.show(bitmap.toString())
                }

            })
            val permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                photoCompat.takePhoto()
            }

        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoCompat.onActivityResult(requestCode, resultCode, data)
    }


    fun handleAction(view: View) {
        when (view.id) {
            R.id.pause -> {
                DownloadManager.shared().pause()
            }
        }
    }
}
