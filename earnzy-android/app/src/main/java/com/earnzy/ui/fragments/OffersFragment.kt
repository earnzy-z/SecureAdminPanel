package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
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
        loadOffers()
    }

    private fun loadOffers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val wall = ApiClient.api.getOfferWall()
                displayOffers(wall)
            } catch (e: Exception) {
                showError("Failed to load offers: ${e.message}")
            }
        }
    }

    private fun displayOffers(wall: Map<String, List<Offer>>) {
        wall.forEach { (category, offers) ->
            offers.forEach { offer ->
                val offerView = ItemOfferBinding.inflate(
                    LayoutInflater.from(context),
                    binding.offersGrid,
                    false
                )
                
                offerView.offerTitle.text = offer.title
                offerView.offerReward.text = "+${offer.reward}"
                
                offerView.claimButton.setOnClickListener {
                    claimOffer(offer)
                }

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                binding.offersGrid.addView(offerView.root, params)
            }
        }
    }

    private fun claimOffer(offer: Offer) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.api.claimOffer(offer.id)
                showSuccess("Offer claimed! Check your wallet.")
            } catch (e: Exception) {
                showError("Already claimed: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
