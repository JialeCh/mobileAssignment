package com.example.mobileassigmentjobplatform.Mypost

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mobileassigmentjobplatform.R
import com.example.mobileassigmentjobplatform.`class`.JobPost
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_edit_post.*
import kotlinx.android.synthetic.main.fragment_post_listing.*
import java.io.IOException
import java.util.*

class EditPost : Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private var filePath: Uri? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private val mAuth = FirebaseAuth.getInstance()
    lateinit var argst: EditPostArgs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_post, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        argst =  EditPostArgs.fromBundle(arguments!!)
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)


    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        showData()
        btnSave.setOnClickListener{
            update()

        }
        imgViewMyPost.setOnClickListener {
            Log.d(Constraints.TAG, "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(getContext()?.getContentResolver(), filePath)
                imgViewMyPost?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showData(){
        Edit_Title.text = Editable.Factory.getInstance().newEditable(argst.titleName)
        Edit_Salary.text =Editable.Factory.getInstance().newEditable(argst.salary)
        Edit_inform.text=Editable.Factory.getInstance().newEditable(argst.inform)
        Edit_Date.text=Editable.Factory.getInstance().newEditable(argst.date)
        Edit_Location.text=Editable.Factory.getInstance().newEditable(argst.location)
        Edit_CompanyName.text=Editable.Factory.getInstance().newEditable(argst.companyName)
        Glide.with(activity!!).load(argst.image)
            .into(imgViewMyPost)
    }

    private fun update(){

        val progressDialog = ProgressDialog(getActivity())
        progressDialog.setTitle("Updating")
        progressDialog.setMessage("Please wait, this may take a while")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val db = FirebaseFirestore.getInstance()
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        var newSalary: String =  Edit_Salary.text.toString()
        var newTitle: String =  Edit_Title.text.toString()
        var newinform: String =  Edit_inform.text.toString()
        var newLocation: String =  Edit_Location.text.toString()
        var newDate: String =  Edit_Date.text.toString()
        var newCompanyName: String =  Edit_CompanyName.text.toString()
        var documentName : String = argst.jobPostID

        db.collection("JobPost").document(documentName)
            .update(
                mapOf(

                "JPcompanyName" to  newCompanyName,
                "JPDate" to  newDate,
                "JPinform" to newinform,
                "JPSalary" to newSalary,
                "JPtitle" to  newTitle,
                "location" to newLocation,
                "userID" to user?.uid.toString()))
            .addOnSuccessListener {
               uploadImage(documentName)

                Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!")
                Toast.makeText(
                    context,
                    "DocumentSnapshot successfully updated!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(EditPostDirections.actionEditPostToMyPostRecycleVeiw(
                    newTitle, newSalary, "", newDate,newinform,newLocation,newCompanyName,documentName,user?.uid.toString()
                ))

                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                Log.w(Constraints.TAG, "Error updating document", e)
                Toast.makeText(context, "Error updating document!", Toast.LENGTH_SHORT)
                    .show()
                progressDialog.dismiss()
            }

    }
    private fun addUploadRecordToDb(uri: String,documentName:String){
        val db = FirebaseFirestore.getInstance()
        val user = mAuth.currentUser
        val uid = user?.uid.toString()
        val data = HashMap<String, Any>()
        data["imageUrl"] = uri
        if( uri != argst.image){
            db.collection("JobPost").document(documentName)
            .update(mapOf(
                "JPImage" to uri
            ))
            .addOnSuccessListener { documentReference ->
                Toast.makeText(context, "Saved to DB", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving to DB", Toast.LENGTH_LONG).show()
            }}

    }
    private fun uploadImage(documentName:String){
        if(filePath != null){
            val ref = storageReference?.child("ProfileUser/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString(),documentName)
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            Toast.makeText(context, "Please Upload an Image", Toast.LENGTH_SHORT).show()

        }
    }


}