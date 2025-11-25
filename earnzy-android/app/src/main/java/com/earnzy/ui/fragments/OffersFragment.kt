package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.data.Offer
import com.earnzy.databinding.FragmentOffersBinding
import com.earnzy.databinding.ItemOfferBinding
import kotlinx.coroutines.launch

class OffersFragment : BaseFragment() {
    private var _binding: FragmentOffersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOffersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showShimmer(true)
        loadOffers()
    }

    private fun loadOffers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val wall = ApiClient.api.getOfferWall()
                displayOffers(wall)
                showShimmer(false)
            } catch (e: Exception) {
                showShimmer(false)
                showError("Failed to load offers: ${e.message}")
            }
        }
    }

    private fun displayOffers(wall: Map<String, List<Offer>>) {
        var columnIndex = 0
        var rowIndex = 0
        
        wall.forEach { (category, offers) ->
            offers.forEach { offer ->
                val offerView = ItemOfferBinding.inflate(
                    LayoutInflater.from(context),
                    binding.offersGrid,
                    false
                )
                
                offerView.offerTitle.text = offer.title
                offerView.offerReward.text = "+${offer.reward}"
                offerView.claimButton.isEnabled = offer.claimedAt == null
                
                offerView.claimButton.setOnClickListener {
                    claimOffer(offer)
                }

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(columnIndex, 1f)
                    rowSpec = GridLayout.spec(rowIndex)
                }
                
                binding.offersGrid.addView(offerView.root, params)
                
                columnIndex = (columnIndex + 1) % 2
                if (columnIndex == 0) rowIndex++
            }
        }
    }

    private fun claimOffer(offer: Offer) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.api.claimOffer(offer.id)
                showSuccess("Offer claimed successfully!")
                loadOffers()
            } catch (e: Exception) {
                showError("Offer already claimed")
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.offersGrid.visibility = View.GONE
            val animation = AnimationUtils.loadAnimation(context, R.anim.shimmer)
        } else {
            binding.offersGrid.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
