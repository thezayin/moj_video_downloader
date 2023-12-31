package com.bluelock.moj.ui.presentation.setting

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.android.installreferrer.BuildConfig
import com.bluelock.moj.R
import com.bluelock.moj.databinding.FragmentSettingBinding
import com.bluelock.moj.remote.RemoteConfig
import com.bluelock.moj.ui.presentation.base.BaseFragment
import com.example.ads.GoogleManager
import com.example.ads.databinding.MediumNativeAdLayoutBinding
import com.example.ads.databinding.NativeAdBannerLayoutBinding
import com.example.ads.newStrategy.types.GoogleInterstitialType
import com.example.ads.ui.binding.loadNativeAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingBinding =
        FragmentSettingBinding::inflate

    @Inject
    lateinit var googleManager: GoogleManager

    private var nativeAd: NativeAd? = null

    @Inject
    lateinit var remoteConfig: RemoteConfig

    override fun onCreatedView() {
        observer()
        if (remoteConfig.nativeAd) {
            showNativeAd()
            showRecursiveAds()
        }

    }

    private fun observer() {
        lifecycleScope.launch {
            binding.apply {
                btnBack.setOnClickListener {
                    findNavController().navigateUp()
                }

                lTerm.setOnClickListener {
                    showInterstitialAd { }
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://bluelocksolutions.blogspot.com/2023/08/terms-and-condition-for-moj.html")
                    )
                    startActivity(intent)
                }
                lPrivacy.setOnClickListener {
                    showInterstitialAd { }
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://bluelocksolutions.blogspot.com/2023/08/privacy-policy-for-moj-video-downloader.html")
                    )
                    startActivity(intent)
                }
                lContact.setOnClickListener {
                    showInterstitialAd { }
                    val emailIntent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:blue.lock.testing@gmail.com")
                    )
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Moj Downloader")
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "your message here")
                    startActivity(Intent.createChooser(emailIntent, "Chooser Title"))
                }
                lShare.setOnClickListener {
                    showInterstitialAd { }
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                        var shareMessage = "\nLet me recommend you this application\n\n"
                        shareMessage =
                            """
                            ${shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID}
                            """.trimIndent()
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        startActivity(Intent.createChooser(shareIntent, "choose one"))
                    } catch (e: java.lang.Exception) {
                        Log.d("jeje_e", e.toString())
                    }

                }
            }
        }
    }

    private fun showInterstitialAd(callback: () -> Unit) {
        if (remoteConfig.showInterstitial) {
            val ad: InterstitialAd? =
                googleManager.createInterstitialAd(GoogleInterstitialType.MEDIUM)

            if (ad == null) {
                callback.invoke()
                return
            } else {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        callback.invoke()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        super.onAdFailedToShowFullScreenContent(error)
                        callback.invoke()
                    }
                }
                ad.show(requireActivity())
            }
        } else {
            callback.invoke()
        }
    }

    private fun showNativeAd() {
        nativeAd = googleManager.createNativeAdSmall()
        nativeAd?.let {
            val nativeAdLayoutBinding = NativeAdBannerLayoutBinding.inflate(layoutInflater)
            nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
            binding.nativeView.removeAllViews()
            binding.nativeView.addView(nativeAdLayoutBinding.root)
            binding.nativeView.visibility = View.VISIBLE
        }
    }

    private fun showDropDown() {
        val nativeAdCheck = googleManager.createNativeFull()
        nativeAdCheck?.let {
            binding.apply {
                dropLayout.bringToFront()
                nativeViewDrop.bringToFront()
            }
            val nativeAdLayoutBinding = MediumNativeAdLayoutBinding.inflate(layoutInflater)
            nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
            binding.nativeViewDrop.removeAllViews()
            binding.nativeViewDrop.addView(nativeAdLayoutBinding.root)
            binding.nativeViewDrop.visibility = View.VISIBLE
            binding.dropLayout.visibility = View.VISIBLE

            binding.btnDropDown.setOnClickListener {
                binding.dropLayout.visibility = View.GONE

            }
            binding.btnDropUp.visibility = View.INVISIBLE

        }
    }

    private fun showRecursiveAds() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (this.isActive) {
                    showNativeAd()
                    showDropDown()
                    showInterstitialAd {  }
                    delay(30000L)
                }
            }
        }
    }
}