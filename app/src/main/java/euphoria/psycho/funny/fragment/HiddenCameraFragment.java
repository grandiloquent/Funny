package euphoria.psycho.funny.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import euphoria.psycho.funny.R;

public class HiddenCameraFragment extends Fragment {
    private Button mButtonTakePicture;


    private void setButton() {
        mButtonTakePicture.setOnClickListener(v -> {

        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hidden_camera, container, false);
        mButtonTakePicture = view.findViewById(R.id.take_picture);
        setButton();
        return view;
    }
}
