package com.example.zenhabit.Activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.zenhabit.R
import com.example.zenhabit.databinding.ActivityRegisterBinding
import com.example.zenhabit.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Victor García, Izan Jimenez, Txell Llanas, Pablo Morante
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var bin: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bin = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(bin.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        auth = Firebase.auth


        val buttonCancel: Button = bin.btnCancel
        buttonCancel.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }


        val buttonRegister: Button = bin.btnRegister
        buttonRegister.setOnClickListener {
            crearUsuari(
                bin.inputCreateEmail.text.toString().trim(),
                bin.inputCreatePsw.text.toString().trim(),
                bin.inputCreateUserName.text.toString().trim()
            )
        }

        val checkBoxTermsAndConditions: TextView = bin.textViewTermsAndConditions
        checkBoxTermsAndConditions.setOnClickListener(){
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://docs.google.com/document/d/1wtYD8rVOgt0BvMbN0Tl1huyKJ5ZbATtrKiwV_hx2gLw/edit?usp=sharing")
            startActivity(intent)
        }
    }


    /**
     * @author Pablo Morante, Izan Jimenez
     */
    private fun crearUsuari(email: String, password: String, nom: String) {
    if (!validateForm()) {
        return
    } else {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var plantes: ArrayList<PlantaUsuari> = ArrayList()
                    addPlantesToList(plantes)
                    val usuari = Usuari(nom, email, ArrayList<RepteUsuari>(), plantes, ArrayList<Objectius>())
                    FirebaseFirestore.getInstance().collection("Usuaris")
                        .document(auth.currentUser!!.uid).set(usuari)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    val actualDay = Calendar.getInstance().getTime()
                    val temporal = VerificacioNotificacio(actualDay,false)
                    FirebaseFirestore.getInstance().collection("Verificacions")
                        .document(auth.currentUser!!.uid).set(temporal)
                    Toast(this).showCustomToast(getString(R.string.user_created), this)
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    if (nom.length > 1) posaNomUser(nom)
                } else {
                    Toast(this).showCustomToast(getString(R.string.error_user_created), this)
                }
            }
    }
}

/**
 * @author Pablo Morante
 */
private fun posaNomUser(nom: String) {
    val profileUpdates = userProfileChangeRequest {
        displayName = nom
    }

    auth.currentUser!!.updateProfile(profileUpdates)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "User profile updated.")
            }
        }
}

/**
 * @author Pablo Morante, Txell Llanas
 */
private fun validateForm(): Boolean {
    var valid = true
    val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{4,}\$".toRegex()

    val email = bin.inputCreateEmail.text.toString().trim()
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        bin.inputCreateEmail.error = getString(R.string.error_email_created)
        valid = false
    } else {
        bin.inputCreateEmail.error = null
    }

    val password = bin.inputCreatePsw.text.toString().trim()
    if (!password.matches(PASSWORD_REGEX)) {
        bin.inputCreatePsw.error = getString(R.string.error_password_created)
        valid = false
    } else {
        bin.inputCreatePsw.error = null
    }

    val nom = bin.inputCreateUserName.text.toString().trim()
    if (nom.length > 15) {
        bin.inputCreateUserName.error = getString(R.string.error_username_created)
        valid = false
    } else {
        bin.inputCreateUserName.error = null
    }

    val checkBox = bin.checkBoxTermsAndConditions
    if (!checkBox.isChecked) {
        valid = false
        Toast(this).showCustomToast(getString(R.string.toast_accept_terms), this)
    }
    return valid
}

/**
 * @author Pablo Morante
 */
private fun Toast.showCustomToast(message: String, activity: RegisterActivity) {
    val layout = activity.layoutInflater.inflate(
        R.layout.toast_layout,
        activity.findViewById(R.id.toast_container)
    )

    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message

    this.apply {
        setGravity(Gravity.CENTER, 0, 750)
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}
    /**
     * @author Pablo Morante
     */
    private fun addPlantesToList(plantes: ArrayList<PlantaUsuari>) {
        plantes.add(PlantaUsuari("Abeto",0))
        plantes.add(PlantaUsuari("Palmera",0))
        plantes.add(PlantaUsuari("Olivo",0))
        plantes.add(PlantaUsuari("Girasol",0))
        plantes.add(PlantaUsuari("Rosa",0))
        plantes.add(PlantaUsuari("Lavanda",0))
        plantes.add(PlantaUsuari("Aloe Vera",0))
        plantes.add(PlantaUsuari("Bambú",0))
        plantes.add(PlantaUsuari("Cactus",0))

    }
}
