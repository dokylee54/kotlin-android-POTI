package dokylee.dream.dream_note

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class OtherphotoActivity : Fragment() {

    private val PHOTODATAIL_REQUEST = 900

    open fun dialog_photodetail(cardData : MovieModel){
        Log.d("test","mm: "+cardData.img_movie_name)

        val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle_photodetail)
        val dialogView = layoutInflater.inflate(R.layout.dialog_photodetail, null)
        val upload_time = dialogView.findViewById(R.id.photodetail_upload) as TextView
        val img_movie_name = dialogView.findViewById(R.id.photodetail_movie_name) as TextView

        upload_time.setText(cardData.upload_time)
        img_movie_name.setText(cardData.img_movie_name)

        builder
            .setView(dialogView)
            .show()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view: View = inflater!!.inflate(R.layout.otherphoto_fragment, container, false)

        val user = FirebaseAuth.getInstance().currentUser

        val recyclerView = view.findViewById<RecyclerView>(R.id.otherphoto_recycleview)
        recyclerView.layoutManager = GridLayoutManager(activity, 1)

        val database = FirebaseDatabase.getInstance()
        val databaseRef = database.getReference()

        val progressDialog = ProgressDialog(activity)

        // Setting progressDialog Title.
        progressDialog.setTitle("Loading")

        // Showing progressDialog.
        progressDialog.show()

        val dataList = ArrayList<MovieModel>()

        // read database
        databaseRef.child("users_uploads")

            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    snapshot.children.forEach { postSnapshots ->

                        postSnapshots.children.forEach{ postSnapshot ->

                            val imageUploadInfo = postSnapshot.getValue(MovieModel::class.java)

                            dataList.add(imageUploadInfo!!)

                        }
                    }

                    dataList.shuffle()

                    val dialogView = layoutInflater.inflate(R.layout.dialog_photodetail, null)
                    val rvAdapter = otherphotoRvAdapter(requireContext(), dialogView, dataList)
                    recyclerView.adapter = rvAdapter

                    // Hiding the progress dialog.
                    progressDialog.dismiss()


                    // photo detail

                }

                override fun onCancelled(databaseError: DatabaseError) {

                    Log.d("gggg", databaseError.toString())
                    Toast.makeText(activity, "Feed Loaded ERROR: " + databaseError.message, Toast.LENGTH_LONG).show()

                    // Hiding the progress dialog.
                    progressDialog.dismiss()

                }
            })

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // 내가 보낸 권한 요청에 대한 답이 왔는지 확인
            PHOTODATAIL_REQUEST -> {

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}