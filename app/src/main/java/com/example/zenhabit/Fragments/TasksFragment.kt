package com.example.zenhabit.Fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zenhabit.R
import com.example.zenhabit.adapter.AdapterObjectius
import com.example.zenhabit.databinding.FragmentTasksBinding
import com.example.zenhabit.databinding.ObjDiariAyoutBinding
import com.example.zenhabit.models.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.tasks.await
import org.w3c.dom.Document
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TasksFragment : Fragment() {

    private lateinit var _binding: FragmentTasksBinding
    private val binding get() = _binding

    private val db = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = Firebase.auth
    private lateinit var data: MutableList<Objectius>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: AdapterObjectius
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var shimmerFrameLayoutObjDiari: ShimmerFrameLayout
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar?.title = getString(R.string.tasks_title)
        val view = binding.root
        _binding.addTasc.setOnClickListener {
            findNavController().navigate(R.id.action_tasksFragment2_to_createEditTaskFragment)
        }

        //ResptesDiaris shimmer
        shimmerFrameLayoutObjDiari = binding.shimmerObjDiari
        shimmerFrameLayoutObjDiari.startShimmer()

        //obtenir els reptes diaris


        FirebaseFirestore.getInstance().collection("Usuaris")
            .document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { resultUser ->



                val llistaReptes = resultUser.get("llistaReptes") as ArrayList<RepteUsuari>
                // resultUser.get("llistaReptes") as ArrayList<RepteUsuari>
                Log.d("REPTESlength1", llistaReptes.size.toString())

                if (llistaReptes != null) {
                    if (llistaReptes.size <= 0) {

                        omplirLlistReptes()

                    }
                    Log.d("REPTESlength3", llistaReptes.size.toString())
                    Log.d("REPTESlength3", llistaReptes.toString())



                    for (i in 1..3) {
                        Log.d("REPTES", "DENTRO")
                        val a = llistaReptes[i] as RepteUsuari
                        Log.d("REPTES", a.toString())


                        if (i == 1) {
                            binding.Obj1.textViewDesc.text = llistaReptes[i].repte.descripcio
                            binding.Obj1.titolRepte.text = llistaReptes[i].repte.titol
                            binding.Obj1.checkboxDone.isChecked = llistaReptes[i].acosneguit
                        }
                        if (i == 2) {
                            binding.Obj2.textViewDesc.text = llistaReptes[i].repte.descripcio
                            binding.Obj2.titolRepte.text = llistaReptes[i].repte.titol
                            binding.Obj2.checkboxDone.isChecked = llistaReptes[i].acosneguit
                        }
                        if (i == 3) {
                            binding.Obj3.textViewDesc.text = llistaReptes[i].repte.descripcio
                            binding.Obj3.titolRepte.text = llistaReptes[i].repte.descripcio
                            binding.Obj3.checkboxDone.isChecked = llistaReptes[i].acosneguit
                        }

                    }


                }


            }


        //Mostrar
        Handler(Looper.getMainLooper()).postDelayed({
            binding.listObjDiari.visibility = View.VISIBLE
            shimmerFrameLayoutObjDiari.stopShimmer()
            shimmerFrameLayoutObjDiari.visibility = View.INVISIBLE
            resetReptesDiaris()  // comprova si ja ha pasat un dia des de la actualització de reptes diaris

        }, 1000)

        //canviar de color els checkboxes
        binding.Obj1.checkboxDone.setOnCheckedChangeListener { buttonView, isChecked ->
            checkChecked(isChecked, binding.Obj1, 1)
        }
        binding.Obj2.checkboxDone.setOnCheckedChangeListener { buttonView, isChecked ->
            checkChecked(isChecked, binding.Obj2, 2)
        }
        binding.Obj3.checkboxDone.setOnCheckedChangeListener { buttonView, isChecked ->
            checkChecked(isChecked, binding.Obj3, 3)
        }


        //----------------NEW RECYCLERVIEW-----------------
        //cargar shimmer
        mRecyclerView = binding.rvTasques
        val mLayoutManager = LinearLayoutManager(this.getActivity())
        shimmerFrameLayout = binding.shimmer
        shimmerFrameLayout.startShimmer()
        //cargar recyclerview
        mRecyclerView.layoutManager = mLayoutManager
        loadData()


        // chart
        pieChart = binding.pieChart
        preparePieData()


        return view
    }

    private fun omplirLlistReptes() {

        val llistaReptes = ArrayList<RepteUsuari>()

        for (i in 1..3) {
            val docref = FirebaseFirestore.getInstance().collection("Reptes")
                .document(i.toString()).get()
                .addOnSuccessListener { result ->

//                    val a = result.toObject<RepteUsuari>()
//                    if (a != null) {
//                        Log.d("REPTESObject", a.acosneguit.toString())
//                    }


                    if (result != null) {
                        val r = Repte(
                            i,
                            result.get("descripcio").toString(),
                            result.get("titol").toString()
                        )

                        val ru =
                            RepteUsuari(r, result.get("vist") as Boolean)

//                                    val titol = result.get("titol")
//                                    val desc = result.get("descripcio")
//                                    val done = result.get("vist") as Boolean
                        Log.d("REPTES", ru.repte.titol)

                        llistaReptes.add(ru)


                    }
                    FirebaseFirestore.getInstance().collection("Usuaris")
                        .document(auth.currentUser!!.uid)
                        .update("llistaReptes", llistaReptes)
                    Log.d("REPTESlength2", llistaReptes.size.toString())

                }.addOnFailureListener { exception ->
                    Log.d("TAG", "ERROR: $exception")
                }
        }


    }

    /***
     * Funcio per canviar de color els checkboxes
     * @author Izan Jimenez
     */
    private fun checkChecked(
        isChecked: Boolean,
        objDiariAyoutBinding: ObjDiariAyoutBinding,
        obj: Int
    ) {
        if (isChecked) {
            objDiariAyoutBinding.repteDiariRow1.background.setTint(Color.parseColor("#4D174C37"))
            FirebaseFirestore.getInstance().collection("Reptes")
                .document(obj.toString()).update("vist", true)

        } else {
            objDiariAyoutBinding.repteDiariRow1.background.setTint(Color.parseColor("#4D3DD497"))
            FirebaseFirestore.getInstance().collection("Reptes")
                .document(obj.toString()).update("vist", false)
        }
    }

    /***
     * Reseteja els reptes diaris cada 24h
     */
    private fun resetReptesDiaris() {
        val actualDay = Calendar.getInstance().time// dia i hora actual

        FirebaseFirestore.getInstance().collection("Verificacions")
            .document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { result ->
                val lastDay = result.getTimestamp("lastDate")!!
                    .toDate() // dia i hora que es va llençar l'última notificació
                val difference: Long = actualDay.time - lastDay.time
                val seconds = difference / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                Log.d("DAYS", days.toString())
                if (days >= 1) { // si ja ha passat un dia canvia els colors i isChecked dels checkboxes
                    checkChecked(false, binding.Obj1, 1)
                    binding.Obj3.checkboxDone.isChecked = false
                    checkChecked(false, binding.Obj1, 2)
                    binding.Obj3.checkboxDone.isChecked = false
                    checkChecked(false, binding.Obj1, 3)
                    binding.Obj3.checkboxDone.isChecked = false

//                    // val doc = FirebaseFirestore.getInstance().collection("Reptes").get().result.count()
//                    Log.d("COUNT", "Count: ${doc}")
                }
            }


    }

    private fun loadData() {

        var ret: ArrayList<Objectius>

        val docref = db.collection("Usuaris").document(Firebase.auth.currentUser!!.uid)
        docref.get().addOnSuccessListener { document ->
            if (document != null) {

                ret = Objectius.dataFirebaseToObjectius(document)
                if (ret.isEmpty()) {
                    mRecyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    mRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }

                val filteredList = ret.filter { !it.complert }

                if (filteredList.isEmpty()) {
                    mRecyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    mRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }

                //shimmer desaparece
                binding.rvTasques.visibility = View.VISIBLE
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.INVISIBLE


                mAdapter = AdapterObjectius(
                    filteredList,
                    { index -> deleteItem(index) },
                    { nom, fecha, descripcio, categoria, tipus, hora, repeticion ->
                        sendItem(
                            nom,
                            fecha,
                            descripcio,
                            categoria,
                            tipus,
                            hora,
                            repeticion
                        )
                    });

                mRecyclerView.adapter = mAdapter

            } else {
                //ERROR
                Log.d("TAG", "DocumentSnapshot data: NO SE ENCONTRO EL DOCUMENTO")

            }

        }.addOnFailureListener { exception ->
            Log.d("TAG", "ERROR AL OBTENER $exception")

        }
    }

    private fun sendItem(
        nom: String,
        fecha: String,
        descripcio: String,
        categoria: String,
        tipus: Boolean,
        hora: String?,
        repeticion: Dies?
    ) {
        if (tipus) {
            val action =
                TasksFragmentDirections.actionTasksFragment2ToCreateEditHabitFragment(
                    nom,
                    hora,
                    fecha,
                    descripcio,
                    categoria,
                    repeticion?.toBooleanArray()
                )
            findNavController().navigate(action)
        } else {
            val action =
                TasksFragmentDirections.actionTasksFragment2ToCreateEditTaskFragment(
                    nom,
                    fecha,
                    descripcio,
                    categoria
                )
            findNavController().navigate(action)
        }
    }

    private fun deleteItem(index: Int) {
        val objectiuSeleccionat = mAdapter.getItem(index) as Objectius
        db.collection("Usuaris").document(Firebase.auth.currentUser!!.uid).get()
            .addOnSuccessListener { result ->
                val objectius = Objectius.dataFirebaseToObjectius(result)
                var index = 0
                for (objectiu in objectius) {
                    if (objectiu.nom == objectiuSeleccionat.nom && objectiu.categoria == objectiuSeleccionat.categoria && objectiu.dataLimit == objectiuSeleccionat.dataLimit && objectiu.horari == objectiuSeleccionat.horari) {
                        val objectiuLlista = objectiu
                        objectiuLlista.complert = true
                        objectius.set(index, objectiuLlista)
                        val filteredList = objectius.filter { !it.complert }
                        mAdapter.setItems(filteredList)
                        break
                    }
                    index++
                }
                db.collection("Usuaris")
                    .document(Firebase.auth.currentUser!!.uid).update("llistaObjectius", objectius)

                val numRandom = (1..9).random()

                db.collection("Plantes").document(numRandom.toString()).get()
                    .addOnSuccessListener { secondResult ->
                        val plantesUsuari = PlantaUsuari.dataFirebaseToPlanta(result)
                        val canvisPlanta = plantesUsuari.get(numRandom)
                        var quantitat = canvisPlanta.quantitat
                        quantitat++
                        canvisPlanta.quantitat = quantitat
                        plantesUsuari.set(numRandom, canvisPlanta)
                        db.collection("Usuaris").document(Firebase.auth.currentUser!!.uid)
                            .update("llistaPlantes", plantesUsuari)
                            .addOnSuccessListener {
                                val language = Locale.getDefault().getLanguage()
                                if (language == "en") {
                                    Toast(activity).showCustomToast(
                                        getString(R.string.toast_objectiu_completat) + " " + secondResult.get(
                                            "name"
                                        ).toString()
                                    )
                                } else if (language == "es") {
                                    Toast(activity).showCustomToast(
                                        getString(R.string.toast_objectiu_completat) + " " + secondResult.get(
                                            "nombre"
                                        ).toString()
                                    )
                                } else {
                                    Toast(activity).showCustomToast(
                                        getString(R.string.toast_objectiu_completat) + " " + secondResult.get(
                                            "nom"
                                        ).toString()
                                    )
                                }
                            }
                    }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun preparePieData() {
        // on below line we are setting user percent value,
        // setting description as enabled and offset for pie chart
        pieChart.setUsePercentValues(true)
        pieChart.getDescription().setEnabled(false)
        pieChart.setExtraOffsets(5f, 5f, 5f, 5f)

        // on below line we are setting drag for our pie chart
        pieChart.setDragDecelerationFrictionCoef(0.25f)

        // on below line we are setting hole
        // and hole color for pie chart
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)

        // on below line we are setting circle color and alpha
        pieChart.setTransparentCircleColor(Color.TRANSPARENT)
        pieChart.setTransparentCircleAlpha(110)

        // on  below line we are setting hole radius
        pieChart.setHoleRadius(75f)
        pieChart.setTransparentCircleRadius(33f)

        // on below line we are setting center text
        pieChart.setDrawCenterText(true)

        // on below line we are setting
        // rotation for our pie chart
        pieChart.rotationAngle = 0f

        // enable rotation of the pieChart by touch
        pieChart.setRotationEnabled(true)
        pieChart.setHighlightPerTapEnabled(false)

        // on below line we are setting animation for our pie chart
        pieChart.animateY(800, Easing.EaseInOutQuad)

        // on below line we are disabling our legend for pie chart
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        var perCC = 0f
        runBlocking {
            perCC = getDataFromFirestore()
        }
        val perNC: Float = 100 - (perCC * 100)

        // on below line we are creating array list and
        // adding data to it to display in pie chart
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(perNC))
        entries.add(PieEntry(perCC * 100))

        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "Mobile OS")

        // on below line we are setting icons.
        dataSet.setDrawIcons(false)

        // on below line we are setting slice for pie
        dataSet.sliceSpace = 1f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors to listwwssw
        val colors: ArrayList<Int> = ArrayList()
        colors.add(Color.parseColor("#A6FD565E"))
        colors.add(Color.parseColor("#993DD497"))

        // on below line we are setting colors.
        dataSet.colors = colors

        // on below line we are setting pie data set
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(0f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChart.setData(data)

        // undo all highlights
        pieChart.highlightValues(null)

        // loading chart
        pieChart.invalidate()
    }

    suspend fun getDataFromFirestore(): Float {
        var perT = 0f
        val total: Float
        var oComplets = 0f
        val objectius: ArrayList<Objectius>

        val result = FirebaseFirestore.getInstance().collection("Usuaris")
            .document(Firebase.auth.currentUser!!.uid).get().await()

        if (result != null) {
            objectius = Objectius.dataFirebaseToObjectius(result)
            total = (objectius.size).toFloat()
            for (objectiu in objectius) {
                if (objectiu.complert) {
                    oComplets++
                }
            }
            perT = (oComplets / total)
        }

        return perT
    }

    private fun Toast.showCustomToast(message: String) {
        val layout = requireActivity().layoutInflater.inflate(
            R.layout.toast_layout,
            requireActivity().findViewById(R.id.toast_container)
        )

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        this.apply {
            setGravity(Gravity.CENTER, 0, 700)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }


}
