package com.errorguys.clientridesharing.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.errorguys.clientridesharing.R


class NavigationView : Fragment() {

    companion object {

        fun newInstance(): NavigationView {
            return NavigationView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation, container, false)
    }
}