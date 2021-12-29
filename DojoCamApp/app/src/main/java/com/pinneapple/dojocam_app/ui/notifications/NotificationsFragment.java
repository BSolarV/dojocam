package com.pinneapple.dojocam_app.ui.notifications;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.GroupList;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentNotificationsBinding;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class NotificationsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    private LoadingDialog loadingDialog = new LoadingDialog(this);

    private List<Integer> day_scores = new ArrayList<Integer>();
    private List<Integer> week_scores= new ArrayList<Integer>();
    private List<Integer> month_scores= new ArrayList<Integer>();
    private List<String> exercises_done= new ArrayList<String>();
    private List<String> exercises_done_names= new ArrayList<String>();
    private List<String> exercises_done_nindex= new ArrayList<String>();
    private Integer best_score = 0;
    private Integer times_done = 0;
    private Boolean firstTime = true;


    private ArrayAdapter arrayAdapter;

    private Integer index_key = 0;

    private ArrayList<String> xLabel = new ArrayList<>();


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        consultScores();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void consultScores() {
        //Borrando dayScores
        try {
            day_scores.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Borrando weekScores
        try {
            week_scores.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Borrando monthScores
        try {
            month_scores.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Borrando exercisesDone
        try {
            exercises_done.clear();
            exercises_done_nindex.clear();
            exercises_done_names.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }


        best_score = 0;
        times_done = 0;




        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String today = formatter.format(date);

        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        //Consulta a BD por los Scores
        if (userReference != null) {
            userReference.get().addOnSuccessListener(command -> {


                //Exercises_done_names
                Task<QuerySnapshot> data = db.collection("ejercicios").get();

                data.addOnSuccessListener(command2 -> {
                    UserData user = command.toObject(UserData.class);
                    assert user != null;

                    HashMap<String, HashMap<String, List<Integer>>> scores =  new HashMap<>();
                    scores =  user.getScores();
                    HashMap <String, List<Integer>> exercise_scores = new HashMap<>();

                    //Parseo de los scores en las distintas listas

                    if(scores != null){
                        for ( String key : scores.keySet() ) {
                            if(key.length() ==  20){
                                exercises_done.add(key);
                            }
                        }

                        List<com.pinneapple.dojocam_app.objects.VideoInfo> docList = command2.toObjects(com.pinneapple.dojocam_app.objects.VideoInfo.class);
                        if ( data.isComplete() ) {
                            int j = 0;
                            int i = 0;
                            for (com.pinneapple.dojocam_app.objects.VideoInfo videoInfo :
                                    docList) {
                                if(exercises_done.contains(command2.getDocuments().get(i).getId().toString())) {
                                    exercises_done_names.add(videoInfo.getNombre().toString());
                                    exercises_done_nindex.add(command2.getDocuments().get(i).getId().toString());
                                    j++;
                                }
                                i++;
                            }

                        }

                        //scores de el ejercicio
                        exercise_scores = scores.get(exercises_done_nindex.get(index_key));

                        day_scores = exercise_scores.get(today);



                        //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();


                        for ( String key : exercise_scores.keySet() ) {
                            exercises_done.add(key.toString());
                        }
                        int aux = date.getDay();

                        Calendar c = Calendar.getInstance();
                        try {
                            c.setTime(formatter.parse(today));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //sumar o restar días
                        if(aux != 0){
                            c.add(Calendar.DATE, -(aux-1));  // number of days to add
                        }else { //Domingo le resto 6
                            c.add(Calendar.DATE, -6);
                        }
                        String monday = formatter.format(c.getTime());  // dt is now the new date

                        //creo una lista de los dias
                        List<String> week_days = new ArrayList<String>();
                        week_days.add(monday);
                        for (int i = 1; i<7; i++ ) {
                            try {
                                c.setTime(formatter.parse(monday));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            c.add(Calendar.DATE, i);
                            String aux3 = formatter.format(c.getTime());
                            week_days.add(aux3);
                        }

                        //obtengo del exercises_scores
                        for (int i = 0; i<7; i++ ) {
                            List<Integer> day_s = exercise_scores.get(week_days.get(i));
                            int prom = 0 ;
                            if(day_s != null){
                                prom = day_s.stream().mapToInt(Integer::intValue).sum();
                                prom /= day_s.size();
                            }
                            week_scores.add(prom);
                        }

                        int aux4 = date.getMonth() + 1;
                        int aux5 = date.getYear() + 1900;

                        int daysInMonth = 0;

                        List<String> month_days = printDatesInMonth(aux5, aux4, daysInMonth);

                        for (int i = 0; i < 4; i++ ) {
                            int weekprom = 0;
                            int div = 0;
                            for (int j = 0; j < 7; j++ ){
                                int prom = 0 ;
                                List<Integer> day_s = exercise_scores.get(month_days.get(j+i*7));
                                if(day_s != null){
                                    prom = day_s.stream().mapToInt(Integer::intValue).sum();
                                    prom /= day_s.size();

                                    times_done += day_s.size();
                                    if ((Integer) Collections.max(day_s) > best_score) {
                                        best_score = (Integer) Collections.max(day_s);
                                    }

                                    div++;
                                }
                                weekprom += prom;

                            }
                            if (div != 0) {
                                weekprom /= div;
                            }

                            month_scores.add(weekprom);

                        }
                        setAll();
                        arrayAdapter.notifyDataSetChanged();
                    }
                    loadingDialog.dismissDialog();



                });



            });
        } else {
            Toast.makeText(getContext(), "aaa", Toast.LENGTH_SHORT).show();
        }

    }
    private List<String> printDatesInMonth(int year, int month, int daysInMonth) {
        List<String> array = new ArrayList<String>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, 1);
        daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < daysInMonth; i++) {
            array.add(fmt.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return array;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog.startLoadingDialog();

    }

    public void setAll(){

        arrayAdapter = new ArrayAdapter(getContext(), R.layout.dropdown_item, exercises_done_names );

        if (firstTime) {
            binding.autoCompleteTextView.setText(exercises_done_names.get(index_key));
            firstTime = false;
        }
        binding.autoCompleteTextView.setAdapter(arrayAdapter);

        binding.autoCompleteTextView.setOnItemClickListener(this);


        BarChart barChart = (BarChart) getView().findViewById(R.id.barChart);
        ArrayList<BarEntry> dias = new ArrayList<>();
        /*dias.add(new BarEntry(1, 25));
        dias.add(new BarEntry(2, 75));
        dias.add(new BarEntry(3, 22));
        dias.add(new BarEntry(4, 31));
        dias.add(new BarEntry(5, 124));
        dias.add(new BarEntry(6, 25));
        dias.add(new BarEntry(7, 28));
        dias.add(new BarEntry(8, 29));
        dias.add(new BarEntry(9, 40));
        dias.add(new BarEntry(10, 21));
        dias.add(new BarEntry(11, 67));*/

        if(day_scores != null) {
            for (int i = 0; i < day_scores.size(); i++) {
                dias.add(new BarEntry (i+1,day_scores.get(i)));
                //Log.wtf("aa",i + ":" + day_scores.get(i).toString());
            }
            //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();
        }else {
            //Toast.makeText(getContext(),"???",Toast.LENGTH_SHORT).show();
        }


        BarDataSet barDataSet = new BarDataSet(dias, "Dia");

        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);
        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("Puntajes Diarios");
        barChart.animateY(2000);

        RadioButton radio_day = (RadioButton) getView().findViewById(R.id.radio_day);
        RadioButton radio_week = (RadioButton) getView().findViewById(R.id.radio_week);
        RadioButton radio_month = (RadioButton) getView().findViewById(R.id.radio_month);

        TextView bestScore = (TextView) getView().findViewById(R.id.bestScore);
        TextView timesDone = (TextView) getView().findViewById(R.id.timesDone);

        bestScore.setText(best_score.toString());
        timesDone.setText(times_done.toString());




        radio_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BarChart barChart = (BarChart) getView().findViewById(R.id.barChart);
                ArrayList<BarEntry> dias = new ArrayList<>();
                //xLabel.clear();

                if(day_scores != null) {
                    for (int i = 0; i < day_scores.size(); i++) {
                        dias.add(new BarEntry (i+1,day_scores.get(i)));
                    }
                }


                BarDataSet barDataSet = new BarDataSet(dias, "Dia");

                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);

                BarData barData = new BarData(barDataSet);
                barChart.setFitBars(true);
                barChart.setData(barData);
                barChart.getDescription().setText("Puntajes Diarios");
                barChart.animateY(2000);
            }

        });

        radio_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<BarEntry> semana = new ArrayList<>();
                /*semana.add(new BarEntry(1, 61));
                semana.add(new BarEntry(2, 22));
                semana.add(new BarEntry(3, 25));
                semana.add(new BarEntry(4, 31));
                semana.add(new BarEntry(5, 58));
                semana.add(new BarEntry(6, 25));
                semana.add(new BarEntry(7, 28));*/

                if(week_scores != null) {
                    for (int i = 0; i < week_scores.size(); i++) {
                        semana.add(new BarEntry (i+1,week_scores.get(i)));
                        //Log.wtf("aa",i + ":" + day_scores.get(i).toString());
                    }
                    //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();
                }


                BarDataSet barDataSet = new BarDataSet(semana, "Puntajes Semanales");

                barDataSet.setColor(Color.CYAN);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);

                BarData barDataweek = new BarData(barDataSet);
                barChart.setFitBars(true);
                barChart.setData(barDataweek);

                xLabel.clear();
                xLabel.add("");
                xLabel.add("Lunes");
                xLabel.add("Martes");
                xLabel.add("Miercoles");
                xLabel.add("Jueves");
                xLabel.add("Viernes");
                xLabel.add("Sabado");
                xLabel.add("Domingo");

                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 8.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                barChart.getDescription().setText("Puntaje Semanal");
                barChart.animateY(2000);
            }
        });
        radio_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<BarEntry> mes = new ArrayList<>();
                //List<String> xxLabel = new ArrayList<>();

                xLabel.clear();
                //xLabel.add("");
                xLabel.add("Semana 1");
                xLabel.add("Semana 2");
                xLabel.add("Semana 3");
                xLabel.add("Semana 4");
                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 4.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });
                /*mes.add(new BarEntry(1, 61));
                mes.add(new BarEntry(2, 22));
                mes.add(new BarEntry(3, 25));
                mes.add(new BarEntry(4, 31));
                mes.add(new BarEntry(5, 58));
                mes.add(new BarEntry(6, 25));
                mes.add(new BarEntry(7, 39));
                mes.add(new BarEntry(8, 48));
                mes.add(new BarEntry(9, 50));
                mes.add(new BarEntry(10, 28));
                mes.add(new BarEntry(11, 18));
                mes.add(new BarEntry(12, 30));*/

                if(month_scores != null) {
                    for (int i = 0; i < month_scores.size(); i++) {
                        mes.add(new BarEntry (i+1,month_scores.get(i)));
                        //Toast.makeText(getContext(),month_scores.get(2).toString(), Toast.LENGTH_SHORT).show();
                        //Log.wtf("aa",i + ":" + day_scores.get(i).toString());
                    }
                    //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();
                }



                BarDataSet barDataSet = new BarDataSet(mes, "Puntajes Mensuales");

                barDataSet.setColor(Color.BLUE);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);

                BarData barDatamonth = new BarData(barDataSet);
                barChart.setFitBars(true);
                barChart.setData(barDatamonth);
                barChart.getDescription().setText("Puntajes Mensuales");
                barChart.animateY(2000);

            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        index_key = i;
        consultScores();
    }
}
