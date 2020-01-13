package com.example.mobileassigmentjobplatform.Search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileassigmentjobplatform.R
import com.example.mobileassigmentjobplatform.`class`.JobPost
import kotlinx.android.synthetic.main.postlist_row.view.*

class searchAdapter(var jobListItem:ArrayList<JobPost>) : RecyclerView.Adapter<searchAdapter.MyViewHolder>() {


    class MyViewHolder(var view: View) : RecyclerView.ViewHolder(view){
    }


    override fun getItemCount(): Int {
        return jobListItem.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.view.txtSalary.setText(jobListItem[position].getJpSalary().toString())
        holder.view.txtLocation.text=jobListItem[position].getLocation()
        holder.view.txtCompanyName.setText(jobListItem[position].getjpCompanyName())
        Glide.with(holder.view).load(jobListItem[position].getJpImage())
            .into(holder.view.imgView_MyPost)
        holder.view.txtDate.setText(jobListItem[position].getJpDate())
        holder.view.txtTitle.setText(jobListItem[position].getJptitle())

        holder.view.setOnClickListener {
            it.findNavController()
                .navigate(searchFragmentDirections.actionSearchFragmentToDetailFragment(
                    jobListItem[position].getJptitle().toString(),jobListItem[position].getJpSalary().toString(),
                    jobListItem[position].getJpImage().toString(),  jobListItem[position].getJpDate().toString(),jobListItem[position].getJpinform().toString(),jobListItem[position].getLocation().toString()
                    ,jobListItem[position].getjpCompanyName().toString(),jobListItem[position].getjpID().toString(),jobListItem[position].getUserID().toString()))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.postlist_row, parent, false)
        return MyViewHolder(v)


    }




}