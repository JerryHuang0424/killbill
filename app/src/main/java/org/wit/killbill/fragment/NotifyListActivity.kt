package org.wit.killbill.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.killbill.R
import org.wit.killbill.activity.PageMainActivity
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.databinding.ActivityListMainBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.models.NotifyModel

class NotifyListFragment : Fragment(), NotifyAdapterListener {
    companion object {
        // add newInstance() method
        fun newInstance(): NotifyListFragment {
            return NotifyListFragment()
        }
    }

    private var _binding: ActivityListMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: MainApp
    private var position: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityListMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as MainApp

        // initialize RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = NotifyAdapter(app.notifyNotifyModels.findAll(), this)
    }

    fun updateRecyclerView(){
        binding.recyclerView.adapter?.notifyItemRangeChanged(0, app.notifyNotifyModels.findAll().size)

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
        inflater.inflate(R.menu.menu_setting, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.item_add -> {
                val launchIntent = Intent(requireContext(), PageMainActivity::class.java)
                getResult.launch(launchIntent)
                return true
            }
            R.id.item_setting -> {
                // deal with setting menu
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(requireContext(), PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            binding.recyclerView.adapter?.notifyItemRangeChanged(0, app.notifyNotifyModels.findAll().size)
        }
    }

    private val getClickResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            binding.recyclerView.adapter?.notifyItemRangeChanged(0, app.notifyNotifyModels.findAll().size)
        }
        if(it.resultCode == 99) {
            binding.recyclerView.adapter?.notifyItemRemoved(position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}