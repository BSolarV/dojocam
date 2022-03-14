package com.pinneapple.dojocam_app;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.pinneapple.dojocam_app.objects.VideoInfo;
import com.pinneapple.dojocam_app.objets.Friends;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Seguidos #newInstance} factory method to
 * create an instance of this fragment.
 */
public class Seguidos extends ListFragment implements AdapterView.OnItemClickListener,SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView title ,desc;

    private List<String> user_list2 = new ArrayList();
    private List<String> id_list2 = new ArrayList();
    private ArrayAdapter adapter;
    private LoadingDialog loadingDialog = new LoadingDialog(this);

    private String search_txt;

    String[] countryNames = new String[100];
    Integer[] imageid = new Integer[100];


    public Seguidos() {
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
    public static Seguidos newInstance(String param1, String param2) {

        Seguidos fragment = new Seguidos();
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
        return inflater.inflate(R.layout.fragment_seguidos, container, false);
    }
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {

        //search_txt = getArguments().getString("difficulty");

        adapter = new ArrayAdapter(getContext(), R.layout.list_friends, user_list2 );
        //ListView lv = (ListView) getView().findViewById(R.id.user_list2);
        //lv.setAdapter(adapter);
        //lv.setOnItemClickListener(this);

        //setContentView(R.layout.fragment_seguidos);


        ListView listView = (ListView) getView().findViewById(R.id.user_list2);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        DocumentReference userReference = db.collection("Friends").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        userReference.get().addOnSuccessListener(command -> {
            boolean bo = false;
            Friends followers = command.toObject(Friends.class);
            if(followers != null) {
                System.out.println(followers.getFollowers());
                int i = 0;
                for (String amiwo : followers.getFollowers()) {
                    System.out.println(amiwo);
                    countryNames[i] = amiwo;
                    imageid[i] = R.mipmap.dojocam_ic;
                    i++;
                    //user_list2.add(amiwo);
                }
            }else{
                Toast.makeText(getContext(),"Busca Amigos en Perfil", Toast.LENGTH_SHORT).show();
                bo = true;
            }
            adapter.notifyDataSetChanged();
            loadingDialog.dismissDialog();
            /*if (bo) {
                Navigation.findNavController(getView()).navigate(R.id.AddFriend);
            }*/
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "No se logro Seguir, intentalo denuevo", e);
            }
        });
        // For populating list data

        CustomCountryList customCountryList = new CustomCountryList((Activity) getContext(), countryNames, imageid);
        listView.setAdapter(customCountryList);
/*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Toast.makeText(getContext(),"Amigo "+countryNames[position-1],Toast.LENGTH_SHORT).show();
            }

        });
*/
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
        Task<QuerySnapshot> data = db.collection("Users").get();

        DocumentReference userReference = db.collection("Friends").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        userReference.get().addOnSuccessListener(command -> {
            Friends followers = command.toObject(Friends.class);
            System.out.println(followers.getFollowers());
            for(String amiwo : followers.getFollowers()) {
                System.out.println(amiwo);
                user_list2.add(amiwo);
            }
            adapter.notifyDataSetChanged();
            loadingDialog.dismissDialog();
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "No se logro Seguir, intentalo denuevo", e);
            }
        });

        bundle.putString("weonId", countryNames[pos]);
        Navigation.findNavController(view).navigate(R.id.perfil_publico, bundle);

    }

    @Override
    public void onResume() {

        super.onResume();

        user_list2.clear();
        //id_list2.clear();

        // Get post and answers from database
/*
        Button add_friend = (Button) getView().findViewById(R.id.AddFriend);
        add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.AddFriend);
            }
        });*/
        DocumentReference userReference = db.collection("Friends").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        /*userReference.get().addOnSuccessListener(command -> {
            boolean bo = false;
            Friends followers = command.toObject(Friends.class);
            if(followers != null) {
                System.out.println(followers.getFollowers());
                for (String amiwo : followers.getFollowers()) {
                    System.out.println(amiwo);
                    user_list2.add(amiwo);
                }
            }else{
                Toast.makeText(getContext(),"Busca Amigos en Perfil", Toast.LENGTH_SHORT).show();
                bo = true;
            }
            adapter.notifyDataSetChanged();
            loadingDialog.dismissDialog();
            /*if (bo) {
                Navigation.findNavController(getView()).navigate(R.id.AddFriend);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "No se logro Seguir, intentalo denuevo", e);
            }
        });*/

        //DocumentReference userReference = db.collection("Friends").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        userReference.get().addOnSuccessListener(command -> {
            boolean bo = false;
            Friends followers = command.toObject(Friends.class);
            if(followers != null) {
                System.out.println(followers.getFollowers());
                int i = 0;
                for (String amiwo : followers.getFollowers()) {
                    System.out.println(amiwo);
                    countryNames[i] = amiwo;
                    imageid[i] = R.mipmap.dojocam_ic;
                    i++;
                    //user_list2.add(amiwo);
                }
            }else{
                Toast.makeText(getContext(),"Busca Amigos en Perfil", Toast.LENGTH_SHORT).show();
                bo = true;
            }
            adapter.notifyDataSetChanged();
            loadingDialog.dismissDialog();
            /*if (bo) {
                Navigation.findNavController(getView()).navigate(R.id.AddFriend);
            }*/
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "No se logro Seguir, intentalo denuevo", e);
            }
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