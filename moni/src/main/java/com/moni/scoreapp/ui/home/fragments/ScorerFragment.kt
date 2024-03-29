package com.moni.scoreapp.ui.home.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.moni.scoreapp.R
import com.moni.scoreapp.data.local.enums.Genders
import com.moni.scoreapp.data.local.enums.RecordStatus
import com.moni.scoreapp.data.remote.models.Apellido
import com.moni.scoreapp.data.remote.models.Dni
import com.moni.scoreapp.data.remote.models.Email
import com.moni.scoreapp.data.remote.models.Fields
import com.moni.scoreapp.data.remote.models.Genero
import com.moni.scoreapp.data.remote.models.Nombre
import com.moni.scoreapp.data.remote.models.RecordRq
import com.moni.scoreapp.data.remote.models.ScoreRs
import com.moni.scoreapp.data.remote.models.Status
import com.moni.scoreapp.databinding.FragmentScorerBinding
import com.moni.scoreapp.ui.home.HomeActivity
import com.moni.scoreapp.ui.home.HomeViewModel
import com.moni.scoreapp.utils.Others.showErrorSnackbar
import com.moni.scoreapp.utils.ResStatus
import com.moni.scoreapp.utils.Resource
import com.moni.scoreapp.utils.Validators

class ScorerFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentScorerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        setTextChangedListeners()

        binding.homeEvaluate.setOnClickListener {
            binding.homeEvaluate.isEnabled = false
            viewModel.getScore(binding.homeDni.editText!!.text.toString())
        }

        viewModel.scoreStatus.observe(viewLifecycleOwner) {
            val content = it.getContentIfNotHandled()
            binding.scorerLoading.visibility =
                if (content?.status == ResStatus.LOADING) VISIBLE else INVISIBLE
            when (content?.status) {
                ResStatus.SUCCESS -> onScoreRqSuccess(content)

                ResStatus.ERROR -> onStatusError()

                else -> {}
            }
        }

        viewModel.recordStatus.observe(viewLifecycleOwner) {
            val content = it.getContentIfNotHandled()
            if (content?.status == ResStatus.ERROR) onStatusError()
        }
    }

    private fun onStatusError() {
        showErrorSnackbar(
            requireActivity().findViewById(R.id.fragment_score_layout),
            getString(R.string.server_error)
        )
        binding.homeEvaluate.isEnabled = true
    }

    private fun onScoreRqSuccess(content: Resource<ScoreRs>) {
        val status = content.data?.status

        saveRecordRqState(status)
        viewModel.createRecordFirebase(viewModel.recordRq!!)

        if (content.data?.status == RecordStatus.APPROVE) {
            (activity as HomeActivity).goToApprovedFragment()
        } else {
            (activity as HomeActivity).goToRejectedFragment()
        }
    }

    private fun setTextChangedListeners() {
        binding.homeName.editText?.doAfterTextChanged { input ->
            binding.homeName.isErrorEnabled =
                !Validators.validateNameOrLastname(input.toString()) && input.toString()
                    .isNotBlank()
            binding.homeName.error = if (binding.homeName.isErrorEnabled)
                getString(R.string.input_name_error)
            else
                ""
            binding.homeEvaluate.isEnabled = !isAnyFieldInvalid()
        }

        binding.homeLastname.editText?.doAfterTextChanged { input ->
            binding.homeLastname.isErrorEnabled =
                !Validators.validateNameOrLastname(input.toString()) && input.toString()
                    .isNotBlank()
            binding.homeLastname.error = if (binding.homeLastname.isErrorEnabled)
                getString(R.string.input_lastname_error)
            else
                ""
            binding.homeEvaluate.isEnabled = !isAnyFieldInvalid()
        }

        binding.homeDni.editText?.doAfterTextChanged { input ->
            binding.homeDni.isErrorEnabled =
                !Validators.validateDni(input.toString()) && input.toString().isNotBlank()
            binding.homeDni.error = if (binding.homeDni.isErrorEnabled)
                getString(R.string.input_dni_error)
            else
                ""
            binding.homeEvaluate.isEnabled = !isAnyFieldInvalid()
        }

        binding.homeEmail.editText?.doAfterTextChanged { input ->
            binding.homeEmail.isErrorEnabled =
                !Validators.validateEmail(input.toString()) && input.toString().isNotBlank()
            binding.homeEmail.error = if (binding.homeEmail.isErrorEnabled)
                getString(R.string.input_email_error)
            else
                ""
            binding.homeEvaluate.isEnabled = !isAnyFieldInvalid()
        }
    }

    private fun isAnyFieldInvalid() = binding.let { b ->
        b.homeName.let { hn -> hn.isErrorEnabled || hn.editText?.text.isNullOrBlank() } ||
                b.homeLastname.let { ln -> ln.isErrorEnabled || ln.editText?.text.isNullOrBlank() } ||
                b.homeDni.let { dni -> dni.isErrorEnabled || dni.editText?.text.isNullOrBlank() } ||
                b.homeEmail.let { he -> he.isErrorEnabled || he.editText?.text.isNullOrBlank() }
    }

    private fun saveRecordRqState(status: RecordStatus? = null) {
        val selectedRb =
            requireActivity().findViewById(binding.homeRbGroup.checkedRadioButtonId) as RadioButton

        viewModel.recordRq = RecordRq(
            Fields(
                nombre = Nombre(binding.homeName.editText!!.text.toString()),
                apellido = Apellido(binding.homeLastname.editText!!.text.toString()),
                dni = Dni(binding.homeDni.editText?.text.toString()),
                email = Email(binding.homeEmail.editText?.text.toString()),
                genero = Genero(Genders.displayGender(selectedRb.text.toString())),
                status = Status(status)
            )
        )
    }

    private fun recoverRqDataFromVM() {
        val fields = viewModel.recordRq?.fields
        if (fields == null) {
            clearFields()
            return
        }

        binding.homeName.editText?.setText(fields.nombre.stringValue)
        binding.homeLastname.editText?.setText(fields.apellido.stringValue)
        binding.homeDni.editText?.setText(fields.dni.stringValue)
        binding.homeEmail.editText?.setText(fields.email.stringValue)
        when (fields.genero.stringValue) {
            Genders.MALE -> binding.homeRbGroup.check(binding.homeRbM.id)
            Genders.FEMALE -> binding.homeRbGroup.check(binding.homeRbF.id)
            Genders.OTHER -> binding.homeRbGroup.check(binding.homeRbX.id)
        }
    }

    private fun clearFields() {
        binding.homeName.let {
            it.editText?.setText("")
            it.isErrorEnabled = false
        }
        binding.homeLastname.let {
            it.editText?.setText("")
            it.isErrorEnabled = false
        }
        binding.homeDni.let {
            it.editText?.setText("")
            it.isErrorEnabled = false
        }
        binding.homeEmail.let {
            it.editText?.setText("")
            it.isErrorEnabled = false
        }
        binding.homeRbGroup.check(binding.homeRbM.id)
        binding.homeEvaluate.isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        saveRecordRqState()
    }

    override fun onResume() {
        super.onResume()
        recoverRqDataFromVM()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}