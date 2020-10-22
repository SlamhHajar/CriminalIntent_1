package com.hajarslamah.criminalintent_2

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*
private const val ARG_DATE = "date"
class DatePickerFragment : DialogFragment(){
    interface Callbacks {
        fun onDateSelected(date: Date)
    }
    val dateListener = DatePickerDialog.OnDateSetListener {
            _: DatePicker, year: Int, month: Int, day: Int ->  // ( _ :)To the variable is ignored var and do n't use it
        val resultDate : Date = GregorianCalendar(year, month, day).time
        targetFragment?.let { fragment ->
            (fragment as Callbacks).onDateSelected(resultDate)
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear=calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val hours=calendar.get(Calendar.HOUR)
        val minutes=calendar.get(Calendar.MINUTE)
        return DatePickerDialog(requireContext(),dateListener,initialYear,initialMonth,initialDay) //hours,minutes
    }
    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}