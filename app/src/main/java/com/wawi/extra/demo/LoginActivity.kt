package com.wawi.extra.demo

import android.Manifest.permission.READ_CONTACTS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
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
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        println("---- chinese week name: ${Date().weekNameCN()}")
        println("---- english week name: ${Date().weekNameEN()}")
        println("---- now is leap year: ${Date().isLeapYear()}")
        println("---- 2008 is leap year: ${2008.isLeapYear()}")
        println("---- 2018 is leap year: ${2018.isLeapYear()}")

        // Set up the login form.
        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener {
//            val url = "https://publicobject.com/helloworld.txt"
             //val url = "http://imtt.dd.qq.com/16891/89E1C87A75EB3E1221F2CDE47A60824A.apk?fsname=com.snda.wifilocating_4.2.62_3192.apk&csr=1bbd"
            val url = "https://nj02all01.baidupcs.com/file/a74deb0e3d119f839dd65e4ba5b6854a?bkt=en-c58a217c5b5bf7b2a908fb7579ce52e3f1a57ebb136083e4b277c5bc600493bee9168ac3f191da77&fid=404856442-250528-472007050951628&time=1568599190&sign=FDTAXGERLQBHSKfW-DCb740ccc5511e5e8fedcff06b081203-FjNlcT6XIS4oHIelpndrgpimhe0%3D&to=69&size=42591684&sta_dx=42591684&sta_cs=0&sta_ft=psd&sta_ct=0&sta_mt=0&fm2=MH%2CGuangzhou%2CAnywhere%2C%2Chubei%2Cct&ctime=1568599020&mtime=1568599020&resv0=cdnback&resv1=0&resv2=&resv3=&vuk=1010835052&iv=0&htype=&randtype=&newver=1&newfm=1&secfm=1&flow_ver=3&pkey=en-35801a4295edc6012316773d9e8e96a32233bec51271dfd896ef950e4d15679d1a8804c87b0c02b2&sl=76480590&expires=8h&rt=sh&r=917575372&vbdid=1103453851&fin=3.psd&rtype=1&dp-logid=5993409289915168544&dp-callid=0.1&hps=1&tsl=80&csl=80&csign=9H8hlOWTyLGwVY8uZnCXOQywuCU%3D&so=0&ut=6&uter=4&serv=0&uc=2251096754&ti=45e4b516918e14430746e23b6d67944c2ecc21b09cdefd38&by=themis"
            ToastCompat.show("Hello")
            GlobalScope.launch {
                DownloadManager.shared().download(url, { read, total, done ->
                    println("---- $read $total  $done")
                }) {
                    println("---- $it")
                }
            }
//            Thread(Progress()).start()
            attemptLogin()
        }

//        GlobalScope.launch {
//            val item = DownloadBean("http://baidu.com", "caowen/*.apk", "*.apk", 100, 30)
//            val item1 = DownloadBean("http://baidu.com", "codeflow/*.apk", "*.apk", 100, 30)
//
//            CommonModule.getDownloadDatabase().downloadDao().insertRecords(item, item1)
//            val bean = CommonModule.getDownloadDatabase().downloadDao().findByUrl("http://baidu.com")
//
//            println("bean: $bean")
//
//        }



    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok
                ) { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) }
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
//            val hud = hud("正在登录")
            val hud = hud()

            Crowd.login(
                ParamLogin("17343203065", AESCrypt.encode("qa123456")),
                {
                    hud.dismiss()
                    println("----->")
                    println(it)
                }, {
                    hud.dismiss()
                    println(it)
                })
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true)
//            mAuthTask = UserLoginTask(emailStr, passwordStr)
//            mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(
            this,
            // Retrieve data rows for the device user's 'profile' contact.
            Uri.withAppendedPath(
                ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY
            ), ProfileQuery.PROJECTION,

            // Select only email addresses.
            ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(
                ContactsContract.CommonDataKinds.Email
                    .CONTENT_ITEM_TYPE
            ),

            // Show primary email addresses first. Note that there won't be
            // a primary email address if the user hasn't specified one.
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        )
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(
            this@LoginActivity,
            android.R.layout.simple_dropdown_item_1line, emailAddressCollection
        )

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY
        )
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mEmail: String, private val mPassword: String) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                return false
            }

            return DUMMY_CREDENTIALS
                .map { it.split(":") }
                .firstOrNull { it[0] == mEmail }
                ?.let {
                    // Account exists, return true if the password matches.
                    it[1] == mPassword
                }
                ?: true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null
            showProgress(false)

            if (success!!) {
                finish()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }

    fun handleAction(view: View) {
        when (view.id) {
            R.id.pause -> {
                DownloadManager.shared().pause()
            }
        }
    }
}
