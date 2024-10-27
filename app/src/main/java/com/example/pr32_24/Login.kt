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
import com.example.pr32_24.databinding.FragmentLoginBinding

class Login : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            binding.registerTextView.setOnClickListener {
                findNavController().navigate(R.id.action_LoginFragment_to_RegistrationFragment)
            }
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authenticateUser(username, password)

        }
    }

    private fun authenticateUser(username: String, password: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("user_data", null)

        if (json != null) {
            val gson = Gson()
            val savedUser = gson.fromJson(json, User::class.java)

            if (savedUser.username == username && savedUser.password == password) {
                Toast.makeText(requireContext(), "Добро пожаловать, $username!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.log_to_main)
            } else {
                Toast.makeText(requireContext(), "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
