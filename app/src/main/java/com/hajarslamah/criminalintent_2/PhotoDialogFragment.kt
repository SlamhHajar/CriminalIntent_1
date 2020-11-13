package com.hajarslamah.criminalintent_2

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val PHOTO_ARGU = "photo"

/**
 * A simple [Fragment] subclass.
 * Use the [PhotoDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PhotoDialogFragment : DialogFragment() {



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var photoFile=  arguments?.getSerializable(PHOTO_ARGU) as File
        val vew = activity?.layoutInflater?.inflate(R.layout.fragment_photo_dialog, null)
        val photoVew = vew?.findViewById(R.id.photo_view_dialog) as ImageView

        if (photoFile == null || !photoFile.exists()) {
            photoVew.setImageDrawable(null)
        } else {
            var bitmap= getScaledBitmap(photoFile.path, requireActivity())
              photoVew.setImageBitmap(bitmap)
      }
//        if (photoFile.exists()) {
//            var bitmap= getScaledBitmap(photoFile.getPath(), requireActivity())
//           photoVew.setImageBitmap(bitmap)
//        } else {
//            photoVew.setImageDrawable(null)}
//

return AlertDialog.Builder(requireContext())
        .setView(view)
        .setTitle("Crime_Image")
        .setNegativeButton("BACK") { dialog , _ ->
            dialog.cancel()
            }
    .create()
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param photoFile Parameter 1.
         * @return A new instance of fragment PhotoDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(photoFile: File):PhotoDialogFragment {
              return  PhotoDialogFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(PHOTO_ARGU, photoFile)

                    }

                }
    }}
}