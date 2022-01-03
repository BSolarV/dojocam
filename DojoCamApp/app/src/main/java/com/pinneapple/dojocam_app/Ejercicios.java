package com.pinneapple.dojocam_app;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.objects.VideoInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Ejercicios#newInstance} factory method to
 * create an instance of this fragment.
 */
@androidx.annotation.RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Ejercicios extends ListFragment implements AdapterView.OnItemClickListener,SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView title ,desc;

    private List<String> vid_list = new ArrayList();
    private List<String> id_list = new ArrayList();
    private ArrayAdapter adapter;
    private LoadingDialog loadingDialog = new LoadingDialog(this);

    private String difficulty;
    public Ejercicios() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Ejercicios.
     */
    // TODO: Rename and change types and number of parameters
    public static Ejercicios newInstance(String param1, String param2) {
        Ejercicios fragment = new Ejercicios();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_ejercicios, container, false);
    }
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        Random r = new Random();
        int il = r.nextInt(100);
        if(il > 70){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            tip dialog = new tip();
            dialog.show(manager,"message dialog");
        }
        difficulty = getArguments().getString("difficulty");

        /*TextView pri = (TextView) getView().findViewById(R.id.textView);
        ImageView pri_img = (ImageView) getView().findViewById(R.id.imageView);

        Button btn3 = (Button) getView().findViewById(R.id.button3);
        Button btn4 = (Button) getView().findViewById(R.id.button4);
        Button btn5 = (Button) getView().findViewById(R.id.button5);


        pri.setOnClickListener((View.OnClickListener) this);
        pri_img.setOnClickListener((View.OnClickListener) this);
        btn3.setOnClickListener((View.OnClickListener) this);
        btn4.setOnClickListener((View.OnClickListener) this);
        btn5.setOnClickListener((View.OnClickListener) this);*/

        adapter = new ArrayAdapter(getContext(), R.layout.list_vid, vid_list );
        ListView lv = (ListView) getView().findViewById(R.id.vid_list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

        loadingDialog.startLoadingDialog();

    }
    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Buscar");

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        Bundle bundle = new Bundle();

        bundle.putString("difficulty" , difficulty);
        bundle.putString("videoId",  id_list.get(pos));
        bundle.putString("name", "Test Video");

        Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);
    }

    @Override
    public void onResume() {

        super.onResume();

        vid_list.clear();
        id_list.clear();
        // Get post and answers from database




        Task<QuerySnapshot> data = db.collection("ejercicios").whereEqualTo("dificultad", difficulty).get();
        data.addOnSuccessListener(command -> {
            List<VideoInfo> docList = command.toObjects(VideoInfo.class);
            if ( data.isComplete() ){
                int i = 0;
                for (VideoInfo videoInfo:
                        docList) {

                    String aux =videoInfo.getNombre();
                    vid_list.add(aux);
                    id_list.add(command.getDocuments().get(i).getId());
                    i++;
                }
                //Toast.makeText(getContext(), "Wena", Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
                loadingDialog.dismissDialog();
                //Toast.makeText(getContext(), "No te veo compare, avispate", Toast.LENGTH_SHORT).show();

                if(i == 0) {
                    // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Tiempo de espera excedido")
                            .setTitle("Error de Conexión");

// 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            //title.setText(command.get("nombre").toString());
            //desc.setText(command.get("descripcion").toString());

        });
        data.addOnFailureListener(command -> {
            loadingDialog.dismissDialog();
            //Toast.makeText(getContext(), "No te veo compare, avispateeee", Toast.LENGTH_SHORT).show();

            // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Tiempo de espera excedido")
                    .setTitle("Error de Conexión");

// 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }
}
