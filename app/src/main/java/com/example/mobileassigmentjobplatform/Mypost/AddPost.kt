package com.example.mobileassigmentjobplatform.Mypost

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.mobileassigmentjobplatform.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import kotlinx.android.synthetic.main.fragment_my_post.*
import java.io.IOException
import java.util.*

class AddPost : DialogFragment() {
    private val PICK_IMAGE_REQUEST = 1
    private var filePath: Uri? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_post, container, false)
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val time = Timestamp(Date()).toDate().toString()
        JPDate.text = "Date : "+ time

        btn_Post.setOnClickListener {
            postingJob(time)
        }
        btnExit.setOnClickListener {
            dialog?.dismiss()
        }

    }



    private fun postingJob(time:String) {


        val db = FirebaseFirestore.getInstance()
       
        val user = mAuth.currentUser
        var jpinform: String = Jpinform.text.toString()
        var jpSalary: String = JPsalary.text.toString()
        var jptitle: String = Jptitle.text.toString()
        var location: String = Jplocation.text.toString()
        var JPcompanyName: String = JPcompanyName.text.toString()

        when {

            TextUtils.isEmpty(jpinform) -> Toast.makeText(
                getActivity(),
                "Information is required !!!",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(jpSalary) -> Toast.makeText(
                getActivity(),
                "Salary is required !!!",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(jptitle) -> Toast.makeText(
                getActivity(),
                "Title is required !!!",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(location) -> Toast.makeText(
                getActivity(),
                "Location is required !!!",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(JPcompanyName) -> Toast.makeText(
                getActivity(),
                "Company Name is required !!!",
                Toast.LENGTH_LONG
            ).show()

            else -> {
                val progressDialog = ProgressDialog(getActivity())
                progressDialog.setTitle("Adding The New Post")
                progressDialog.setMessage("Please wait, this may take a while")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                
                val docData = hashMapOf(
                    "JPcompanyName" to JPcompanyName,
                    "JPDate" to time,
                    "JPImage" to "",
                    "JPinform" to jpinform,
                    "JPSalary" to "RM " + jpSalary + "/month",
                    "JPtitle" to jptitle,
                    "location" to location,
                    "userID" to user?.uid

                )

                db.collection("JobPost")
                    .add(docData)
                    .addOnSuccessListener {
                        Log.d(Constraints.TAG, "DocumentSnapshot successfully written!")
                        Toast.makeText(
                            context,
                            "DocumentSnapshot successfully written!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog?.dismiss()
                        progressDialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.w(Constraints.TAG, "Error writing document", e)
                        Toast.makeText(context, "Error writing document!", Toast.LENGTH_SHORT)
                            .show()
                        dialog?.dismiss()
                        progressDialog.dismiss()
                    }

            }
        }

    }



}