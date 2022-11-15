package com.example.zenhabit.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.zenhabit.R
import com.example.zenhabit.databinding.FragmentCreateEditHabitBinding
import com.example.zenhabit.databinding.FragmentCreateEditTaskBinding

/**
 * A simple [Fragment] subclass.
 * Use the [CreateEditTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateEditTaskFragment : Fragment() {

    // View Binding (Fragment)
    private var _binding: FragmentCreateEditTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (View Binding)
        _binding = FragmentCreateEditTaskBinding.inflate(inflater, container, false)
        val view = binding.root

        // binding pels botons 'btn_crearEditarHabit' i 'btn_guardarCrearEditarHabit'
        binding.btnCrearEditarHabit.setOnClickListener {
            findNavController().navigate(R.id.action_createEditTaskFragment_to_createEditHabitFragment)
        }
        binding.btnGuardarCrearEditarTasca.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_tasksFragment2)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Carregar dades XML + crear adaptador pel Dropdown Menu (Categories)
        val categories = resources.getStringArray(R.array.categories)
        val adapter = activity?.let {
            ArrayAdapter(
                it,
                R.layout.list_item, // Carrego layout per mostrar els ítems
                categories
            )
        }

        with(binding.autoCompleteTextView){
            setAdapter(adapter)
        }
    }

}