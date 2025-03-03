package com.example.cwc

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cwc.adapters.ProfilePostAdapter
import com.example.cwc.data.local.AppDatabase
import com.example.cwc.data.models.Post
import com.example.cwc.data.repository.UserRepository
import com.example.cwc.viewmodel.UserViewModel
import com.example.cwc.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

  private lateinit var profilePostAdapter: ProfilePostAdapter
  private lateinit var recyclerView: RecyclerView
  private lateinit var swipeRefreshLayout: SwipeRefreshLayout
  private var postList = mutableListOf<Post>()

  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  // רישום לקליטת תמונה מהגלריה (לשינוי תמונת הפרופיל)
  private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    uri?.let {
      uploadProfilePicture(it)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_profile, container, false)

    // טעינת פרטי המשתמש והצגתם ב-UI
    loadUserDetails(view)

    // הגדרת תת-פרגמנט לניווט בתחתית
    val childFragment = BottomNavFragment()
    val bundle = Bundle()
    bundle.putString("current_page", "profile")
    childFragment.arguments = bundle

    // טעינת תמונת הפרופיל (אם קיימת) – כאן נטען באמצעות Picasso מה-URL ששמור ב-Firestore
    val profileImage = view.findViewById<ImageView>(R.id.profile_picture)
    loadProfilePicture(profileImage)

    // לחיצה על כפתור עריכת הפרופיל – מעבר ל-EditProfileActivity
    view.findViewById<View>(R.id.edit_profile_picture_text).setOnClickListener {
      val intent = android.content.Intent(requireContext(), EditProfileActivity::class.java)
      startActivity(intent)
    }

    // אתחול SwipeRefreshLayout
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_profile)
    swipeRefreshLayout.setOnRefreshListener {
      fetchPosts()
      loadUserDetails(view)
    }

    // אתחול RecyclerView להצגת הפוסטים של המשתמש
    recyclerView = view.findViewById(R.id.recycler_view_profile)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    profilePostAdapter = ProfilePostAdapter(postList, requireContext())
    recyclerView.adapter = profilePostAdapter

    // הצגת תת-פרגמנט הניווט
    childFragmentManager.beginTransaction()
      .replace(R.id.navbar_container, childFragment)
      .commit()

    // טעינת הפוסטים של המשתמש
    fetchPosts()

    return view
  }

  // פונקציה לטעינת פרטי המשתמש מ-Firestore ועדכון רכיבי ה-UI
  private fun loadUserDetails(rootView: View) {
    val userId = auth.currentUser?.uid
    if (userId == null) {
      Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
      return
    }
    db.collection("users").document(userId)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val firstName = document.getString("firstname") ?: "undefined"
          val lastName = document.getString("lastname") ?: "undefined"
          val city = document.getString("city") ?: "undefined"
          val country = document.getString("country") ?: "undefined"
          val email = document.getString("email") ?: "undefined"
          Log.d("ProfileFragment", "Fetched user details: firstname=$firstName, lastname=$lastName, city=$city, country=$country, email=$email")
          rootView.findViewById<TextView>(R.id.fullname_text)?.text = "$firstName $lastName"
          rootView.findViewById<TextView>(R.id.location_text)?.text = country
          rootView.findViewById<TextView>(R.id.email_text)?.text = email
        } else {
          Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(requireContext(), "Failed to load user details", Toast.LENGTH_SHORT).show()
        Log.e("ProfileFragment", "Error fetching user details", e)
      }
  }

  // העלאת תמונת הפרופיל ועדכון כתובת התמונה במסמך המשתמש
  fun uploadProfilePicture(imageUri: Uri) {
    val storageRef = FirebaseStorage.getInstance().reference
    val userId = auth.currentUser?.uid
    val profileImageRef = storageRef.child("profile_pictures/$userId.jpg")

    profileImageRef.putFile(imageUri)
      .addOnSuccessListener {
        profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
          saveImageUrlToFirestore(downloadUri.toString())
        }
        Toast.makeText(requireContext(), "Profile Picture Uploaded!", Toast.LENGTH_SHORT).show()
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
      }
  }

  fun saveImageUrlToFirestore(imageUrl: String) {
    val userId = auth.currentUser?.uid
    FirebaseFirestore.getInstance().collection("users").document(userId!!)
      .update("profileImageUrl", imageUrl)
      .addOnSuccessListener {
        Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
      }
  }

  // טעינת תמונת הפרופיל באמצעות Picasso מה-URL ששמור ב-Firestore
  private fun loadProfilePicture(profileImage: ImageView) {
    val userId = auth.currentUser?.uid ?: return
    FirebaseFirestore.getInstance().collection("users").document(userId)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val profileImageUrl = document.getString("profileImageUrl")
          if (!profileImageUrl.isNullOrEmpty()) {
            if (profileImageUrl.startsWith("http://") || profileImageUrl.startsWith("https://")) {
              // אם זה URL מהאינטרנט – משתמשים ב-Picasso
              Picasso.get()
                .load(profileImageUrl)
                .placeholder(R.drawable.profile_foreground)
                .error(R.drawable.profile_foreground)
                .into(profileImage)
            } else {
              // אם זה נתיב מקומי – טוענים את התמונה דרך BitmapFactory
              val bitmap = BitmapFactory.decodeFile(profileImageUrl)
              if (bitmap != null) {
                profileImage.setImageBitmap(bitmap)
              } else {
                profileImage.setImageResource(R.drawable.profile_foreground)
              }
            }
          }
        }
      }
      .addOnFailureListener {
        Log.e("ProfileFragment", "Failed to load profile image: ${it.message}")
      }
  }


  // שליפת הפוסטים של המשתמש – בדיקה לפי user_id
  private fun fetchPosts() {
    swipeRefreshLayout.isRefreshing = true

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    if (currentUserId == null) {
      Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
      swipeRefreshLayout.isRefreshing = false
      return
    }

    FirebaseFirestore.getInstance().collection("posts")
      .whereEqualTo("user_id", currentUserId)
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener { documents ->
        Log.d("ProfileFragment", "Fetched posts count: ${documents.size()}")
        postList.clear()
        for (document in documents) {
          val post = document.toObject(Post::class.java)
          if (post.user_id == currentUserId) {
            postList.add(post)
          }
        }
        profilePostAdapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
      }
      .addOnFailureListener { exception ->
        Log.e("ProfileFragment", "Error fetching posts", exception)
        Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
        swipeRefreshLayout.isRefreshing = false
      }
  }
}
