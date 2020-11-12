package com.hajarslamah.criminalintent_2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import java.text.DateFormat
import java.util.*

class CrimeListFragment:Fragment() {
    /**     * Required interface for hosting activities     */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)    }
    private var callbacks: Callbacks? = null
    //////////////////////////////////////////@onAttach////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?    }
    /////////////////////////////////////////////onDetach/////////////////////
    override fun onDetach() {
        super.onDetach()
        callbacks = null    }
    ///////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }
    /////////////////////////////////////////////
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    //////////////////////////////////////////////////////
    private lateinit var crimeRecyclerView:RecyclerView
    private lateinit var noDataView: TextView
    private lateinit var newCrimeButton: Button
  // private var adapter:CrimeAdapter?=null
 // private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
   private var adapter: CrimeAdapter? = CrimeAdapter()
    /////////////////////////////////////ViewModel/////////////////////////////////////////
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)    }

///////////////////////////////////////////////////////////////
      override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
}
////////////////////////////////////StaticInstance///////////////////////////////
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
 ///////////////////////////////////////OncreateView/////////////////////
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view=inflater.inflate(R.layout.crime_list_fragment,container,false)
        crimeRecyclerView =
            view.findViewById(R.id.crime_recycler_view) as RecyclerView
     noDataView=
         view.findViewById(R.id.empty_view) as TextView
     newCrimeButton=
         view.findViewById(R.id.new_crime_button) as Button

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
     crimeRecyclerView.adapter = adapter
     //  updateUI()
   return  view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                   // Log.i(TAG, "Got crimes ${crimes.size}")
        updateUI(crimes)
                }
    })
        ///////////////////////////////////Add new crime using Button If the View empty
        newCrimeButton.setOnClickListener {
            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
        }
    }
 /////////////////////////////////////////Fun UpdateUI///////////////////
   // private fun updateUI() {
 private fun updateUI(crimes: List<Crime>) {
   //  val crimes = crimeListViewModel.crimes
     if (crimes.isEmpty()) {
         crimeRecyclerView.setVisibility(View.GONE)
         newCrimeButton.setVisibility(View.VISIBLE)
         noDataView.setVisibility(View.VISIBLE)

     }
     else {
         adapter = CrimeAdapter()
         crimeRecyclerView.setVisibility(View.VISIBLE)
         noDataView.setVisibility(View.GONE)
         newCrimeButton.setVisibility(View.GONE)
    crimeRecyclerView.adapter = adapter
      adapter= crimeRecyclerView.adapter as CrimeAdapter
     adapter?.submitList(crimes)
    }}
/////////////////////////////////////ViewHolder/////////////////
    private  inner class CrimeViewHolder(view:View):  RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var crime: Crime
         var solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
          val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
         val dateTextView: TextView = itemView.findViewById(R.id.crime_date)

        init {
            itemView.setOnClickListener(this)
        }
     fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.getDateInstance(DateFormat.FULL).format(this.crime.date).toString()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        override fun onClick(v: View?) {
//            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT)
//                .show()
            callbacks?.onCrimeSelected(crime.id)
          
//                val fragment = CrimeFragment.newInstance(crime.id)
//                val fm = activity?.supportFragmentManager
//                fm?.beginTransaction()
//                    ?.replace(R.id.fragment_container, fragment)
//                    ?.commit()
//
       }
    }
 /////////////////////////////////////Adapter//////////////////////////////
 //  androidx.recyclerview.widget.ListAdapter<Crime, CrimeHolder>   RecyclerView.Adapter<CrimeViewHolder>() {
    private inner class CrimeAdapter : ListAdapter<Crime,CrimeViewHolder>(CrimeDiffUtil()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : CrimeViewHolder {

            val crimeNormalView = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
             return CrimeViewHolder(crimeNormalView)

        }

        //override fun getItemCount(){
       //     if (crime.isEmpty()) {

         //   }
       // }

        override fun onBindViewHolder(holder: CrimeViewHolder, position: Int) {
            val crime = getItem(position)
            holder.bind(crime)
        }
    }
    class CrimeDiffUtil:DiffUtil.ItemCallback<Crime>(){
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
         return oldItem.id===newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            var sameId=oldItem.id==newItem.id
            var sameTitle=oldItem.title==newItem.title
            var sameIsSolved=oldItem.isSolved==newItem.isSolved
            var sameDate=oldItem.date==newItem.date
            var sameItem=(sameId && sameTitle && sameIsSolved  &&sameDate)
            return sameItem

        }




    }
}

