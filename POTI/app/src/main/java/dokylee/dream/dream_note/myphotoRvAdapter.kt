package dokylee.dream.dream_note
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class myphotoRvAdapter (val context: Context, val dialogView: View, val items: ArrayList<MovieModel>) : RecyclerView.Adapter<myphotoRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.myphoto_cardview_item, p0, false)
        return ViewHolder(v)
    }
    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        val item = items[position]

        Glide.with(vh.itemView.context)
            .load(item.img_src)  // get URL
            .into(vh.img_src)  // load URL image to the imageview

        vh.img_des?.text = item.img_movie_name

        vh.iv.setOnClickListener {
            Log.d("test", "CLICK :"+position.toString()) // position starts with 0

            val builder = AlertDialog.Builder(context, R.style.MyAlertDialogStyle_photodetail)
            val upload_time = dialogView.findViewById(R.id.photodetail_upload) as TextView
            val img_movie_name = dialogView.findViewById(R.id.photodetail_movie_name) as TextView
            val img_des = dialogView.findViewById(R.id.photodetail_des) as TextView

            val user = FirebaseAuth.getInstance().currentUser
            val databaseRef = FirebaseDatabase.getInstance().getReference()

            upload_time.setText(item.upload_time)
            img_movie_name.setText(item.img_movie_name)
            img_des.setText(item.img_des)

            if (dialogView.getParent() != null)
                (dialogView.getParent() as ViewGroup).removeView(dialogView)

            builder
                .setView(dialogView)
                .setNegativeButton("delete") { dialogInterface, i ->
                    databaseRef.child("users_uploads").child(user!!.uid).child(item.ImageUploadId).removeValue()
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_LONG)
                }
                .show()
        }
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img_src = itemView.findViewById<ImageView>(R.id.myphoto_movie_img)
        val img_des = itemView.findViewById<TextView>(R.id.myphoto_movie_des)

        val iv = itemView
    }
}