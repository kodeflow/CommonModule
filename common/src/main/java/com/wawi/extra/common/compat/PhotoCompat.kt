package com.wawi.api.compat

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment.getExternalStorageDirectory
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException


interface PhotoCompatDelegate {
    fun onPhoto(bitmap: Bitmap?)
}

private const val AUTHORITY = "com.wawi.api.takephoto.fileprovider"

open class PhotoCompat(context: Any) {
    private var context: Any? = null
    /** 当前请求类型【拍照、相册】 */
    var currentRequest = -1

    private lateinit var imageUri: Uri

    private var delegate: PhotoCompatDelegate? = null

    init {
        when (context) {
            is Fragment -> this.context = context
            is Activity -> this.context = context
            else -> throw Exception("Unknown context type: $context. @{android.support.v4.app.Fragment} & @{android.app.Activity} can be only supported")
        }
    }

    /**
     * 设置代理对象回调返回结果
     */
    open fun setPhotoCompatDelegate(delegate: PhotoCompatDelegate) {
        this.delegate = delegate
    }

    /**
     * 相册获取图片
     */
    open fun chooseFromAlbum() {
        // 保存当前请求类型
        currentRequest = RequestType.REQUEST_CHOOSE_PHOTO

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (context is Fragment) {
            (context as Fragment).startActivityForResult(intent, currentRequest)
        } else {
            (context as Activity).startActivityForResult(intent, currentRequest)
        }

    }

    /**
     * 拍照获取图片
     */
    open fun takePhoto() {
        val ctx: Context?
        if (context is Fragment) {
            ctx = (context as Fragment).context
        } else {
            ctx = (context as Activity)
        }

        currentRequest = RequestType.REQUEST_TAKE_PHOTO
        val outputImage = File(ctx?.externalCacheDir,"output_image.jpg")

        try {
            if(outputImage.exists()) {
                outputImage.delete()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        imageUri = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri.fromFile(outputImage)
        }else {
            FileProvider.getUriForFile(ctx!!, AUTHORITY, outputImage)
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        if (context is Activity) {
            (context as Activity).startActivityForResult(intent, currentRequest)
        } else {
            (context as Fragment).startActivityForResult(intent, currentRequest)
        }
    }

    /**
     * handle photo data
     * 此方法在Fragment.onActivityResult 或者 Activity.onActivityResult 中调用
     */
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // 如果是拍照
                RequestType.REQUEST_TAKE_PHOTO -> {
                    val bitmap = BitmapFactory.decodeStream(((context as? Activity) ?: (context as Fragment).context)?.contentResolver?.openInputStream(imageUri))
                    // Callback
                    delegate?.onPhoto(bitmap)
                }
                // 如果是从相册选择
                RequestType.REQUEST_CHOOSE_PHOTO -> {
                    handleResult(data)
                }
            }
        }
    }
    /**
     * handle permission granted
     * 此方法在onRequestPermissionsResult验证权限通过后调用
     */
    open fun onRequestPermissionGranted() {
        if (currentRequest == RequestType.REQUEST_CHOOSE_PHOTO) {
            chooseFromAlbum()
        } else {
            takePhoto()
        }
    }

    /**
     * ---------------- private methods ----------------
     */

    private fun handleResult(data: Intent?) {
        var imagePath:String? = null
        val uri = data?.data

        val ctx: Context?
        if (context is Activity) {
            ctx = context as Activity
        } else {
            ctx = (context as Fragment).context
        }

        if(DocumentsContract.isDocumentUri(ctx, uri)) {

            val docId = DocumentsContract.getDocumentId(uri)
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val split = docId.split(":")
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    imagePath = "${ getExternalStorageDirectory() }/${ split[1] }"
                }
            }
            // MediaProvider
            else if(isMediaDocument(uri)) {
                val id = docId.split(":")[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
                imagePath = getImagePath(contentUri,null)
            }
        }
        // MediaStore (and general)
        else if ("content".equals(uri?.scheme, true)) {
            imagePath = if (isGooglePhotosUri(uri)) {
                uri?.lastPathSegment
            } else {
                getImagePath(uri, null)
            }
        }
        // File
        else if ("file".equals(uri?.scheme, true)) {
            imagePath = uri?.path
        }

        // Callback
        if(imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            delegate?.onPhoto(bitmap)
        }else {
            delegate?.onPhoto(null)
        }
    }

    private fun  getImagePath(uri: Uri?, selection: String?): String? {
        if (uri == null) {
            return null
        }
        var path: String? = null
        val cursor: Cursor? = if (context is Activity) {
            (context as Activity).contentResolver?.query(uri,null,selection,null,null)
        } else {
            (context as Fragment).context?.contentResolver?.query(uri,null,selection,null,null)
        }

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

    // ---------------- 判断uri类型 ----------------
    private fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri?.authority
    }

    private fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri?.authority
    }

    private fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri?.authority
    }

    private fun isGooglePhotosUri(uri: Uri?): Boolean {
        return "com.google.android.apps.photos.content" == uri?.authority
    }
}

object RequestType {
    const val REQUEST_TAKE_PHOTO = 2.shl(1)
    const val REQUEST_CHOOSE_PHOTO = 2.shl(2)
}