package dokylee.dream.dream_note

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.os.Environment
import kotlinx.android.synthetic.main.home_bottomsheet.*
import java.io.IOException
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.home_fragment.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/*
** Firebase rule

*storage: allow all users

*database:
{
    "rules": {
        "users": {
            ".read": "auth != null",
            "$uid": {
                ".read": "auth != null",
                ".write": "$uid === auth.uid"
            }
        }
    }
}

*/

class HomeActivity : Fragment() {

    val FRAGMENT_GALLERY_PERMISSION_REQUEST = 1002
    val FRAGMENT_STORAGE_PERMISSION_REQUEST = 1003
    var FilePathUri: Uri? = null

    val storage = FirebaseStorage.getInstance()
    val database = FirebaseDatabase.getInstance()
    val storageRef = storage.getReference()
    val databaseRef = database.getReference()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val user = FirebaseAuth.getInstance().currentUser

        var view: View = inflater!!.inflate(R.layout.home_fragment, container, false)
        var screenshot_view: View= view.findViewById(R.id.myphoto_screenshot_view) as LinearLayout
        var myphoto_level = view.findViewById(R.id.myphoto_level) as TextView

        if(user == null)
        {
            view = inflater!!.inflate(R.layout.before_login, container, false)
            Toast.makeText(activity, "AFTER logging-in!!", Toast.LENGTH_SHORT).show()
        }

        else {

            /*
                home background
            */
//            // screenshot event
////            val screenshot_but = view.findViewById(R.id.screenshot_but) as Button
////            screenshot_but.setOnClickListener {
////
////                if (PermissionUtil().requestPermission(
////                        requireActivity(), FRAGMENT_STORAGE_PERMISSION_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE
////                    )
////                ) {
////                    saveScreenshot(screenshot_view)
////                }
////
////                Log.d("test", "here")
////            }


            // get current user's specific data
            val recyclerView = view.findViewById<RecyclerView>(R.id.myphoto_recycleview)
            recyclerView.layoutManager = GridLayoutManager(activity, 5)

            databaseRef.child("users_uploads").child(user.uid)

                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val dataList = ArrayList<MovieModel>()
                        var myphoto_count : Int = 0

                        snapshot.children.forEach{ postSnapshot ->

                            myphoto_count += 1

                            val imageUploadInfo = postSnapshot.getValue(MovieModel::class.java)

                            dataList.add(imageUploadInfo!!)
                        }
                        dataList.reverse()

                        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_photodetail, null)
                        val rvAdapter = myphotoRvAdapter(requireContext(), dialogView, dataList)
                        recyclerView.adapter = rvAdapter

                        var myphoto_count_level: String
                        Log.d("test", "myphoto_count"+myphoto_count.toString())

                        when {
                            myphoto_count == 0 -> myphoto_count_level = "아래 서랍을 위로 올려서 포토티켓을 만들어보세요 \uD83E\uDD70"
                            myphoto_count < 5 -> myphoto_count_level = "새싹 포티"
                            myphoto_count < 10 -> myphoto_count_level = "자라나는 새싹 포티"
                            myphoto_count < 15 -> myphoto_count_level = "많이 자란 새싹 포티"
                            myphoto_count < 20 -> myphoto_count_level = "어린이 포티"
                            myphoto_count < 30 -> myphoto_count_level = "뛰어 다니는 어린이 포티"
                            myphoto_count < 40 -> myphoto_count_level = "영화를 읽는 청소년 포티"
                            myphoto_count < 50 -> myphoto_count_level = "성년이 된 청소년 포티"
                            myphoto_count < 60 -> myphoto_count_level = "어른이 포티"
                            myphoto_count < 70 -> myphoto_count_level = "성장한 어른 포티"
                            else -> myphoto_count_level = "멈추지 않는 어른 포티"
                        }

