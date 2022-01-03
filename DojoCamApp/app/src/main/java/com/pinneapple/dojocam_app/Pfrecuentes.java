package com.pinneapple.dojocam_app;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.objets.Pregunta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Pfrecuentes#newInstance} factory method to
 * create an instance of this fragment.
 */
@androidx.annotation.RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Pfrecuentes extends ListFragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<String> bdQuestions = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayAdapter<String> adapter2;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private List<String> user_list = new ArrayList();
    private List<String> id_list = new ArrayList();


    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Pfrecuentes() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Pfrecuentes.
     */
    // TODO: Rename and change types and number of parameters
    public static Pfrecuentes newInstance(String param1, String param2) {
        Pfrecuentes fragment = new Pfrecuentes();
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
        return inflater.inflate(R.layout.fragment_pfrecuentes, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        adapter2 = new ArrayAdapter(getContext(), R.layout.list_vid, user_list);
        ListView lv = (ListView) getView().findViewById(R.id.preg_list4);
        lv.setAdapter(adapter2);
        lv.setOnItemClickListener(this);

        loadingDialog.startLoadingDialog();
        /*String tuString = "<b>¿Que es necesario para usar la Dojcam?</b>";
        TextView tv1 = (TextView)getView().findViewById(R.id.Preg1);
        tv1.setText(Html.fromHtml(tuString));

        String tuString2 = "<i>Poseer tu dispositivo movil en la posicion adecuada y tener al menos tu metro cuadrado libre, no queremos generar accidentes infortuitos</i>";
        TextView tv11 = (TextView)getView().findViewById(R.id.Res1);
        tv11.setText(Html.fromHtml(tuString2));


        String tuString3 = "<b>¿Por qué existe Dojocam?</b>";
        TextView tv2 = (TextView)getView().findViewById(R.id.Preg2);
        tv2.setText(Html.fromHtml(tuString3));

        String tuString31 = "<i>Porque necesitamos dar frente a los abusos</i>";
        TextView tv21 = (TextView)getView().findViewById(R.id.Res2);
        tv21.setText(Html.fromHtml(tuString31));

        String tuString4 = "<b>¿Como funciona el sistema de entrenamiento?</b>";
        TextView tv3 = (TextView)getView().findViewById(R.id.Preg3);
        tv3.setText(Html.fromHtml(tuString4));

        String tuString5 = "<i>A traves del video previo practico te demostramos que debes realizar, donde deberas seguir el esqueleto celeste </i>";
        TextView tv51 = (TextView)getView().findViewById(R.id.Res3);
        tv51.setText(Html.fromHtml(tuString5));*/

        Button btn = (Button) getView().findViewById(R.id.Prop_preg);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Enviar Pregunta");
                alert.setMessage("");

                final EditText input = new EditText(getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        //DocumentReference ref = mDatabase.getReference("BDQuestions");
                       /* DocumentReference userReference = db.collection("FAQ").document("BDQuestions");
                        bdQuestions.add(value.toString());
                        userReference.update("data",bdQuestions);*/
                        CollectionReference questions = db.collection("FAQTest");
                        questions.add(new Pregunta("",value.toString(),"-Pendiente",new ArrayList<>(),new ArrayList<>()));
                        // Do something with value!
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        Bundle bundle = new Bundle();
        String user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());


        bundle.putString("Id",  id_list.get(pos));
        bundle.putString("name", "Pregunta");
        bundle.putString("userId",user);

        Navigation.findNavController(view).navigate(R.id.pdetail, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();

        //consulta a bd
        DocumentReference userReference = db.collection("FAQ").document("BDQuestions");

        if (userReference != null) {
            userReference.get().addOnSuccessListener(command -> {
                bdQuestions = (List<String>) command.get("data");
            });
        }
        user_list.clear();
        id_list.clear();

        // Get post and answers from database

        Task<QuerySnapshot> data = db.collection("FAQTest").get();
        data.addOnSuccessListener(command -> {
            List<Pregunta> docList = command.toObjects(Pregunta.class);

            if ( data.isComplete() ){
                int i = 0;
                for (Pregunta Pregunta:
                        docList) {
                    String aux = Pregunta.getPregunta();
                    System.out.println(aux);
                    user_list.add(aux);
                    id_list.add(command.getDocuments().get(i).getId());
                    i++;
                }
                adapter2.notifyDataSetChanged();
                loadingDialog.dismissDialog();
                //Toast.makeText(getContext(), "No te veo compare, avispate", Toast.LENGTH_SHORT).show();

                if(i == 0) {
                    // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Tiempo de espera excedido")
                            .setTitle("Error de Conexión");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            //title.setText(command.get("nombre").toString());
            //desc.setText(command.get("descripcion").toString());

        });
        data.addOnFailureListener(command -> {
            loadingDialog.dismissDialog();
            System.out.println("ashjgdkjasgd");


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
        adapter2.getFilter().filter(newText);
        return true;
    }
}