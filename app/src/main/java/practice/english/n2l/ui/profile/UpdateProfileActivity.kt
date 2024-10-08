package practice.english.n2l.ui.profile


import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityUpdateProfileBinding
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.ProfileRepo
import practice.english.n2l.repository.ProfileRepoImp
import practice.english.n2l.ui.BaseActivity
import practice.english.n2l.util.Constants
import practice.english.n2l.util.RealPathUtil
import practice.english.n2l.viewmodel.ProfileViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors

class UpdateProfileActivity : BaseActivity() {
    private val repository                           : ProfileRepo by lazy { ProfileRepoImp(RetrofitService.service,this) }
    private val viewModel                            : ProfileViewModel by lazy { ViewModelProvider(this, ProfileViewModel.Factory(repository))[ProfileViewModel::class.java] }
    private lateinit var binding                     : ActivityUpdateProfileBinding
    private  lateinit var  studentProfile            : StudentProfile
    private val progressDialog                       : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private lateinit var userDetail                  : UserDetail
    private  var imagePath                           :String?=null
    private val cameraCapture = 1
    private val galleryCapture = 2
    private var picUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_profile)
        binding.lifecycleOwner = this

        viewModel.getStudentProfileLiveData().observe(this){
            it?.let {
                binding.studentProfile=it
                imagePath=it.photoLocalPath
                this.studentProfile=it
            }
        }
        viewModel.updateProfile.observe(this){
            when(it) {
                is ResponseHandle.Loading->{
                    progressDialog.setMessage(getString(R.string.update_wait_msg))
                    progressDialog.startLoading()
                }
                is ResponseHandle.Success ->{
                    when (it.data!!.responseCode) {
                        800 -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.IO) {
                                    val mainUrl=it.data.responseData.mainUrl[0]
                                    val studentProfile= it.data.responseData.studentProfile
                                    studentProfile.photoData=downloadAnswerImage(mainUrl.url, studentProfile.photoPath)
                                    studentProfile.photoLocalPath = writeMasterResourceImageDataInInternalMemory(studentProfile.userId.toString(),"jpg",studentProfile.photoData)
                                    viewModel.updateStudentProfile(studentProfile.name,studentProfile.emailId,studentProfile.photoLocalPath,studentProfile.userId)
                                    viewModel.updateName(studentProfile.name!!)
                                }
                                progressDialog.dismissLoading()

                                runOnUiThread {
                                    SuccessDialog(this@UpdateProfileActivity,
                                        Constants.Information, getString(R.string.update_success_msg),object: SuccessImp {
                                            override fun onYes(context: Activity, dialog: Dialog?) {
                                                dialog!!.dismiss()
                                            }
                                        }).show(supportFragmentManager, SuccessDialog.TAG)
                                }
                            }
                        }
                        in 300..302 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)}
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(this, Constants.Error, Constants.ServerFailure).show(this.supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(this.supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        binding.ImgBtnCameraCapture.setOnClickListener {
            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(this@UpdateProfileActivity)
            builder.setTitle("Add Photo!")
            builder.setItems(options) { dialog, item ->
                when (item) {
                    0 -> takePhotoFromCamera()
                    1 -> choosePhotoFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            builder.show()
        }
        binding.BtnUpdateProfile.setOnClickListener {
            binding.FullNameValidator.isErrorEnabled = false
            binding.EmailIdValidator.isErrorEnabled = false
            if(binding.EtFullName.text.toString().isBlank())
            {
                binding.FullNameValidator.isErrorEnabled = true
                binding.FullNameValidator.error = "Please Enter Full Name"
            }
            else if(binding.EtEmailId.text.toString().isNotEmpty() && !isValidEmail(binding.EtEmailId.text.toString()))
            {
                binding.EmailIdValidator.isErrorEnabled = true
                binding.EmailIdValidator.error = "Please Enter valid email address"
            }
            else if (!imagePath.isNullOrEmpty()) {
                val file = File(imagePath!!)
                val requestFile = file.absoluteFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("ProfilePhoto", file.name, requestFile)
                val dob = "".toRequestBody("application/json".toMediaTypeOrNull())
                val email = binding.EtEmailId.text.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val name = binding.EtFullName.text.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val userid = userDetail.userId.toRequestBody("application/json".toMediaTypeOrNull())
                viewModel.updateProfile(userid, name, dob, email, body)
            } else
                WarningDialog(this, Constants.Information, getString(R.string.label_enter_profile_image)).show(this.supportFragmentManager, WarningDialog.TAG)
        }

        binding.LinBackNav.setOnClickListener {
            val intent = Intent(this@UpdateProfileActivity, UserProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
            //startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity"))
            //finishAndRemoveTask()
        }
    }
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-.]+)\\.([a-zA-Z]{2,5})$")
        return emailRegex.matches(email)
    }
    private fun choosePhotoFromGallery() {
        if (cameraForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, galleryCapture)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            galleryImageActivity.launch(intent)
        }
        else
            requestPermissionLauncherForGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    private fun takePhotoFromCamera() {
        if (cameraForPermission(Manifest.permission.CAMERA, cameraCapture)) {
            try {
                val values = ContentValues(1)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                picUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri)
                cameraImageActivity.launch(captureIntent)
            } catch (anfe: ActivityNotFoundException) {
                val toast = Toast.makeText(this@UpdateProfileActivity, "This device doesn't support the crop action!", Toast.LENGTH_SHORT)
                toast.show()
            }
        } else requestPermissionLauncherForCamera.launch(Manifest.permission.CAMERA)
    }
    private var requestPermissionLauncherForCamera: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                try {
                    val values = ContentValues(1)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    picUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri)
                    cameraImageActivity.launch(captureIntent)
                } catch (anfe: ActivityNotFoundException) {
                    val toast = Toast.makeText(this@UpdateProfileActivity, "This device doesn't support the crop action!", Toast.LENGTH_SHORT)
                    toast.show()
                }
            } else Toast.makeText(this@UpdateProfileActivity,"permission denied",Toast.LENGTH_LONG).show()
        }
    private var requestPermissionLauncherForGallery: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                intent.flags=Intent.FLAG_GRANT_READ_URI_PERMISSION
                galleryImageActivity.launch(intent)
            } else Toast.makeText(this@UpdateProfileActivity, "permission denied", Toast.LENGTH_SHORT).show()
        }
    private fun compressImage(imageUri: String?): ByteArray {
        val filePath: String? = getRealPathFromURI(imageUri!!)
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        options.inJustDecodeBounds = false
        options.inTempStorage = ByteArray(16 * 1024)
        try {
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()

            when (orientation) {
                6 -> {
                    matrix.postRotate(90f)
                    Log.d("EXIF", "Exif: $orientation")
                }
                3 -> {
                    matrix.postRotate(180f)
                    Log.d("EXIF", "Exif: $orientation")
                }
                8 -> {
                    matrix.postRotate(270f)
                    Log.d("EXIF", "Exif: $orientation")
                }
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
    private fun getRealPathFromURI(contentURI: String): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }

    }
    private var cameraImageActivity: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            progressDialog.startLoading()
            val executorService = Executors.newSingleThreadExecutor()
            executorService.execute {
                val cropImagePath: String? = RealPathUtil.getRealPath(this@UpdateProfileActivity, picUri!!)
                val cropImage = File(cropImagePath!!)
                val bitmap = BitmapFactory.decodeFile(cropImage.absolutePath)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                var byteArray = byteArrayOutputStream.toByteArray()
                if (byteArray.size > 512000) {
                    byteArray = compressImage(cropImagePath)
                }
                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                imagePath=writeMasterResourceImageDataInInternalMemory(userDetail.userId,"jpg",base64Image)
                runOnUiThread{
                    progressDialog.dismissLoading()
                    binding.StudentImage.setImageURI(picUri)
                }
            }
            executorService.shutdown()
        }
    }
    private var galleryImageActivity: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            progressDialog.startLoading()
            val data: Intent? = result.data
            val imageUri = data!!.data
            val executorService =
                Executors.newSingleThreadExecutor()
            executorService.execute {
                val cropImagePath: String? = RealPathUtil.getRealPath(this@UpdateProfileActivity, imageUri!!)
                val cropImage = File(cropImagePath!!)
                val bitmap = BitmapFactory.decodeFile(cropImage.absolutePath)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                var byteArray = byteArrayOutputStream.toByteArray()
                if (byteArray.size > 512000)
                    byteArray = compressImage(cropImagePath)
                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                imagePath=writeMasterResourceImageDataInInternalMemory(userDetail.userId,"jpg",base64Image)
                runOnUiThread{
                    progressDialog.dismissLoading()
                    binding.StudentImage.setImageURI(imageUri)
                }
            }
            executorService.shutdown()
        }
    }
    private fun cameraForPermission(permission: String, requestCode: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this@UpdateProfileActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@UpdateProfileActivity, permission)) ActivityCompat.requestPermissions(this@UpdateProfileActivity, arrayOf(permission), requestCode)
            else ActivityCompat.requestPermissions(this@UpdateProfileActivity, arrayOf(permission), requestCode)
            true
        }
        else {
            false
        }
    }

    private fun downloadAnswerImage(url: String?,urlImage: String): String {
        try {
            val imageUrl = URL("$url$urlImage")
            val uCon = imageUrl.openConnection()
            val `is` = uCon.getInputStream()
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var read = 0
            while (`is`.read(buffer, 0, buffer.size).also { read = it } != -1) {
                baos.write(buffer, 0, read)
            }
            val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(baos.toByteArray())
            baos.flush()
            baos.close()
            return  base64
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    private fun writeMasterResourceImageDataInInternalMemory(filename:String,fileType:String,filDataBase64:String?):String {
        try {
            val assetsDir = File(filesDir, "assets")
            if (!assetsDir.exists()) assetsDir.mkdirs()
            val assetsDirPath = assetsDir.absolutePath
            val assetsDirFile =File("$assetsDirPath/$filename.$fileType")
            val bytes: ByteArray = Base64.decode(filDataBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
            val fOut = FileOutputStream(assetsDirFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return assetsDirFile.absolutePath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    /*private fun saveCaptureImageAndSaveDataInInternalMemory(bitmap: Bitmap,filename:String,fileType:String):String {
        try {
            val assetsDir = File(filesDir, "assets")
            if (!assetsDir.exists()) assetsDir.mkdirs()
            val assetsDirPath = assetsDir.absolutePath
            val assetsDirFile =File("$assetsDirPath/$filename.$fileType")
            val fOut = FileOutputStream(assetsDirFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return assetsDirFile.absolutePath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }*/
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUserDetailEvent(userDetail : UserDetail) {
        val userDetailStickyEvent= EventBus.getDefault().getStickyEvent(UserDetail::class.java)
        if (userDetailStickyEvent != null) {
            this.userDetail=userDetail
        }
    }
}