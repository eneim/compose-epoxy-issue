package app.issue.compose.epoxy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.issue.compose.epoxy.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup ViewPager with adapter
        val pagerAdapter = MainPagerAdapter(this)
        binding.pages.adapter = pagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(
            binding.tabs,
            binding.pages
        ) { tab, position ->
            tab.text = "Page $position"
        }.attach()
    }
}

private class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 16

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return EpoxyFragment.newInstance(position)
    }
}
