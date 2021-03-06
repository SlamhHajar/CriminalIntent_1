package com.hajarslamah.criminalintent_2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.icu.text.DateFormat.FULL
import android.icu.text.DateFormat.getDateInstance

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.text.DateFormat.FULL

import java.util.*

private const val DIALOG_PHOTO = "DialogPhoto"
private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_Time = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 2
private const val REQUEST_DAIL = 3
private const val REQUEST_PHOTO = 4
class CrimeFragment:Fragment(),DatePickerFragment.Callbacks,TimePickerFragment.Callbacks {
    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }
    // private lateinit var treeObserver: ViewTreeObserver

    private lateinit var crime:Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var suspectphone: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
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
        val view=inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        suspectphone = view.findViewById(R.id.suspect_phone) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView


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
                        photoFile = crimeDetailViewModel.getPhotoFile(crime)
                        photoUri = FileProvider.getUriForFile(requireActivity(),
                                "com.hajarslamah.criminalintent_2.fileprovider",
                                photoFile)
                        updateUI()
                    }
                })
    }
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(this.crime.date).toString()
        // timeButton.text=crime.date.toString()
        //solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }
    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path,photoView.width, photoView.height)
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }
    //////////////////////////////////Get 4 parmeter to report
    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        }
        else {
            getString(R.string.crime_report_unsolved)
        }


        val dateString =  java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(this.crime.date).toString()
        ////////////////Suspect/////////////////////
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
            getString(R.string.crime_report_suspect, crime.suspect_phone)
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
                // val queryFields= arrayOf(ContactsContract.Contacts.DISPLAY_NAME,ContactsContract.Contacts._ID)//to get a contact ID on your original query
                val queryFields= arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER)//ContactsContract.CommonDataKinds.Phone That Connect betwen number and Name with out qury to phone number
                val cursor = requireActivity().contentResolver
                        .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    val suspect_phone = it.getString(1) //to get number phone

                    crime.suspect=suspect
                    crime.suspect_phone=suspect_phone //to store number on database
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.setText(crime.suspect)
                    suspectphone.setText(crime.suspect_phone)// to display number on button
                    ////////////////////////////// try with out ContactsContract.CommonDataKinds.Phone on name
                    // val suspect_id = it.getString(1)//ID to connected with phone number to get a contact ID on your original query
                    // val phoneUri= ContactsContract.CommonDataKinds.Phone.CONTENT_URI  //The MIME type of CONTENT_URI providing a directory of phones.
                    //    val  queryPhone=arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    //   var sort=arrayOf(suspect_id)
                    // val cursorPhone = requireActivity().contentResolver
                    // .query(phoneUri, queryPhone, ContactsContract.CommonDataKinds.Phone.CONTACT_ID "= ?",null ,null )
                }

            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }}
    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }
    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
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
        suspectphone.apply {
            val pickContactIntent = Intent(Intent.ACTION_DIAL)
            pickContactIntent.data = Uri.parse("tel:${crime.suspect_phone}")
            setOnClickListener {

                startActivityForResult(pickContactIntent, REQUEST_DAIL)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(pickContactIntent,
                            PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
        photoView.apply {
            viewTreeObserver.apply { if (isAlive) { //Indicates whether this ViewTreeObserver is alive. When an observer is not alive, any call to a method (except this one) will throw an exception.
                addOnGlobalLayoutListener{        //(object : ViewTreeObserver.OnGlobalLayoutListener {
                    updatePhotoView()
                }}
            }
            setOnClickListener{
                PhotoDialogFragment.newInstance(photoFile).apply {
                    show(this@CrimeFragment.requireFragmentManager() , "ZOOM_PHOTO")
                }
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