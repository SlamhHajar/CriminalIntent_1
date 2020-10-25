package com.hajarslamah.criminalintent_2

import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_Time = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 2
class CrimeFragment:Fragment(),DatePickerFragment.Callbacks,TimePickerFragment.Callbacks {
    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private lateinit var crime:Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)    }
    //////////////////////////////////////////////////////
    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
    /////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
        //  Log.d(TAG, "args bundle crime ID: $crimeId")
        // Eventually, load crime from database    }
    }





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       // return super.onCreateView(inflater, container, savedInstanceState)
val view=inflater.inflate(R.layout.fragment_crime,container,false)
      titleField = view.findViewById(R.id.crime_title) as EditText
         dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
           solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

//            dateButton.apply {
//               text = crime.date.toString()
//                isEnabled = false
//                             }
                          return view
              }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
        updateUI()
                }
            })
    }
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
       // timeButton.text=crime.date.toString()
        //solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect    }
    }
    //////////////////////////////////Get 4 parmeter to report
    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        }
        else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        ////////////////Suspect/////////////////////
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }
///////////////////////////////////
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when {
        resultCode != Activity.RESULT_OK -> return
        requestCode == REQUEST_CONTACT && data != null -> {
            val contactUri: Uri? = data.data
            val queryFields= arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            val cursor = requireActivity().contentResolver
                .query(contactUri!!, queryFields, null, null, null)
            cursor?.use {
                 if (it.count == 0) {
                 return
                 }
                it.moveToFirst()

                val suspect = it.getString(0)
                crime.suspect=suspect
                crimeDetailViewModel.saveCrime(crime)
                suspectButton.setText(crime.suspect)
        }

        }}}
    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onStart() {
        super.onStart()
         val titleWatcher = object : TextWatcher {
              override fun beforeTextChanged(
                 sequence: CharSequence?,
                   start: Int,
                    count: Int,
                    after: Int
                 )
              {
                // This space intentionally left blank
            }


             override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
            }
        }
        dateButton.setOnClickListener {
           // DatePickerFragment().apply {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_Time)
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent->
               // startActivity(it)
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            ////////////////////////To sure if the app is exist or not//////////
            //pickContactIntent.addCategory(Intent.CATEGORY_HOME)
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onTimeSelected(time: Date) {
      //  crime.date.time=time
        val pattern = "h:mm a"
      val simpleDateFormat = SimpleDateFormat(pattern)
        val timeShow = simpleDateFormat.format(time)
       timeButton.text=timeShow
//        timeButton.text = SimpleDateFormat("HH:mm",Locale.getDefault()).format(time);

    }
}