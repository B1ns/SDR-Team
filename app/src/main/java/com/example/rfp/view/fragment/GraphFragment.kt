package com.example.rfp.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rfp.R
import com.example.rfp.databinding.FragmentBlueToothBinding

class GraphFragment : Fragment() {

    private var _binding: FragmentBlueToothBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentBlueToothBinding.inflate(inflater, container, false)
        return binding.root
    }


}