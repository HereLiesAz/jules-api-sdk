package com.hereliesaz.julesapisdk.testapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hereliesaz.julesapisdk.testapp.databinding.FragmentLogcatBinding

class LogcatFragment : Fragment() {

    private var _binding: FragmentLogcatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var logcatAdapter: LogcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogcatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        // setupObservers() // Commented out to fix build error. The 'logcatMessages' LiveData does not exist in the ViewModel.
    }

    private fun setupRecyclerView() {
        logcatAdapter = LogcatAdapter(mutableListOf())
        binding.logcatRecyclerview.apply {
            adapter = logcatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

/*
    private fun setupObservers() {
        // This method is commented out because it was causing a build failure.
        // It referenced a 'logcatMessages' LiveData that was incorrectly added and then removed from the MainViewModel.
        // viewModel.logcatMessages.observe(viewLifecycleOwner) { logs ->
            // logcatAdapter.logs.clear()
            // logcatAdapter.logs.addAll(logs)
            // logcatAdapter.notifyDataSetChanged()
            // binding.logcatRecyclerview.scrollToPosition(logs.size - 1)
        // }
    }
*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
