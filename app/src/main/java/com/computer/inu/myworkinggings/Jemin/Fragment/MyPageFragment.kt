package com.computer.inu.myworkinggings.Jemin.Fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.computer.inu.myworkinggings.Jemin.Activity.PasswdModifyActivity
import com.computer.inu.myworkinggings.Jemin.Get.Response.GetOtherInformResponse
import com.computer.inu.myworkinggings.Moohyeon.Data.UserPageData
import com.computer.inu.myworkinggings.Moohyeon.get.GetMypageResponse
import com.computer.inu.myworkinggings.Network.ApplicationController
import com.computer.inu.myworkinggings.Network.NetworkService
import com.computer.inu.myworkinggings.R
import com.computer.inu.myworkinggings.Seunghee.Activity.ProfileSettingMenuActivity
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import com.computer.inu.myworkinggings.Jemin.Adapter.GuestActAdapter
import com.computer.inu.myworkinggings.Jemin.Get.Response.GetOtherActiveResponse
import com.computer.inu.myworkinggings.Jemin.Get.Response.GetProfileImgUrlResponse
import com.computer.inu.myworkinggings.Seunghee.db.SharedPreferenceController
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*

class MyPageFragment : Fragment() {
    val networkService: NetworkService by lazy {
        ApplicationController.instance.networkService
    }
    var image : String? = ""
    var name : String = " "
    var job : String = ""
    var company : String = ""
    var field : String = ""
    var status : String = ""
    var coworkingEnabled : Int = 0
    var checkFlag : Int = 0
    var keword : String= ""
    var profileImgUrl : String = ""
    var userID : Int = 0

    var my_or_other_flag : Int = 0

    // 처음 프래그먼트 추가
    fun addFragment(fragment : Fragment){
        val fm = childFragmentManager
        val transaction = fm.beginTransaction()
        val myIntroFragment = MypageIntroFragment()
        val bundle = Bundle()
        bundle.putString("name", name)
        bundle.putString("job", job)
        bundle.putString("company", company)
        bundle.putString("image", image)
        bundle.putString("field", field)
        bundle.putInt("userID", userID)
        bundle.putInt("my_or_other_flag", my_or_other_flag)
        Log.v("asdf", "보내는필드 = " + field)
        bundle.putString("status", status)
        bundle.putInt("coworkingEnabled", coworkingEnabled)
        myIntroFragment.setArguments(bundle)
        transaction.add(R.id.mypage_content_layout, myIntroFragment)
        transaction.commit()
    }

