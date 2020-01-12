package com.example.mobileassigmentjobplatform.userProfile

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.core.net.toUri

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mobileassigmentjobplatform.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.imagebtn
import kotlinx.android.synthetic.main.fragment_my_post.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.io.IOException
import java.util.*


class EditProfile : Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private var filePath: Uri? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_edit_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        getData()
        btnSave.setOnClickListener{
            updateInfo(filePath.toString())
        }
        btnCancel.setOnClickListener{
            findNavController().navigate(EditProfileDirections.actionEditProfileToUserProfile())
        }
        imagebtn.setOnClickListener {

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
                    imagebtn?.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    private fun getData() {
        val user = mAuth.currentUser
        val uid = user?.uid.toString()

        val docRef = db.collection("User").document(uid)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                var name = document["Name"].toString()
                txtName.text = Editable.Factory.getInstance().newEditable(name)
                var email = document["Email"]
                txtEmail.text = email.toString()
                var gender = document["Gender"].toString()
                if(gender.equals("Male")){
                    rbMale.isChecked =true
                    rbFemale.isChecked =false
                }
                else
                {
                    rbMale.isChecked =false
                    rbFemale.isChecked =true
                }

                var IC = document["IC"].toString()
                txtIC.text = Editable.Factory.getInstance().newEditable(IC)

                var phoneNum = document["Phone Number"].toString()
                txtPhoneNum.text = Editable.Factory.getInstance().newEditable(phoneNum)

                var salary = document["Salary"].toString()
                txtSalary.text = Editable.Factory.getInstance().newEditable(salary)

                var education = document["highEducation"].toString()
                txtEducation.text =Editable.Factory.getInstance().newEditable(education)

                var city = document["City"].toString()
                txtCity.text = Editable.Factory.getInstance().newEditable(city)

                var Introduction = document["Introduction"].toString()
                txtIntroduction.text = Editable.Factory.getInstance().newEditable(Introduction)

                var imageUrl: Uri? = document["profile"].toString().toUri()
                Glide.with(activity!!).load(imageUrl)
                    .into(imagebtn)

                Log.d("document", "DocumentSnapshot data: ${document["Email"]}")
            } else {
                Log.d("document", "No such document")
            }
        }
    }
    private fun updateInfo(uri: String) {
        uploadImage()
        val username: String = txtName.text.toString()

        val ic:String = txtIC.text.toString()
        val phoneNum = txtPhoneNum.text.toString()
        val education: String = txtEducation.text.toString()
        val city: String = txtCity.text.toString()
        val newsalary : String = "RM "+txtSalary.text.toString() +"/month"
        val introduction: String = txtIntroduction.text.toString()
        val userRef = db.collection("User").document(mAuth.uid.toString())
        var gender = " "

        if(rbMale.isChecked) {
            gender = "Male"
        }else {
            gender = "Female"
        }
        val image : String = uri
        db.runTransaction{transaction ->

            transaction.update(userRef,"Name",username)
            transaction.update(userRef,"Gender",gender)
            transaction.update(userRef,"IC",ic)
            transaction.update(userRef,"Phone Number",phoneNum)
            transaction.update(userRef,"highEducation",education)
            transaction.update(userRef,"City",city)
            transaction.update(userRef,"Salary",newsalary)
            transaction.update(userRef,"Introduction",introduction)
            transaction.update(userRef,"profile",image)

        }

            .addOnSuccessListener {

                Toast.makeText(context, "Successfully edit", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Transaction failure.", e)
                Toast.makeText(context, "Failed to edit", Toast.LENGTH_SHORT).show()
            }
    }
    private fun addUploadRecordToDb(uri: String){
        val db = FirebaseFirestore.getInstance()
        val user = mAuth.currentUser
        val uid = user?.uid.toString()
        val data = HashMap<String, Any>()
        data["imageUrl"] = uri
        Glide.with(activity!!).load(uri)
            .into(imagebtn)
        db.collection("User")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(context, "Saved to DB", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving to DB", Toast.LENGTH_LONG).show()
            }
    }
    private fun uploadImage(){
        if(filePath != null){
            val ref = storageReference?.child("ProfileUser/" + UUID.randomUUID().toString())
            ref?.putFile(filePath!!)?.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> {
                Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show()
            })?.addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(context, "Image Uploading Failed " + e.message, Toast.LENGTH_SHORT).show()
            })
        }else{
            Toast.makeText(context, "Please Select an Image", Toast.LENGTH_SHORT).show()
        }
    }

}
