package me.alberto.mymiles.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.alberto.mymiles.database.MilesDatabase
import me.alberto.mymiles.databinding.FragmentHomeBinding
import me.alberto.mymiles.repository.MilesRepository

/**
 * A simple [Fragment] subclass.
 */
class HomeFrag : Fragment() {
    private val repository = MilesRepository(MilesDatabase.getDatabase())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentHomeBinding.inflate(inflater, container, false)



        binding.startWalk.setOnClickListener {
            this.findNavController().navigate(HomeFragDirections.actionHomeFragToMapFragment())
        }

        val viewModel = ViewModelProvider(
            this,
            HomeViewModel.Factory(repository)
        ).get(HomeViewModel::class.java)

        binding.recentDataList.adapter = RecentDetailsAdapter()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

}
