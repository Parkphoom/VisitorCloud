package com.wacinfo.visitorcloud.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.databinding.ActivityAdminMenuBinding


class AdminMenuActivity : AppCompatActivity(), View.OnClickListener {

    private val binding: ActivityAdminMenuBinding by lazy {
        ActivityAdminMenuBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.visitorBtn.setOnClickListener(this)


    }

    override fun onClick(view: View?) {
       if(view == binding.visitorBtn){
           val intent = Intent(this@AdminMenuActivity, MainActivity::class.java)
           intent.addFlags(
               Intent.FLAG_ACTIVITY_CLEAR_TOP
           )
           startActivity(intent)
           finish()

       }
    }



}