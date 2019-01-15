package euphoria.psycho.funny.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.util.Simple;
import euphoria.psycho.funny.util.ThreadUtils;
import euphoria.psycho.funny.util.debug.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TranslateFragment extends Fragment {
    private static final String LANGUAGE_AUTO = "auto";
    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_ZH = "zh";
    private static final String TAG = "Funny/TranslateFragment";
    private Button mChinese;
    private EditText mEditText;
    private Button mEnglish;
    private TextView mTarget;

    private void executeQuery(String sourceLanguage, String targetLanguage) {
        String q = Simple.getString(mEditText.getText());
        if (Simple.isNullOrWhiteSpace(q)) return;
        ThreadUtils.postOnBackgroundThread(() -> {
            String s = query(q, sourceLanguage, targetLanguage);
            ThreadUtils.postOnMainThread(() -> {
                mTarget.setText(s);
            });
        });
    }

    private String generateTranslateURL(String str, String sourceLanguage, String targetLanguage)

    {
        return "https://translate.google.cn/translate_a/single?client=gtx&sl="
                + sourceLanguage
                + "&tl="
                + targetLanguage
                + "&dt=t&dt=bd&ie=UTF-8&oe=UTF-8&dj=1&source=icon&q="
                + str;
    }

    private String parseJSON(String str) throws JSONException {
        JSONObject obj = new JSONObject(str);
        if (obj.has("sentences")) {
            JSONArray sentences = obj.getJSONArray("sentences");
            if (sentences.length() < 1) return null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sentences.length(); i++) {
                sb.append(sentences.getJSONObject(i).getString("trans"));
            }
            return sb.toString();
        }
        return null;
    }

    @WorkerThread
    private String query(String query, String sourceLanguage, String targetLanguage) {

        String q = null;
        try {
            q = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "[query] ---> ", e);
            return null;
        }
        Request request = new Request.Builder()
                .url(generateTranslateURL(q, sourceLanguage, targetLanguage)).build();

        try {
            Response response = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build().newCall(request).execute();

            if (response.isSuccessful()) {
                try {
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) return null;
                    return parseJSON(responseBody.string());
                } catch (JSONException e) {
                    return e.getMessage();
                }
            } else {
                return null;
            }
        } catch (IOException e) {

            return e.getMessage();
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translator, container, false);
        mEditText = view.findViewById(R.id.editText);
        mEnglish = view.findViewById(R.id.english);
        mChinese = view.findViewById(R.id.chinese);
        mTarget = view.findViewById(R.id.target);

        mEnglish.setOnClickListener(v ->
        {
            executeQuery(LANGUAGE_ZH, LANGUAGE_EN);

        });
        mChinese.setOnClickListener(v -> {
            executeQuery(LANGUAGE_EN, LANGUAGE_ZH);

        });
        return view;
    }
}
