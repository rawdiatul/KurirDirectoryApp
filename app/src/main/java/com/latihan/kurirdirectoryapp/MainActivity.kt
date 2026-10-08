package com.latihan.kurirdirectoryapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.latihan.kurirdirectoryapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var favoriteHelper: FavoriteHelper

    // Master dataset directly from API
    private var allUsers: List<UserResponse> = emptyList()

    // Filter states
    private var searchQuery: String = ""
    private var selectedCompany: String = "Semua Perusahaan"
    private var isFavoriteOnly: Boolean = false
    private var isSortAscending: Boolean = true

    // Company spinner adapter
    private lateinit var companySpinnerAdapter: ArrayAdapter<String>
    private val companyList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoriteHelper = FavoriteHelper(this)

        setupRecyclerView()
        setupSearch()
        setupModeToggle()
        setupSpinners()
        setupActions()

        fetchUsers()
    }

    override fun onResume() {
        super.onResume()
        // Refresh filter/list in case favorite status was toggled in DetailActivity
        if (allUsers.isNotEmpty()) {
            applyFiltersAndSort()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            favoriteHelper = favoriteHelper,
            onItemClick = { user ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_USER, user)
                }
                startActivity(intent)
            },
            onFavoriteClick = { _, _ ->
                // If in favorite filter mode, re-evaluating filters removes unfavorited items
                if (isFavoriteOnly) {
                    applyFiltersAndSort()
                } else {
                    updateStatistics(getCurrentFilteredCount(), allUsers.size)
                }
            }
        )

        binding.rvKurir.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = userAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                applyFiltersAndSort()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupModeToggle() {
        binding.toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isFavoriteOnly = (checkedId == R.id.btnFilterFavorite)
                applyFiltersAndSort()
            }
        }
    }

    private fun setupSpinners() {
        // Setup Company Spinner
        companyList.clear()
        companyList.add(getString(R.string.all_companies))
        companySpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            companyList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCompany.adapter = companySpinnerAdapter

        binding.spinnerCompany.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCompany = companyList.getOrElse(position) { getString(R.string.all_companies) }
                applyFiltersAndSort()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Sort Spinner
        val sortOptions = listOf(
            getString(R.string.sort_name_asc),
            getString(R.string.sort_name_desc)
        )
        val sortAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerSort.adapter = sortAdapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                isSortAscending = (position == 0)
                applyFiltersAndSort()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupActions() {
        binding.btnRefresh.setOnClickListener {
            fetchUsers()
        }

        binding.btnRetry.setOnClickListener {
            fetchUsers()
        }

        binding.btnResetFilter.setOnClickListener {
            resetFilters()
        }

        binding.btnResetEmpty.setOnClickListener {
            resetFilters()
        }
    }

    private fun resetFilters() {
        binding.etSearch.text?.clear()
        binding.toggleGroupFilter.check(R.id.btnFilterAll)
        binding.spinnerCompany.setSelection(0)
        binding.spinnerSort.setSelection(0)
        searchQuery = ""
        isFavoriteOnly = false
        selectedCompany = getString(R.string.all_companies)
        isSortAscending = true
        applyFiltersAndSort()
    }

    private fun fetchUsers() {
        showLoadingState()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getAllUsers()
                }

                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!
                    allUsers = users
                    updateCompanySpinner(users)
                    applyFiltersAndSort()
                    showSuccessState()
                } else {
                    showErrorState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorState()
            }
        }
    }

    private fun updateCompanySpinner(users: List<UserResponse>) {
        val currentSelected = selectedCompany
        val companies = users.mapNotNull { it.company?.name }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        companyList.clear()
        companyList.add(getString(R.string.all_companies))
        companyList.addAll(companies)
        companySpinnerAdapter.notifyDataSetChanged()

        val previousIndex = companyList.indexOf(currentSelected)
        if (previousIndex >= 0) {
            binding.spinnerCompany.setSelection(previousIndex)
        } else {
            binding.spinnerCompany.setSelection(0)
            selectedCompany = getString(R.string.all_companies)
        }
    }

    private fun applyFiltersAndSort() {
        if (allUsers.isEmpty()) return

        val query = searchQuery.lowercase()
        val allCompanyLabel = getString(R.string.all_companies)

        // 1. Filtering pipeline
        var filtered = allUsers.filter { user ->
            // Search filter: name, username, email, company.name
            val matchSearch = query.isEmpty() ||
                    user.name.lowercase().contains(query) ||
                    user.username.lowercase().contains(query) ||
                    user.email.lowercase().contains(query) ||
                    (user.company?.name?.lowercase()?.contains(query) == true)

            // Company filter
            val matchCompany = selectedCompany == allCompanyLabel ||
                    user.company?.name.equals(selectedCompany, ignoreCase = true)

            // Favorite filter
            val matchFavorite = !isFavoriteOnly || favoriteHelper.isFavorite(user.id)

            matchSearch && matchCompany && matchFavorite
        }

        // 2. Sorting pipeline
        filtered = if (isSortAscending) {
            filtered.sortedBy { it.name.lowercase() }
        } else {
            filtered.sortedByDescending { it.name.lowercase() }
        }

        // 3. Update Adapter
        userAdapter.submitList(filtered)

        // 4. Update Dynamic Statistics
        updateStatistics(filtered.size, allUsers.size)

        // 5. Handle Empty State
        if (filtered.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvKurir.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvKurir.visibility = View.VISIBLE
        }

        // Check if any filter is active to show Reset button
        val isFilterActive = searchQuery.isNotEmpty() ||
                isFavoriteOnly ||
                selectedCompany != allCompanyLabel ||
                !isSortAscending

        binding.btnResetFilter.visibility = if (isFilterActive && filtered.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun getCurrentFilteredCount(): Int {
        return userAdapter.itemCount
    }

    private fun updateStatistics(filteredCount: Int, totalCount: Int) {
        val allCompanyLabel = getString(R.string.all_companies)
        val isFilterActive = searchQuery.isNotEmpty() ||
                isFavoriteOnly ||
                selectedCompany != allCompanyLabel

        binding.tvStatistics.text = if (isFilterActive) {
            getString(R.string.stat_filtered_format, filteredCount, totalCount)
        } else {
            getString(R.string.stat_found_format, filteredCount)
        }
    }

    private fun showLoadingState() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.rvKurir.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.statisticsLayout.visibility = View.GONE
    }

    private fun showSuccessState() {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.statisticsLayout.visibility = View.VISIBLE
    }

    private fun showErrorState() {
        binding.layoutLoading.visibility = View.GONE
        binding.rvKurir.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.statisticsLayout.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
    }
}
