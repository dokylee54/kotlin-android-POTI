package dokylee.dream.dream_note

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil {

    private val TAG = "PermissionUtil"

    fun requestPermission(activity: Activity, requestCode : Int, vararg permissions : String) : Boolean
    {
        var granted = true
        var permissionNeeded = ArrayList<String>()  // 얻어야 하는 권한 담는 곳

        permissions.forEach {

            val permissionCheck = ContextCompat.checkSelfPermission(activity, it)
            val hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED // true : 모든 권한 만족

            granted = granted and hasPermission //hasPermission에 따라서 granted가 변함

            if(!hasPermission) {
                permissionNeeded.add(it)
            }
        }

        if(granted) return true

        else {

            //다 있는게 아니라면 권한 요청 작업
            ActivityCompat.requestPermissions(
                activity, permissionNeeded.toTypedArray(), requestCode  //toTypedArray : arraylist -> string 배열 변환
            )

            return false
        }
    }

    //얻어졌는지 확인하는 함수
    fun permissionGranted (requestCode: Int, permissionCode: Int, grantResults: IntArray): Boolean
    {
        return requestCode == permissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }



}