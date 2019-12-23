package dokylee.dream.dream_note

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {


    private val SIGN_IN_FIRST = 1000
    val SIGN_IN_PROFILE = 1001
    val FRAGMENT_GALLERY_PERMISSION_REQUEST = 1002
    val FRAGMENT_STORAGE_PERMISSION_REQUEST = 1003


    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private val fragmentManager = supportFragmentManager

    // 4개의 메뉴에 들어갈 Fragment들
    private val menu1Fragment = HomeActivity()
    private val menu2Fragment = OtherphotoActivity()
    private val menu3Fragment = ChatbotActivity()
    private val menu4Fragment = ProfileActivity()


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val user = FirebaseAuth.getInstance().currentUser
        val transaction = fragmentManager.beginTransaction()
        when (item.itemId) {

            R.id.navigation_home -> {
                //textMessage.setText(R.string.title_home)
                transaction.replace(R.id.fragment_container, menu1Fragment).commitAllowingStateLoss();
                //return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_createPlanet -> {
                //textMessage.setText(R.string.title_createplanet)
                transaction.replace(R.id.fragment_container, menu2Fragment).commitAllowingStateLoss();
                //return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_chatbot -> {
                //textMessage.setText(R.string.title_chatbot)
                transaction.replace(R.id.fragment_container, menu3Fragment).commitAllowingStateLoss();
                //return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                //textMessage.setText(R.string.title_profile)
//                val menu4Fragment = ProfileActivity()
                transaction.replace(R.id.fragment_container, menu4Fragment).commitAllowingStateLoss();
                //return@OnNavigationItemSelectedListener true
            }
        }
        true
    }


    /*
     *  Permission request Callback
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            // home_fagment에서 퍼미션 응답
            FRAGMENT_GALLERY_PERMISSION_REQUEST -> {
                Log.d("test","GALLERY forward->fragment func")
                menu1Fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }

            FRAGMENT_STORAGE_PERMISSION_REQUEST -> {
                Log.d("test","STORAGE forward->fragment func")
                menu1Fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        /*navigation bar*/
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // 첫 화면 지정
        val setFirstFragment = fragmentManager.beginTransaction()
        setFirstFragment.replace(R.id.fragment_container, menu1Fragment).commitAllowingStateLoss()

        //textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        /*authentication*/
        //checkPreviousLogin()
    }

    /*
    * Authentication
    * */

    open fun signoutAccount(fragment: Fragment) {
        Log.d("hahaha", "SIGNOUT START")
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("hahaha", "로그아웃 테스크 완료")
                    Log.d("hahaha", "SIGNOUT END")
                    Toast.makeText(this, "Sign out successfully~", Toast.LENGTH_LONG).show()
                    val setFragment = fragmentManager.beginTransaction()
                    Log.d("hahaha", "part1")
                    setFragment.remove(fragment).commitNow()
                    Log.d("hahaha", "part2")
                    Log.d("hahaha", "part3")
                    val m = ProfileActivity()
                    Log.d("hahaha", "part4")
                    setFragment.replace(R.id.fragment_container, m).commitNow()
                    Log.d("hahaha", "part5")
                }
            }
    }

    open fun showLoginWindow(SIGN_IN_CODE: Int) {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.activity_login)
            .setGoogleButtonId(R.id.google_signin_but)
            .setEmailButtonId(R.id.email_signin_but)
            .build()

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAuthMethodPickerLayout(customLayout)
                .setAvailableProviders(providers)
                .setTheme(R.style.AppTheme)
                .build(),
            SIGN_IN_CODE
        )
    }

    /*로그인 결과 받는 부분 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            //home sign in request
            SIGN_IN_PROFILE -> {
                val response = IdpResponse.fromResultIntent(data)

                if (resultCode == Activity.RESULT_OK) {
                    // Successfully signed in

                    // save user info in the database
                    val databaseRef = FirebaseDatabase.getInstance().getReference().child("users")
                    val user = FirebaseAuth.getInstance().currentUser
                    val userInfo = UserInfoModel(user!!.email)

                    databaseRef.child(user.uid).setValue(userInfo)
                        .addOnSuccessListener {
                            Log.d("gggg", "save userInfo successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.d("gggg", "save userInfo ERROR: "+exception)
                        }


                    // update profile fragment
                    val setFragment = fragmentManager.beginTransaction()
                    setFragment.remove(menu4Fragment).commitNow()
                    val p = ProfileActivity()
                    setFragment.replace(R.id.fragment_container, p).commitNow()

                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    Toast.makeText(this, "Sign in failed, try again", Toast.LENGTH_LONG).show()
                }
            }

            //profile sign in request
            SIGN_IN_FIRST -> {
                val response = IdpResponse.fromResultIntent(data)

                if (resultCode == Activity.RESULT_OK) {
                    // Successfully signed in
                    val user = FirebaseAuth.getInstance().currentUser

                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    Toast.makeText(this, "Sign in failed, try again", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}
