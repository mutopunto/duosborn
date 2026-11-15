package com.duosborn.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Involucro (wrapper) Android per l'app web "duosborn".
 *
 * Non c'è logica dell'app qui dentro: tutta l'app (vocabolario, grammatica,
 * quiz, progressi...) resta nell'unico file HTML/CSS/JS che gira in una
 * WebView a schermo intero. Questa classe si limita a:
 *   1) caricare il sito live (sempre aggiornato, da GitHub Pages);
 *   2) se non c'è connessione, ripiegare sulla copia inclusa nell'app
 *      (assets/site.html), così l'app funziona anche offline;
 *   3) far funzionare il tasto "indietro" di Android dentro la pagina web
 *      invece di chiudere subito l'app.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LIVE_URL = "https://mutopunto.github.io/duosborn/";
    private static final String OFFLINE_URL = "file:///android_asset/site.html";

    private WebView webView;
    private boolean fellBackToOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // Essenziale: l'app salva progressi/impostazioni in localStorage.
        // Senza questo, ogni salvataggio fallirebbe in silenzio.
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // Se fallisce il caricamento della pagina principale (es. nessuna
                // connessione) passa una sola volta alla copia offline inclusa
                // nell'app, invece di mostrare la pagina di errore del browser.
                if (request.isForMainFrame() && !fellBackToOffline) {
                    fellBackToOffline = true;
                    view.loadUrl(OFFLINE_URL);
                }
            }
        });

        webView.loadUrl(LIVE_URL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
