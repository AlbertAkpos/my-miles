package me.alberto.mymiles.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_home.view.*
import me.alberto.mymiles.R

/**
 * A simple [Fragment] subclass.
 */
class HomeFrag : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        view.start_walk.setOnClickListener {
            this.findNavController().navigate(HomeFragDirections.actionHomeFragToMapFragment())
        }
        return view
    }

}
