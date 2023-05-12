package com.cyberflow.dauthsdk.view

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.databinding.ActivityAuthLayoutBinding
import java.util.zip.Inflater

class DAuthActivity: AppCompatActivity() {

    private var _binding: ActivityAuthLayoutBinding ?= null
    private val binding: ActivityAuthLayoutBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }


}