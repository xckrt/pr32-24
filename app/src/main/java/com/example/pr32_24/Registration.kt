package com.example.pr32_24
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.google.gson.Gson
import com.example.pr32_24.databinding.FragmentRegistrationBinding

class Registration : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmedPass = binding.editTextConfirmPassword.text.toString()
            if (validateInput(username, password, confirmedPass)) {
                saveUserCredentials(username, password)
                Toast.makeText(requireContext(), "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.reg_to_login)
            }
        }
    }

    private fun validateInput(username: String, password: String, confirmedPass: String): Boolean {
        if (username.isBlank()) {
            binding.editTextUsername.error = "Введите Логин"
            return false
        }
        if (password.isBlank()) {
            binding.editTextPassword.error = "Введите пароль"
            return false
        }
        if (password.length < 8) {
            binding.editTextPassword.error = "Длина пароля должна быть не менее 8 символов"
            return false
        }
        if (confirmedPass.isBlank() || confirmedPass != password) {
            binding.editTextConfirmPassword.error = "Пароли не совпадают"
            return false
        }
        return true
    }

    private fun saveUserCredentials(username: String, password: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val user = User(username, password)
        val gson = Gson()
        val json = gson.toJson(user)
        sharedPreferences.edit().putString("user_data", json).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