                        if(myphoto_count==0) myphoto_level.setText(myphoto_count_level)
                        else myphoto_level.setText("[ " + myphoto_count_level.toString() + " ] 달성!")
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("gggg", "myphoto loaded ERROR: "+databaseError)
                    }
                })


            /*
                bottomsheet
             */

            Log.d("gggg", user.uid)

            val progressDialog = ProgressDialog(activity)

            val upload_but = view.findViewById(R.id.upload_but) as Button
            val select_but = view.findViewById(R.id.select_but) as Button

            // select button
            select_but.setOnClickListener {

                if (PermissionUtil().requestPermission(
                        requireActivity(), FRAGMENT_GALLERY_PERMISSION_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) openGallery()

            }


            // upload button
            upload_but.setOnClickListener {
                UploadImageFileToFirebaseStorage(view, progressDialog)
            }

        }

        return view

    }

    /*
    * save screenshot func
    * */
    private fun saveScreenshot(view : View) {
        Log.d("test", "saveScreenshot")
        view.setDrawingCacheEnabled(true)
        view.buildDrawingCache()

        val bm :Bitmap = view.getDrawingCache()
        saveBitmap(bm)
    }

    private fun saveBitmap(bitmap: Bitmap) {
        Log.d("test", "saveBitmap")

        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
        Log.d("test", "root: "+root)

        val myDir = File(root, "/POTI")
        Log.d("test", "myDir: "+myDir)

        Log.d("test", "myDir.exists(): "+myDir.exists())

        if(!myDir.exists()){
            myDir.mkdirs()
        }

        Log.d("test", "myDir.exists(): "+myDir.exists())

        var outStream: FileOutputStream? = null

        val fileName = String.format("%s_%d.jpg", "Image", System.currentTimeMillis())
        Log.d("test", "fileName: "+fileName)
        val outFile = File(myDir, fileName)

        Log.d("test", "outFile: "+outFile)

        outStream = FileOutputStream(outFile)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream!!.flush()
        outStream.close()

        Log.d("test", "screenshot saved")

        Toast.makeText(activity, "screenshot saved succussfully", Toast.LENGTH_LONG)
    }

    private fun openGallery() {

        // Creating intent.
        val intent = Intent()

        // Setting intent type as image to select image from phone storage.
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Please Select Image"), FRAGMENT_GALLERY_PERMISSION_REQUEST)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            // 내가 보낸 권한 요청에 대한 답이 왔는지 확인
            FRAGMENT_GALLERY_PERMISSION_REQUEST -> {
                if (PermissionUtil().permissionGranted(requestCode, FRAGMENT_GALLERY_PERMISSION_REQUEST, grantResults)) openGallery()
                else
                {
                    Toast.makeText(activity, "gallery permission denied", Toast.LENGTH_LONG)
                    Log.d("test", "perm denied~")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FRAGMENT_GALLERY_PERMISSION_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            FilePathUri = data.getData()

            try {

                // Getting selected image into Bitmap.
                val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, FilePathUri)
//                val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri)

                // Setting up bitmap selected image into ImageView.
                myphoto_img.setImageBitmap(bitmap)

            } catch (e: IOException) {

                e.printStackTrace()
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    fun GetFileExtension(uri: Uri): String? {

        val contentResolver = activity?.contentResolver

        val mimeTypeMap = MimeTypeMap.getSingleton()

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver?.getType(uri))

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    fun UploadImageFileToFirebaseStorage(view: View, progressDialog :ProgressDialog) {

        val user = FirebaseAuth.getInstance().currentUser

        var bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.home_bottom_sheet) as View)

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Uploading...")

            // Showing progressDialog.
            progressDialog.show()

            // Creating second StorageReference.
            val img_filename = "" + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri!!)
            val storageRef2nd =
                storageRef.child("users_uploads_img").child(user!!.uid).child(img_filename)

            // Adding addOnSuccessListener to second StorageReference.
            storageRef2nd.putFile(FilePathUri!!)

                .addOnSuccessListener { taskSnapshot ->
                    Log.d("gggg", "storage success")

                    // Getting URL of the img
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener{ ImgSrc ->

                        // moviemodel info(1) - current time
                        val date : Date = Calendar.getInstance().time
                        val locale: Locale = Locale.getDefault()
                        val format = "yyyy-MM-dd_HH:mm:ss"
                        val formatter = SimpleDateFormat(format, locale)
                        val tempUploadTime = formatter.format(date)
                        Log.d("gggg", "time: "+tempUploadTime)

                        // moviemodel info(2) - others
                        val ImageUploadId = "img_"+databaseRef.push().key // Getting image upload ID.
                        val tempImgSrc = ImgSrc.toString() // img source URL
                        val tempImgMovieName = myphoto_movie_name.text.toString().trim() // img movie name
                        val tempImgDes = myphoto_des.text.toString().trim() // img description
                        val imageUploadInfo = MovieModel(tempUploadTime, ImageUploadId, tempImgSrc, tempImgMovieName, tempImgDes)


                        Log.d("gggg", ImageUploadId + ":" + imageUploadInfo)

                        val photoRef = storage.getReferenceFromUrl(tempImgSrc)

                        // Adding image upload id s child element into databaseReference.
                        databaseRef.child("users_uploads").child(user.uid).child(ImageUploadId!!).setValue(imageUploadInfo)

                            .addOnSuccessListener {
                                Log.d("gggg", "db success")

                                // Showing toast message after done uploading.
                                Toast.makeText(activity, "Image Uploaded Successfully ", Toast.LENGTH_LONG).show()

                                progressDialog.dismiss()

                                // clear&close the bottom sheet
                                myphoto_des.setText(null)
                                myphoto_movie_name.setText(null)
                                myphoto_img.setImageBitmap(null)
                                FilePathUri = null
                                bottomSheetBehavior.setState(STATE_COLLAPSED)

                                //hide keyboard
                                keyboardUtils().hideKeyboard(view)
                            }

                            .addOnFailureListener { exception ->

                                photoRef.delete()

                                // Hiding the progressDialog.
                                progressDialog.dismiss()

                                // Showing exception erro message.
                                Toast.makeText(activity, "Image Uploaded ERROR: " + exception.message, Toast.LENGTH_LONG).show()
                                Log.d("gggg", "database ERROR")

                            }

                    }
                }

                // If something goes wrong .
                .addOnFailureListener{ exception ->
                    // Hiding the progressDialog.
                    progressDialog.dismiss()

                    // Showing exception error message.
                    Toast.makeText(activity, "Image Uploaded ERROR: " + exception.message, Toast.LENGTH_LONG).show()
                    Log.d("gggg", "storage ERROR")
                }

                // On progress change upload time.
                .addOnProgressListener {
                    // Setting progressDialog Title.
                    progressDialog.setTitle("Image is Uploading...")
                }
        }

        else {

            Toast.makeText(requireContext(), "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show()
            FilePathUri = null

        }
    }

}
