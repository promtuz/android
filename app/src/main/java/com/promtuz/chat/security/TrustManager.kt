package com.promtuz.chat.security

import android.content.Context
import com.promtuz.chat.R
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object TrustManager {
    fun pinned(context: Context): X509TrustManager {
        val cf = CertificateFactory.getInstance("X.509")

        val caInput = context.resources.openRawResource(R.raw.root_ca)
        val ca = cf.generateCertificate(caInput)
        caInput.close()

        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        ks.setCertificateEntry("rootCA", ca)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        return tmf.trustManagers[0] as X509TrustManager
    }
}