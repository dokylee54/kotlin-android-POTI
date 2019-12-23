package dokylee.dream.dream_note

import android.content.Context
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.profile_fragment.*
import com.firebase.ui.auth.AuthUI
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.home_fragment.*
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_feedback.*
import android.widget.RatingBar




class ProfileActivity : Fragment() {

    private val SIGN_IN_PROFILE = 1001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Log.d("hahaha","ProfileActivity is created")
        val view: View = inflater!!.inflate(R.layout.profile_fragment, container, false)
        val delete_profile = view.findViewById(R.id.delete_profile) as TextView
        val profile_userid = view.findViewById(R.id.profile_userid) as TextView
        val feedback_but = view.findViewById(R.id.feedback_but) as TextView
        val log_status = view.findViewById(R.id.log_status) as TextView
        val profile_img = view.findViewById(R.id.profile_img) as ImageView

        val user = FirebaseAuth.getInstance().currentUser

        if(user==null){
            //Log.d("hahaha","user NULL")
            delete_profile.setText("AFTER log-in")
            profile_userid.setText("login!!")
            log_status.setText(R.string.login)

            log_status.setOnClickListener {
                (activity as MainActivity).showLoginWindow(SIGN_IN_PROFILE)
            }
        }
        else {
            //Log.d("hahaha","user login")
            Glide.with(activity)
                .load(user.photoUrl)  // get URL
                .into(profile_img)  // load URL image to the imageview

            profile_userid.setText(user?.email)
            log_status.setText(R.string.logout)

            log_status.setOnClickListener {
                (activity as MainActivity).signoutAccount(this)
            }

            delete_profile.setOnClickListener {

                //delete alarm
                val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
                val dialogView = layoutInflater.inflate(R.layout.dialog_delete_user, null)

                builder
                    .setView(dialogView)
                    .setPositiveButton("ok") { dialogInterface, i ->

                        val databaseRef = FirebaseDatabase.getInstance().getReference()
                        databaseRef.child("users").child(user!!.uid).removeValue()

                        user.delete()
                            .addOnSuccessListener {
                                Log.d("gggg", "deleted user successfully ㅠㅠ")
                            }
                            .addOnFailureListener {
                                Log.d("gggg", "deleted user error")
                            }

                        (activity as MainActivity).signoutAccount(this)

                    }
                    .setNegativeButton("cancel") { dialogInterface, i ->
                        /* 취소일 때 아무 액션이 없으므로 빈칸 */
                    }
                    .show()

            }

            feedback_but.setOnClickListener {

                //delete alarm
                val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
                val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
                val feedback_rating = dialogView.findViewById(R.id.feedback_rating) as RatingBar
                val feedback_content = dialogView.findViewById(R.id.feedback_content) as EditText

                builder
                    .setView(dialogView)
                    .setPositiveButton("send") { dialogInterface, i ->

                        // send
                        Log.d("gggg", "rating: "+feedback_rating.rating.toString())

                        val feedbackInfo = feedbackModel(user.uid, feedback_rating.rating.toString(), feedback_content.text.toString().trim())
                        val databaseRef = FirebaseDatabase.getInstance().getReference()
                        databaseRef.child("feedback").setValue(feedbackInfo)
                            .addOnSuccessListener {

                                Toast.makeText(activity, "Thank you for your feedback!!", Toast.LENGTH_LONG).show()
                                Log.d("gggg", "feedback ok")
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(activity, "Feedback ERROR: "+exception, Toast.LENGTH_LONG).show()
                                Log.d("gggg", "feedback ERROR")
                            }

                    }
                    .setNegativeButton("cancel") { dialogInterface, i ->
                        /* 취소일 때 아무 액션이 없으므로 빈칸 */
                    }
                    .show()

            }
        }

        //Log.d("hahaha","before return")
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        //Log.d("hahaha", "ProfileActivity is destoryed")
    }

    override fun onDetach() {
        super.onDetach()
        //Log.d("hahaha", "ProfileActivity is detached")
    }

}