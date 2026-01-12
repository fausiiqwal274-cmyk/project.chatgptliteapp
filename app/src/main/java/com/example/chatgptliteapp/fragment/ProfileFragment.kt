package com.example.chatgptliteapp.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.chatgptliteapp.R
import com.example.chatgptliteapp.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var imgProfile: ImageView

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadProfileImage(it) }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        imgProfile = view.findViewById(R.id.imgProfile)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val btnEdit = view.findViewById<TextView>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser ?: run {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        val uid = user.uid
        tvEmail.text = user.email

        // Load data user
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tvUsername.text =
                        snapshot.child("username").getValue(String::class.java) ?: "User"

                    val photoUrl =
                        snapshot.child("photo").getValue(String::class.java)
                    if (!photoUrl.isNullOrEmpty()) {
                        imgProfile.setImageURI(Uri.parse(photoUrl))
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Ganti foto profil
        imgProfile.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Edit username
        btnEdit.setOnClickListener {
            val input = EditText(requireContext())
            input.hint = "New username"

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Username")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        database.child("users").child(uid).child("username")
                            .setValue(newName)
                        tvUsername.text = newName
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    database.child("users").child(uid).child("photo")
                        .setValue(downloadUri.toString())
                    imgProfile.setImageURI(uri)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }
}