    // 프래그먼트 교체
    fun replaceFragment(fragment: Fragment, checkFlag : Int) {
        val fm = childFragmentManager
        val transaction = fm.beginTransaction()

        if(checkFlag == 0){
            val myIntroFragment = MypageIntroFragment()
            val bundle = Bundle()
            bundle.putString("name", name)
            bundle.putString("job", job)
            bundle.putString("company", company)
            bundle.putString("image", image)
            bundle.putString("field", field)
            bundle.putInt("my_or_other_flag", my_or_other_flag)
            bundle.putInt("userID", userID)
            Log.v("asdf", "보내는필드 = " + field)
            bundle.putString("status", status)
            bundle.putInt("coworkingEnabled", coworkingEnabled)
            myIntroFragment.setArguments(bundle)
            transaction.replace(R.id.mypage_content_layout, myIntroFragment)
            transaction.commit()
        }
        else{
            val myactFragment = MypageActFragment()
            val bundle = Bundle()
            bundle.putString("name", name)
            bundle.putInt("my_or_other_flag", my_or_other_flag)
            bundle.putInt("userID", userID)
            myactFragment.setArguments(bundle)
            transaction.replace(R.id.mypage_content_layout, myactFragment)
            transaction.commit()
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v : View = inflater.inflate(R.layout.fragment_my_page,container,false)
        v.mypage_act_view.visibility = View.INVISIBLE

        val extra = arguments
        if(extra != null){
            userID = extra!!.getInt("userID")
            my_or_other_flag = 1
            Log.v("MypageFragent", "받아온 유저 아이디 = " +userID)
            getOtherPage()
        }
        else{
            my_or_other_flag = 0
            getUserPagePost()
        }

        v.mypage_act_btn.setTextColor(Color.parseColor("#bcc5d3"))
        v.mypage_intro_btn.setTextColor(Color.parseColor("#f7746b"))

        // '소개' 클릭 시
        v.mypage_intro_btn.setOnClickListener {
            Log.v("asdf","소개 클릭")
            mypage_act_btn.isSelected = false
            mypage_intro_btn.isSelected = true
            mypage_act_btn.setTextColor(Color.parseColor("#bcc5d3"))
            mypage_intro_btn.setTextColor(Color.parseColor("#f7746b"))
            mypage_intro_view.setVisibility(View.VISIBLE)
            mypage_act_view.setVisibility(View.INVISIBLE)
            checkFlag=0
            replaceFragment(MypageIntroFragment(), checkFlag)
        }

        // '활동' 클릭 시
        v.mypage_act_btn.setOnClickListener {
            Log.v("asdf","활동 클릭")
            mypage_act_btn.isSelected = true
            mypage_intro_btn.isSelected = false
            mypage_intro_btn.setTextColor(Color.parseColor("#bcc5d3"))
            mypage_act_btn.setTextColor(Color.parseColor("#f7746b"))
            mypage_act_view.setVisibility(View.VISIBLE)
            mypage_intro_view.setVisibility(View.INVISIBLE)
            checkFlag=1

            replaceFragment(MypageActFragment(), checkFlag)
        }

        v.iv_btn_my_page_setting.setOnClickListener {
            var intent = Intent(activity, ProfileSettingMenuActivity::class.java)
            intent.putExtra("profileImgUrl", profileImgUrl)
            intent.putExtra("name",mypage_name_tv.text)
            startActivity(intent)
        }


        return v
    }


    fun getOtherPage() {
        var getOtherInformResponse = networkService.getOtherPageInform( SharedPreferenceController.getAuthorization(context!!),userID) // 유저아이디의 타인 유저 아이디 추가
        getOtherInformResponse.enqueue(object : Callback<GetOtherInformResponse> {
            override fun onResponse(call: Call<GetOtherInformResponse>?, response: Response<GetOtherInformResponse>?) {
                Log.v("TAG", "타인페이지 서버 통신 연결")
                if (response!!.isSuccessful) {
                    Log.v("TAG", "타인페이지 서버 통신 연결 성공")
                    if (response!!.body()!!.message == "자격 없음") {
                        iv_btn_my_page_setting.visibility = View.GONE
                    } else {
                        iv_btn_my_page_setting.visibility = View.VISIBLE
                    }
                    Log.v("asdf", "응답 바디 = " + response.body().toString())
                    name = response.body()!!.data.name!!
                    mypage_name_tv.text = name
                    Glide.with(context).load(response.body()!!.data.image).into(mypage_background_img)

                    if(response.body()!!.data.status!! == "NONE"){
                        status = "-"
                    }
                    else{
                        status = response.body()!!.data.status!!
                    }
                    if (response.body()!!.data.image != null) {
                        Glide.with(context).load(response.body()!!.data.image).centerCrop().into(mypage_background_img)
                        image = response.body()!!.data.image!!
                    }
                    if(response.body()!!.data.field!! == "\"\"")
                    {
                        field = "-"
                    }
                    else{
                        field = response.body()!!.data.field!!
                    }

                    if(response.body()!!.data.image == "")
                        name = response.body()!!.data.name!!
                    if(response.body()!!.data.job!! == "\"\""){
                        mypage_job_tv.text = "역할"
                        job = "역할"
                    }
                    else{
                        job = response.body()!!.data.job!!
                        mypage_job_tv.text = response.body()!!.data.job!!
                    }
                    if(response.body()!!.data.company!! == "\"\""){
                        mypage_team_tv.text = "소속"
                        company = "소속"
                    }
                    else{
                        company=response.body()!!.data.company!!
                        mypage_team_tv.text = response.body()!!.data.company!!
                    }
                    if(response.body()!!.data.region!! == "NONE"){
                        mypage_region_tv.text = "장소"
                    }
                    else{
                        transRegion(response.body()!!.data.region!!)

                    }
                    if (response.body()!!.data.coworkingEnabled == true) {
                        coworkingEnabled = 1
                    } else {
                        coworkingEnabled = 0
                    }

                    for (i in 0..response.body()!!.data.keywords.size - 1) {
                        if (i == 0) {
                            mypage_keyword_tv.text = "#" + response.body()!!.data.keywords[i]
                        } else {
                            mypage_keyword_tv.append("    #" + response.body()!!.data.keywords[i])
                        }
                    }
                    addFragment(MypageIntroFragment())
                }
                else{
                    Log.v("TAG", "타인페이지 서버 값 전달 실패")
                }
            }

            override fun onFailure(call: Call<GetOtherInformResponse>?, t: Throwable?) {
                Log.v("TAG", "타인페이지 서버 통신 실패 = " + t.toString())
            }
        })
    }
    fun getUserPagePost(){
        var getMypageResponse: Call<GetMypageResponse> = networkService.getMypageResponse("application/json", SharedPreferenceController.getAuthorization(context!!))
        getMypageResponse.enqueue(object : Callback<GetMypageResponse> {
            override fun onResponse(call: Call<GetMypageResponse>?, response: Response<GetMypageResponse>?) {
                Log.v("TAG", "보드 서버 통신 연결")
                if (response!!.isSuccessful) {
                    name = response.body()!!.data.name!!
                    mypage_name_tv.text = name
                    if(response.body()!!.data.job!! == "\"\""){
                        mypage_job_tv.text = "역할"
                        job = "역할"
                    }
                    else{
                        job = response.body()!!.data.job!!
                        mypage_job_tv.text = response.body()!!.data.job!!
                    }
                    if(response.body()!!.data.company!! == "\"\""){
                        mypage_team_tv.text = "소속"
                        company = "소속"
                    }
                    else{
                        company=response.body()!!.data.company!!
                        mypage_team_tv.text = response.body()!!.data.company!!
                    }
                    if(response.body()!!.data.region!! == "NONE"){
                        mypage_region_tv.text = "장소"
                    }
                    else{
                        transRegion(response.body()!!.data.region!!)
                    }

                    if (response.body()!!.data.image != null) {
                        Glide.with(context).load(response.body()!!.data.image).centerCrop().into(mypage_background_img)
                        image = response.body()!!.data.image!!
                    }
                    if(response.body()!!.data.field!! == "\"\"")
                    {
                        field = "-"
                    }
                    else{
                        field = response.body()!!.data.field!!
                    }
                    if(response.body()!!.data.status!! == "NONE"){
                        status = "-"
                    }
                    else{
                        status = response.body()!!.data.status!!
                    }

                    name = response.body()!!.data.name!!
                    if (response.body()!!.data.coworkingEnabled!! == true) {
                        coworkingEnabled = 1
                    } else {
                        coworkingEnabled = 0
                    }


                    for (i in 0..response.body()!!.data.keywords.size - 1) {
                        if (i == 0) {
                            mypage_keyword_tv.text = "#" + response.body()!!.data.keywords[i]
                        } else {
                            mypage_keyword_tv.append("    #" + response.body()!!.data.keywords[i])
                        }
                    }
                    addFragment(MypageIntroFragment())
                }
                else{
                    Log.v("TAG", "마이페이지 서버 값 전달 실패")
                }
            }
            override fun onFailure(call: Call<GetMypageResponse>?, t: Throwable?) {

                Log.v("TAG", "통신 실패 = " +t.toString())
            }
        })
    }

    fun getProfileImgUrl() {
        var getProfileImgUrlResponse = networkService.getProfileImgUrl("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjksInJvbGUiOiJVU0VSIiwiaXNzIjoiR2luZ3MgVXNlciBBdXRoIE1hbmFnZXIiLCJleHAiOjE1NDkwODg1Mjd9.P7rYzg9pNtc31--pL8qGYkC7cx2G93HhaizWlvForfg") // 네트워크 서비스의 getContent 함수를 받아옴
        getProfileImgUrlResponse.enqueue(object : Callback<GetProfileImgUrlResponse> {
            override fun onResponse(call: Call<GetProfileImgUrlResponse>?, response: Response<GetProfileImgUrlResponse>?) {
                Log.v("TAG", "프로필 이미지 서버 통신 연결")
                if (response!!.isSuccessful) {
                    Log.v("TAG", "프로필 이미지 조회 성공")
                    Glide.with(context).load(response.body()!!.data.image).centerCrop().into(mypage_background_img)
                    profileImgUrl = response.body()!!.data.image
                }
            }

            override fun onFailure(call: Call<GetProfileImgUrlResponse>?, t: Throwable?) {
                Log.v("TAG", "프로필 이미지 서버 연결 실패 = " + t.toString())
            }
        })
    }

    fun transRegion(regionValue : String){
        var selectedRegion : String = ""
        selectedRegion = regionValue
        if(regionValue == "SEOUL"){
            selectedRegion = "서울"
        }
        else if(regionValue == "SEJONG"){
            selectedRegion = "세종"
        }
        else if(regionValue == "BUSAN"){
            selectedRegion = "부산"
        }
        else if(regionValue == "DAEGU"){
            selectedRegion = "대구"
        }
        else if(regionValue == "DAEJEON"){
            selectedRegion = "대전"
        }
        else if(regionValue!! == "INCHEON"){
            selectedRegion = "인천"
        }
        else if(regionValue!! == "ULSAN"){
            selectedRegion = "울산"
        }
        else if(regionValue!! == "GWANGJU"){
            selectedRegion = "광주"
        }
        else if(regionValue!! == "GANGWON"){
            selectedRegion = "강원"
        }
        else if(regionValue!! == "GYUNGGI"){
            selectedRegion = "경기"
        }
        else if(regionValue!! == "CHUNG_NAM"){
            selectedRegion = "충남"
        }
        else if(regionValue == "CHUNG_BUK"){
            selectedRegion = "충북"
        }
        else if(regionValue!! == "JEON_BUK"){
            selectedRegion = "전북"
        }
        else if(regionValue!! == "JEON_NAM"){
            selectedRegion = "전남"
        }
        else if(regionValue!! == "GYEONG_NAM"){
            selectedRegion = "경남"
        }
        else if(regionValue == "GYEONG_BUK"){
            selectedRegion = "경북"
        }
        else if(regionValue!! == "JEJU"){
            selectedRegion = "제주"
        }
        else{
            selectedRegion = "NONE"
        }
        mypage_region_tv.text = selectedRegion

    }

}