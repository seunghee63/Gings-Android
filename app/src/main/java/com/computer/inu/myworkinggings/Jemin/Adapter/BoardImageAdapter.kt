package com.computer.inu.myworkinggings.Jemin.Adapter

import android.content.Context

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import com.computer.inu.myworkinggings.Jemin.Activity.MypageUpdateActivity
import com.computer.inu.myworkinggings.Jemin.Data.ImageType
import com.computer.inu.myworkinggings.Moohyeon.Activity.DetailBoardActivity
import com.computer.inu.myworkinggings.R
import com.computer.inu.myworkinggings.Seunghee.Activity.UpBoardActivity
import kotlinx.android.synthetic.main.activity_detail_board.*
import kotlinx.android.synthetic.main.activity_mypage_update.*
import kotlinx.android.synthetic.main.activity_up_board.*


class BoardImageAdapter(var boardImageItem : ArrayList<ImageType>, var requestManager : RequestManager, var insertFlag : Int, var insertUrlorUri : Int, var insertImageUrlSize : Int) : RecyclerView.Adapter<BoardImageViewHolder>() {

    var checkFlag : Int = 0 // 0 = 업보드 등록, 1 = 마이페이지 등록, 2 = 리보드 등록
    var checkUrlorUri : Int = 0 // 0 = url값, 1 = uri값
    var getImageUrlSize : Int = 0
    var urlRemovedCount : Int = 0
    var reboardUrlRemovedCount : Int = 0
    var deleteImageUrlList = ArrayList<String>()

    //내가 쓸 뷰홀더가 뭔지를 적어준다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardImageViewHolder {

        val mainView : View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_board_image, parent, false)
        boardImageAdapter = this
        return BoardImageViewHolder(mainView)
    }

    override fun getItemCount(): Int = boardImageItem.size

    //데이터클래스와 뷰홀더를 이어준다.
    override fun onBindViewHolder(holder: BoardImageViewHolder, position: Int) {
        checkUrlorUri = insertUrlorUri
        getImageUrlSize = insertImageUrlSize
        checkFlag = insertFlag

        // 특정 사진 삭제 버튼  클릭시
        holder.boardImageDeleteBtn.setOnClickListener {

            // 업보드쪽
            if(checkFlag == 0){
                deleteImageUrlList.add(boardImageItem[position].imageUrl!!)
                boardImageItem.removeAt(position)
                if(boardImageItem.size==0){
                    UpBoardActivity.upBoardActivity.upboard_pick_recyclerview.visibility = View.GONE
                }

                // 서버로부터 받은 사진 지우기
                if(position <= getImageUrlSize-1-urlRemovedCount){

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, boardImageItem.size);
                    urlRemovedCount += 1
                }
                // 갤러리에서 올린 사진 지우기
                else{
                    UpBoardActivity.upBoardActivity.imagesList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, boardImageItem.size);
                }
                // 나중에 다시 추가 UpBoardActivity.upBoardActivity.imagesList.removeAt(position)
            }
            // 자기소개쪽
            else if(checkFlag == 1){
                deleteImageUrlList.add(boardImageItem[position].imageUrl!!)
                boardImageItem.removeAt(position)
                if(boardImageItem.size==0){
                    MypageUpdateActivity.mypageUpdateActivity.mypage_update_recyclerview.visibility = View.GONE
                }

                // 서버로부터 받은 사진 지우기
                if(position <= getImageUrlSize-1-urlRemovedCount){

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, boardImageItem.size);
                    urlRemovedCount += 1
                }
                // 갤러리에서 올린 사진 지우기
                else{
                    MypageUpdateActivity.mypageUpdateActivity.postImagesList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, boardImageItem.size);
                }

            }

            // 리보드쪽
            else if(checkFlag == 2){
                if(getImageUrlSize > 0){
                    deleteImageUrlList.add(boardImageItem[position].imageUrl!!)
                    boardImageItem.removeAt(position)
                    if(boardImageItem.size==0){
                        DetailBoardActivity.detailBoardActivity.detail_board_reboard_img_recyclerview.visibility = View.GONE
                    }
                    getImageUrlSize -= 1
                }
                else{
                    boardImageItem.removeAt(position)
                    if(boardImageItem.size==0){
                        DetailBoardActivity.detailBoardActivity.detail_board_reboard_img_recyclerview.visibility = View.GONE
                    }
                    DetailBoardActivity.detailBoardActivity.reboardImagesList.removeAt(position)
                }
                notifyItemRemoved(position)
                notifyDataSetChanged();
            }
        }

        if(checkFlag==0){
            // 서버로부터 받은 이미지 리스트 개수가 0이면
            if(getImageUrlSize == 0){
                // 차례대로 이미지 url을 통해서 리사이클러뷰에 등록
                requestManager.load(boardImageItem[getImageUrlSize+position].imageUri).centerCrop().into(holder.boardImageView)
            }
            // 서버로부터 받은 이미지 리스트 개수가 0보다 크다면
            else{
                // 서버로부터 받은 이미지들부터 먼저 리사이클러뷰에 등록
                if(position <= getImageUrlSize-1-urlRemovedCount) {
                    // 차례대로 이미지 url을 통해서 리사이클러뷰에 등록
                    requestManager.load(boardImageItem[position].imageUrl).centerCrop().into(holder.boardImageView)
                }
                // 그다음 갤러리에서 추가된 이미지 리스트 리사이클러뷰에 등록
                else{
                    // 차례대로 이미지 uri를 통해서 리사이클러뷰에 등록
                    requestManager.load(boardImageItem[position].imageUri).centerCrop().into(holder.boardImageView)
                }
            }

        }
        else if(checkFlag == 1){
            // 서버로부터 받은 이미지 리스트 개수가 0이면
            if(getImageUrlSize == 0){
                // 차례대로 이미지 url을 통해서 리사이클러뷰에 등록
                requestManager.load(boardImageItem[getImageUrlSize+position].imageUri).centerCrop().into(holder.boardImageView)
            }
            // 서버로부터 받은 이미지 리스트 개수가 0보다 크다면
            else{
                // 서버로부터 받은 이미지들부터 먼저 리사이클러뷰에 등록
                if(position <= getImageUrlSize-1-urlRemovedCount) {
                    // 차례대로 이미지 url을 통해서 리사이클러뷰에 등록
                    requestManager.load(boardImageItem[position].imageUrl).centerCrop().into(holder.boardImageView)
                }
                // 그다음 갤러리에서 추가된 이미지 리스트 리사이클러뷰에 등록
                else{
                    // 차례대로 이미지 uri를 통해서 리사이클러뷰에 등록
                    requestManager.load(boardImageItem[position].imageUri).centerCrop().into(holder.boardImageView)
                }
            }
        }
        else if(checkFlag==2){
            if(getImageUrlSize == 0){
                // 차례대로 이미지 url을 통해서 리사이클러뷰에 등록
                requestManager.load(boardImageItem[getImageUrlSize+position].imageUri).centerCrop().into(holder.boardImageView)
            }

            if(getImageUrlSize - urlRemovedCount > 0){
                if(position == 0) {
                    // 차례대로 이미지 url을 통해서 리사이클러뷰에 등록
                    requestManager.load(boardImageItem[position].imageUrl).centerCrop().into(holder.boardImageView)
                }
            }
            else{
                requestManager.load(boardImageItem[position].imageUri).centerCrop().into(holder.boardImageView)

            }
        }
    }

    companion object {
        lateinit var boardImageAdapter: BoardImageAdapter
    }

}
