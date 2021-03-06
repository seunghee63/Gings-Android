package com.computer.inu.myworkinggings.Moohyeon.Activity

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.computer.inu.myworkinggings.Jemin.Activity.MainActivity
import android.util.Log
import android.widget.Toast
import com.computer.inu.myworkinggings.Network.ApplicationController
import com.computer.inu.myworkinggings.Network.NetworkService
import com.computer.inu.myworkinggings.R
import com.computer.inu.myworkinggings.Seunghee.Post.PostLogInResponse
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.graphics.Color
import android.support.v4.app.FragmentActivity
import android.util.Base64
import android.widget.LinearLayout
import com.computer.inu.myworkinggings.Jemin.Activity.ChatActivity
import com.computer.inu.myworkinggings.Jemin.Activity.ChatActivity.ChatClass.mStompClient
import com.computer.inu.myworkinggings.Jemin.Activity.ChatActivity.ChatClass.sendMessage
import com.computer.inu.myworkinggings.Jemin.Activity.ChatActivity.ChatClass.sendMessageForCreate
import com.computer.inu.myworkinggings.Jemin.Adapter.ChatAdapter
import com.computer.inu.myworkinggings.Jemin.Data.ChatListItem
import com.computer.inu.myworkinggings.Jemin.RealmDB.ChatRoom
import com.computer.inu.myworkinggings.Jemin.RealmDB.User
import com.computer.inu.myworkinggings.Seunghee.db.SharedPreferenceController
import com.google.firebase.iid.FirebaseInstanceId
import com.kakao.util.helper.Utility.getPackageInfo
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.toast
import org.json.JSONArray
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompHeader
import ua.naiksoftware.stomp.client.StompClient
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {
    val FINISH_INTERVAL_TIME = 2000
    var backPressedTime : Long = 0
    var chatData = ArrayList<ChatListItem>()
    // 방 테이블
    var id : Int = 0
    var type : String = ""

    // 유저 테이블 리스트
    var idList = ArrayList<Int>()
    var name = ArrayList<String>()
    var job = ArrayList<String>()
    var image = ArrayList<String>()
    var list_cnt : Int = 0

    // Realm
    lateinit var realm : Realm

    override fun onBackPressed() {
        var tempTime = System.currentTimeMillis()
        var intervalTime = tempTime - backPressedTime

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed()
        } else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    var userID: Int = 0

    val networkService: NetworkService by lazy {
        ApplicationController.instance.networkService
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        pushAlarm()

        if (intent.getStringExtra("sender_id") != null) {
        } else {
        }
        if (SharedPreferenceController.getAutoAuthorization(this).isNotEmpty()) {
            toast("자동로그인 성공")
            startActivity<MainActivity>() // 그사람 정보로 해야함
            finish()
        }
        val boardID = intent.getIntExtra("BoardId", -1)
        if (boardID > 0) {
            Log.v("카카오로그인", "으로들어옴")

            Log.v("Login", "받는 보드 번호 = " + intent.getIntExtra("BoardId", -1))

            val boardID = intent.getIntExtra("BoardId", -1)
            Log.v("Login", "받는 보드 번호 (boardID)= " + boardID)


            if (SharedPreferenceController.getAutoAuthorization(this).isNotEmpty()) {
                if (intent.getStringExtra("clickAction") != null) {
                    if (intent.getStringExtra("clickAction") == "마이페이지") {
                        startActivity<MainActivity>("check" to "마이페이지")
                    } else if (intent.getStringExtra("clickAction") == "detail board") {
                        startActivity<MainActivity>("check" to "detail board", "sender_id" to intent.getStringExtra("sender_id"))
                    }
                } else {
                    Log.v("푸시알람 하는중", boardID.toString())
                    //toast(boardID)
                    if (boardID > 0) {
                        Log.v("카카오로그인", "으로들어옴")
                        startActivity<DetailBoardActivity>("BoardId" to boardID)
                    } else {
                        //***로그인 통신***
                        Log.v("카카오로그인", "으로들어오지않음")
                        Log.v("푸시알람 하는중 잘못 옴", "푸시알람 하는중 잘못 옴")
                        startActivity<MainActivity>()
                        toast("자동로그인 되었습니다.")
                        //sendLink()
                    }

                    //startActivity<MainActivity>() // 그사람 정보로 해야함
                    //finish()
                }
            }

        }

            tv_login_join_us.setOnClickListener {
                startActivity<SignUp1Activity>()
            }
            tv_login_about_gings.setOnClickListener {

            }

            //***로그인 통신***
            tv_login_login_button.setOnClickListener {
                //startActivity<MainActivity>()

                getLoginResponse(boardID)
            }

            //startActivity<BottomNaviActivity>()
            //getLoginResponse()
            //startActivity<BottomNaviActivity>()

            //getKeyHash(applicationContext)
            //Log.v("카카오",getKeyHash(applicationContext))

    }
    fun pushAlarm(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val channelDescription = "Default Channel"
            var notificationChannel = notificationManager.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                val importance = NotificationManager.IMPORTANCE_HIGH //Set the importance level
                notificationChannel = NotificationChannel(channelId, channelDescription, importance)
                notificationChannel!!.setLightColor(Color.GREEN) //Set if it is necesssary
                notificationChannel!!.enableVibration(true) //Set if it is necesssary
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
    fun getKeyHash(context: Context): String? {
        val packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES) ?: return null

        for (signature in packageInfo!!.signatures) {
            try {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
            } catch (e: NoSuchAlgorithmException) {

                //Log.w(this, "Unable to get MessageDigest. signature=$signature", e)
            }

        }
        return null
    }

    //로그인 통신
    private fun getLoginResponse(boardID: Int) {
        if (et_login_id.text.toString().isNotEmpty() && et_login_pw.text.toString().isNotEmpty()) {

            val input_email = et_login_id.text.toString()
            val input_pw = et_login_pw.text.toString()
            val jsonObject: JSONObject = JSONObject()
            jsonObject.put("email", input_email)
            jsonObject.put("pwd", input_pw)
            jsonObject.put("fcm", FirebaseInstanceId.getInstance().getToken().toString()) //fcm 토큰받기
            val gsonObject: JsonObject = JsonParser().parse(jsonObject.toString()) as JsonObject
            Log.v("LoginActivity", "확인")

            val postLogInResponse = networkService.postLoginResponse("application/json", gsonObject)

            postLogInResponse.enqueue(object : Callback<PostLogInResponse> {

                override fun onFailure(call: Call<PostLogInResponse>, t: Throwable) {
                    Log.e("Login fail", t.toString())
                }

                override fun onResponse(call: Call<PostLogInResponse>, response: Response<PostLogInResponse>) {
                    Log.v("LoginActivity", "확인2")
                    if (response.isSuccessful) {
                        Log.v("LoginActivity", "확인3")

                        Log.v("커톡공유해야한다고ㅗㅗㅗㅗ", boardID.toString())
                        //toast(boardID)


                        if (response.body()!!.message == "로그인 성공" && cb_login_auto_check_box.isChecked == true) {


                            val token = response.body()!!.data.jwt.toString()
                            val userId = response.body()!!.data.userId

                            SharedPreferenceController.setAutoAuthorization(this@LoginActivity, token)
                            SharedPreferenceController.setAuthorization(this@LoginActivity, response.body()!!.data.jwt.toString())
                            SharedPreferenceController.setUserId(this@LoginActivity, response.body()!!.data.userId)
                            if (boardID > 0) {

                                startActivity<DetailBoardActivity>("BoardId" to boardID)
                                finish()

                            } else {
                                //***로그인 통신***
                                Log.v("카카오로그인", "으로들어오지않음")

                                startActivity<MainActivity>()
                                finish()
                            }
                            startActivity<MainActivity>()
                            finish()
                        } else if (response.body()!!.message == "로그인 성공" && cb_login_auto_check_box.isChecked == false) {
                            SharedPreferenceController.setAuthorization(this@LoginActivity, response.body()!!.data.jwt.toString())
                            SharedPreferenceController.setUserId(this@LoginActivity, response.body()!!.data.userId)
                            if (boardID > 0) {

                                startActivity<DetailBoardActivity>("BoardId" to boardID)
                                finish()

                            } else {
                                //***로그인 통신***
                                Log.v("카카오로그인", "으로들어오지않음")

                                startActivity<MainActivity>()
                                finish()
                            }
                            startActivity<MainActivity>()
                            finish()
                        } else {
                            toast("회원 정보가 틀렸습니다.")
                        }

                    } else {
                        Log.v("LoginActivity", "확인5")
                    }
                }
            })
        } else {
            Toast.makeText(applicationContext, "빈칸 없이 입력해주세요.", Toast.LENGTH_LONG).show()
        }

    }

}