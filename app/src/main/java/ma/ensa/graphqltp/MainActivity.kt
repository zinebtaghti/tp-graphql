package ma.ensa.graphqltp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import ma.ensa.graphqltp.adapter.ComptesAdapter
import ma.ensa.graphqltp.data.MainViewModel
import ma.ensa.graphqltp.type.TypeCompte

class MainActivity : AppCompatActivity() {

    // Déclaration des vues
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: FloatingActionButton
    private lateinit var totalCountText: TextView
    private lateinit var totalSumText: TextView
    private lateinit var averageText: TextView

    // ViewModel et adapter
    private val viewModel: MainViewModel by viewModels()
    private val comptesAdapter = ComptesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        initializeViews()

        // Configuration RecyclerView
        setupRecyclerView()

        // Configuration du bouton "Ajouter"
        setupAddButton()

        // Observation du ViewModel
        observeViewModel()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.comptesRecyclerView)
        addButton = findViewById(R.id.addCompteButton)
        totalCountText = findViewById(R.id.totalCountText)
        totalSumText = findViewById(R.id.totalSumText)
        averageText = findViewById(R.id.averageText)
    }

    private fun setupRecyclerView() {
        recyclerView.apply {
            adapter = comptesAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupAddButton() {
        addButton.setOnClickListener {
            showAddCompteDialog()
        }
    }

    private fun observeViewModel() {
        // Observation des comptes
        viewModel.comptesState.observe(this) { state ->
            when (state) {
                is MainViewModel.UIState.Success -> {
                    comptesAdapter.updateList(state.data)
                }
                is MainViewModel.UIState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Observation des statistiques
        viewModel.totalSoldeState.observe(this) { state ->
            when (state) {
                is MainViewModel.UIState.Success -> {
                    // Mise à jour des statistiques
                    totalCountText.text = "Total Accounts: ${state.data?.count ?: 0}"
                    totalSumText.text = "Total Balance: ${state.data?.sum ?: 0.0} €"
                    averageText.text = "Average Balance: ${state.data?.average ?: 0.0} €"
                }
                is MainViewModel.UIState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun showAddCompteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_compte, null)
        val soldeInput = dialogView.findViewById<TextInputEditText>(R.id.soldeInput)

        // Remplace les boutons radio par des éléments personnalisés côte à côte
        val courantView = dialogView.findViewById<TextView>(R.id.typeCourant)
        val epargneView = dialogView.findViewById<TextView>(R.id.typeEpargne)

        var selectedType: TypeCompte = TypeCompte.COURANT

        // Gestion de la sélection entre "Courant" et "Épargne"
        courantView.setOnClickListener {
            selectedType = TypeCompte.COURANT

        }

        epargneView.setOnClickListener {
            selectedType = TypeCompte.EPARGNE

        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Account")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val solde = soldeInput.text.toString().toDoubleOrNull()
                if (solde == null) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.saveCompte(solde, selectedType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
