package com.niya.gps.presentation.fragment

import android.os.Bundle
import android.view.View
import com.niya.gps.utils.ViewBindingFragment
import com.niya.gps.databinding.FragmentTracksBinding

class TracksFragment :
    ViewBindingFragment<FragmentTracksBinding>(FragmentTracksBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}