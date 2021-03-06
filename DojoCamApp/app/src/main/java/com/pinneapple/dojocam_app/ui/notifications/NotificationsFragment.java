package com.pinneapple.dojocam_app.ui.notifications;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentNotificationsBinding;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private Boolean isLoading = false;

    private Integer dataSegment = 0;

    private List<Integer> day_scores = new ArrayList<Integer>();
    private List<Integer> week_scores= new ArrayList<Integer>();
    private List<Integer> month_scores= new ArrayList<Integer>();

    private List<Integer> day_percent = new ArrayList<Integer>();
    private List<Integer> week_percent= new ArrayList<Integer>();
    private List<Integer> month_percent= new ArrayList<Integer>();

    private List<Integer> day_times = new ArrayList<Integer>();
    private List<Integer> week_times = new ArrayList<Integer>();
    private List<Integer> month_times = new ArrayList<Integer>();

    private List<String> exercises_done= new ArrayList<String>();
    private List<String> exercises_done_names= new ArrayList<String>();
    private List<String> exercises_done_nindex= new ArrayList<String>();

    private Integer best_score = 0;
    private Integer times_done = 0;
    private Boolean firstTime = true;

    private ArrayAdapter<String> arrayAdapter;

    private Integer index_key = 0;

    private ArrayList<String> xLabel = new ArrayList<>();

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private Date date;
    private String today;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Boolean retrieved = false;
    private Map< String, Object > DBScores;

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

        //Borrando exercisesDone
        try {
            exercises_done.clear();
            exercises_done_nindex.clear();
            exercises_done_names.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MainActivity.checkLogin(requireActivity());
        DocumentReference scoresReference = db.collection("Scores").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        //Consulta a BD por los Scores
        scoresReference.get().addOnSuccessListener(command -> {

            //Exercises_done_names
            Task<QuerySnapshot> data = db.collection("ejercicios").get();

            data.addOnSuccessListener(command2 -> {
                HashMap< String, HashMap<String, HashMap<String, List<Integer>>>> scores =  new HashMap<>();
                DBScores = command.getData();
                retrieved = true;

                //Parseo de los scores en las distintas listas

                if(DBScores != null){
                    exercises_done = new ArrayList<String>(DBScores.keySet());
                    /*
                    for ( String key : scores.keySet() ) {
                        if(key.length() ==  20){
                            exercises_done.add(key);
                        }
                    }*/

                    List<com.pinneapple.dojocam_app.objects.VideoInfo> docList = command2.toObjects(com.pinneapple.dojocam_app.objects.VideoInfo.class);
                    if ( data.isComplete() ) {
                        int j = 0;
                        int i = 0;
                        for (com.pinneapple.dojocam_app.objects.VideoInfo videoInfo :
                                docList) {
                            if(exercises_done.contains(command2.getDocuments().get(i).getId())) {
                                exercises_done_names.add(videoInfo.getNombre());
                                exercises_done_nindex.add(command2.getDocuments().get(i).getId());
                                j++;
                            }
                            i++;
                        }

                    }

                    setUpData();
                    displayBarsBySegment();
                }else {
                    loadingDialog.dismissDialog();
                    isLoading = false;
                }

            });

        });

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!isLoading){
            loadingDialog.startLoadingDialog();
            isLoading = true;
        }

        date = new Date();
        today = formatter.format(date);
        TextView dateDisplay = view.findViewById(R.id.statisticsDateDisplay);
        dateDisplay.setText(today);

        ImageButton prevDayButton = view.findViewById(R.id.statisticsPrevDay);
        ImageButton nextDayButton = view.findViewById(R.id.statisticsNextDay);
        nextDayButton.setEnabled(false);

        prevDayButton.setOnClickListener(view1 -> {
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(formatter.parse(today));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE, -1);  // number of days to add
            today = formatter.format(c.getTime());
            dateDisplay.setText(today);

            nextDayButton.setEnabled(true);

            setUpData();
            displayBarsBySegment();
        });

        nextDayButton.setOnClickListener(view1 -> {
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(formatter.parse(today));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE, 1);  // number of days to add
            today = formatter.format(c.getTime());
            dateDisplay.setText(today);

            Date todayDate = new Date();
            String todayDateString = formatter.format(todayDate);
            if(today.equals(todayDateString))
                nextDayButton.setEnabled(false);

            setUpData();
            displayBarsBySegment();
        });

        setUpData();
        displayBarsBySegment();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpData(){

        if ( !retrieved ) {
            return;
        }

        //Borrando dayScores
        try {
            day_scores.clear();
            day_times.clear();
            day_percent.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Borrando weekScores
        try {
            week_scores.clear();
            week_percent.clear();
            week_times.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Borrando monthScores
        try {
            month_scores.clear();
            month_times.clear();
            month_percent.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        best_score = 0;
        times_done = 0;

        if(!isLoading){
            loadingDialog.startLoadingDialog();
            isLoading = true;
        }

        HashMap <String, HashMap<String, List<Integer>>> exercise_scores = new HashMap<>();

        //scores de el ejercicio
        exercise_scores =  new HashMap<String, HashMap<String, List<Integer>>>();

        HashMap<String, HashMap<String, List<Long>>> scoresLong = (HashMap<String, HashMap<String, List<Long>>>) DBScores.get(exercises_done_nindex.get(index_key));

        for (String oneDate : scoresLong.keySet()){
            for ( String type: scoresLong.get(oneDate).keySet() ){
                if( !exercise_scores.containsKey(oneDate) ) {
                    exercise_scores.put(oneDate, new HashMap<String, List<Integer>>());
                }
                if( !exercise_scores.get(oneDate).containsKey(type) ){
                    exercise_scores.get(oneDate).put(type, new ArrayList<Integer>());
                }
                for(int index = 0; index < scoresLong.get(oneDate).get(type).size(); index++){
                    exercise_scores.get(oneDate).get(type).add( scoresLong.get(oneDate).get(type).get(index).intValue() );
                }
            }
        }

        day_scores = new ArrayList<Integer>();
        day_percent = new ArrayList<Integer>();
        day_times = new ArrayList<Integer>();
        if(exercise_scores.containsKey(today)) {
            day_scores = Objects.requireNonNull(exercise_scores.get(today)).get("scores");
            day_percent = Objects.requireNonNull(exercise_scores.get(today)).get("percent");
            day_times = Objects.requireNonNull(exercise_scores.get(today)).get("times");

        }

        // Se crean las fechas
        int aux = date.getDay();
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(formatter.parse(today));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //sumar o restar d??as
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
            if( exercise_scores.containsKey(week_days.get(i)) ){
                List<Integer> day_s = Objects.requireNonNull(exercise_scores.get(week_days.get(i))).get("scores");
                List<Integer> day_p = Objects.requireNonNull(exercise_scores.get(week_days.get(i))).get("percent");
                List<Integer> day_t = Objects.requireNonNull(exercise_scores.get(week_days.get(i))).get("times");
                int prom_score = 0;
                int prom_percent = 0;
                int prom_time = 0;
                if(day_s != null){
                    int sum_s = day_s.stream().mapToInt(Integer::intValue).sum();
                    int sum_p = day_p.stream().mapToInt(Integer::intValue).sum();
                    int sum_t = day_t.stream().mapToInt(Integer::intValue).sum();

                    prom_score = (Integer) Math.round( (float) sum_s / day_s.size() );
                    prom_percent = (Integer) Math.round( (float) sum_p / day_s.size() );
                    prom_time = (Integer) Math.round( (float) sum_t / day_s.size() );
                }
                week_scores.add(prom_score);
                week_percent.add(prom_percent);
                week_times.add(prom_time);
            }else{
                week_scores.add(0);
                week_percent.add(0);
                week_times.add(0);
            }
        }

        int aux4 = date.getMonth() + 1;
        int aux5 = date.getYear() + 1900;

        int daysInMonth = 0;

        List<String> month_days = printDatesInMonth(aux5, aux4, daysInMonth);

        for (int i = 0; i < 4; i++ ) {

            int weekprom_s = 0;
            int weekprom_p = 0;
            int weekprom_t = 0;
            int div = 0;

            for (int j = 0; j < 7; j++ ){

                if( exercise_scores.containsKey(month_days.get(j+i*7)) ){

                    List<Integer> day_s = exercise_scores.get(month_days.get(j+i*7)).get("scores");
                    List<Integer> day_p = exercise_scores.get(month_days.get(j+i*7)).get("percent");
                    List<Integer> day_t = exercise_scores.get(month_days.get(j+i*7)).get("times");

                    if(day_s != null){
                        weekprom_s += day_s.stream().mapToInt(Integer::intValue).sum();
                        weekprom_p += day_p.stream().mapToInt(Integer::intValue).sum();
                        weekprom_t += day_t.stream().mapToInt(Integer::intValue).sum();
                        div += day_s.size();

                        times_done += day_s.size();
                        if ((Integer) Collections.max(day_s) > best_score) {
                            best_score = (Integer) Collections.max(day_s);
                        }

                    }
                }
            }
            if (div != 0) {
                weekprom_s /=  div;
                weekprom_p /=  div;
                weekprom_t /=  div;
            }
            month_scores.add(weekprom_s);
            month_percent.add(weekprom_p);
            month_times.add(weekprom_t);
        }
        setAll();
        arrayAdapter.notifyDataSetChanged();

        if(isLoading){
            loadingDialog.dismissDialog();
            isLoading = false;
        }
    }

    public void setAll(){

        arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, exercises_done_names );

        if (firstTime) {
            binding.autoCompleteTextView.setText(exercises_done_names.get(index_key));
            firstTime = false;
        }
        binding.autoCompleteTextView.setAdapter(arrayAdapter);

        binding.autoCompleteTextView.setOnItemClickListener(this);



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
                dataSegment = 0;
                displayBarsBySegment();
            }

        });

        radio_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSegment = 1;
                displayBarsBySegment();
            }
        });
        radio_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSegment = 2;
                displayBarsBySegment();
            }
        });

    }

    private void initBars(){

        BarChart barChart_s = (BarChart) requireView().findViewById(R.id.barChart);
        BarChart barChart_p = (BarChart) requireView().findViewById(R.id.barChartPerformance);
        BarChart barChart_t = (BarChart) requireView().findViewById(R.id.barChartTime);

        ArrayList<BarEntry> dias_s = new ArrayList<>();
        ArrayList<BarEntry> dias_p = new ArrayList<>();
        ArrayList<BarEntry> dias_t = new ArrayList<>();

        if(day_scores != null) {
            for (int i = 0; i < day_scores.size(); i++) {
                dias_s.add(new BarEntry (i+1,day_scores.get(i)));
                dias_p.add(new BarEntry (i+1,day_percent.get(i)));
                dias_t.add(new BarEntry (i+1,day_times.get(i)));
                //Log.wtf("aa",i + ":" + day_scores.get(i).toString());
            }
            //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();
        }else {
            //Toast.makeText(getContext(),"???",Toast.LENGTH_SHORT).show();
        }


        BarDataSet barDataSet_s = new BarDataSet(dias_s, "Dia");
        BarDataSet barDataSet_p = new BarDataSet(dias_p, "Dia");
        BarDataSet barDataSet_t = new BarDataSet(dias_t, "Dia");

        barDataSet_s.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet_s.setValueTextColor(Color.BLACK);
        barDataSet_s.setValueTextSize(16f);

        barDataSet_p.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet_p.setValueTextColor(Color.BLACK);
        barDataSet_p.setValueTextSize(16f);

        barDataSet_t.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet_t.setValueTextColor(Color.BLACK);
        barDataSet_t.setValueTextSize(16f);


        BarData barData_s = new BarData(barDataSet_s);
        BarData barData_p = new BarData(barDataSet_p);
        BarData barData_t = new BarData(barDataSet_t);


        barChart_s.setFitBars(true);
        barChart_s.setData(barData_s);
        barChart_s.getDescription().setText("Puntajes Diarios");
        barChart_s.animateY(2000);

        barChart_p.setFitBars(true);
        barChart_p.setData(barData_p);
        barChart_p.getDescription().setText("Porcentaje de Logro");
        barChart_p.animateY(2000);

        barChart_t.setFitBars(true);
        barChart_t.setData(barData_t);
        barChart_t.getDescription().setText("Tiempo de Entrenamiento");
        barChart_t.animateY(2000);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        index_key = i;
        setUpData();
        displayBarsBySegment();
    }

    private void displayBarsBySegment(){

        BarChart barChart_s = (BarChart) requireView().findViewById(R.id.barChart);
        BarChart barChart_p = (BarChart) requireView().findViewById(R.id.barChartPerformance);
        BarChart barChart_t = (BarChart) requireView().findViewById(R.id.barChartTime);

        if( barChart_s == null ) return;

        BarDataSet barDataSet_s;
        BarDataSet barDataSet_p;
        BarDataSet barDataSet_t;

        BarData barData_s;
        BarData barData_p;
        BarData barData_t;

        XAxis xAxis;

        switch (dataSegment){
            case 0:
                resetChart(barChart_s);
                resetChart(barChart_p);
                resetChart(barChart_t);

                ArrayList<BarEntry> dias_s = new ArrayList<>();
                ArrayList<BarEntry> dias_p = new ArrayList<>();
                ArrayList<BarEntry> dias_t = new ArrayList<>();

                if(day_scores != null) {
                    for (int i = 0; i < day_scores.size(); i++) {
                        dias_s.add(new BarEntry (i+1,day_scores.get(i)));
                        dias_p.add(new BarEntry (i+1,day_percent.get(i)));
                        dias_t.add(new BarEntry (i+1,day_times.get(i)));
                    }
                }

                barDataSet_s = new BarDataSet(dias_s, "Vez del d??a");
                barDataSet_p = new BarDataSet(dias_p, "Vez del d??a");
                barDataSet_t = new BarDataSet(dias_t, "Vez del d??a");

                barDataSet_s.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_s.setValueTextColor(Color.BLACK);
                barDataSet_s.setValueTextSize(16f);

                barDataSet_p.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_p.setValueTextColor(Color.BLACK);
                barDataSet_p.setValueTextSize(16f);

                barDataSet_t.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_t.setValueTextColor(Color.BLACK);
                barDataSet_t.setValueTextSize(16f);

                barData_s = new BarData(barDataSet_s);
                barData_p = new BarData(barDataSet_p);
                barData_t = new BarData(barDataSet_t);

                barChart_s.setFitBars(true);
                barChart_s.setData(barData_s);
                barChart_s.getDescription().setText("Puntajes Diarios");
                barChart_s.animateY(2000);

                barChart_p.setFitBars(true);
                barChart_p.setData(barData_p);
                barChart_p.getDescription().setText("Porcentaje de Logro");
                barChart_p.animateY(2000);

                barChart_t.setFitBars(true);
                barChart_t.setData(barData_t);
                barChart_t.getDescription().setText("Tiempo de Entrenamiento");
                barChart_t.animateY(2000);

                xLabel.clear();
                for( int i = 0; i <= day_scores.size(); i++ )  xLabel.add(Integer.toString(i));

                xAxis = barChart_s.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));

                xAxis = barChart_p.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));

                xAxis = barChart_t.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));

                break;

            case 1:
                resetChart(barChart_s);
                resetChart(barChart_p);
                resetChart(barChart_t);

                xLabel.clear();
                xLabel.add("");
                xLabel.add("Lunes");
                xLabel.add("Martes");
                xLabel.add("Miercoles");
                xLabel.add("Jueves");
                xLabel.add("Viernes");
                xLabel.add("Sabado");
                xLabel.add("Domingo");

                xAxis = barChart_s.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelCount(week_scores.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 8.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                xAxis = barChart_p.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelCount(week_scores.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 8.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                xAxis = barChart_t.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelCount(week_scores.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 8.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                ArrayList<BarEntry> semana_s = new ArrayList<>();
                ArrayList<BarEntry> semana_p = new ArrayList<>();
                ArrayList<BarEntry> semana_t = new ArrayList<>();

                if(week_scores != null) {
                    for (int i = 0; i < week_scores.size(); i++) {
                        semana_s.add(new BarEntry (i+1,week_scores.get(i)));
                        semana_p.add(new BarEntry (i+1,week_percent.get(i)));
                        semana_t.add(new BarEntry (i+1,week_times.get(i)));
                        //Log.wtf("aa",i + ":" + day_scores.get(i).toString());
                    }
                    //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();
                }


                barDataSet_s = new BarDataSet(semana_s, "D??a");
                barDataSet_p = new BarDataSet(semana_p, "D??a");
                barDataSet_t = new BarDataSet(semana_t, "D??a");

                barDataSet_s.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_s.setValueTextColor(Color.BLACK);
                barDataSet_s.setValueTextSize(16f);

                barDataSet_p.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_p.setValueTextColor(Color.BLACK);
                barDataSet_p.setValueTextSize(16f);

                barDataSet_t.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_t.setValueTextColor(Color.BLACK);
                barDataSet_t.setValueTextSize(16f);

                BarData barDataweek_s = new BarData(barDataSet_s);
                BarData barDataweek_p = new BarData(barDataSet_p);
                BarData barDataweek_t = new BarData(barDataSet_t);

                barChart_s.setFitBars(true);
                barChart_s.setData(barDataweek_s);
                barChart_s.getDescription().setText("Puntajes Diarios");
                barChart_s.animateY(2000);

                barChart_p.setFitBars(true);
                barChart_p.setData(barDataweek_p);
                barChart_p.getDescription().setText("Porcentaje de Logro");
                barChart_p.animateY(2000);

                barChart_t.setFitBars(true);
                barChart_t.setData(barDataweek_t);
                barChart_t.getDescription().setText("Tiempo de Entrenamiento");
                barChart_t.animateY(2000);

                break;

            case 2:
                resetChart(barChart_s);
                resetChart(barChart_p);
                resetChart(barChart_t);

                ArrayList<BarEntry> mes_s = new ArrayList<>();
                ArrayList<BarEntry> mes_p = new ArrayList<>();
                ArrayList<BarEntry> mes_t = new ArrayList<>();

                //List<String> xxLabel = new ArrayList<>();

                xLabel.clear();
                //xLabel.add("");
                xLabel.add("Semana 1");
                xLabel.add("Semana 2");
                xLabel.add("Semana 3");
                xLabel.add("Semana 4");
                xAxis = barChart_s.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelCount(month_scores.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 4.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                xAxis = barChart_p.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelCount(month_scores.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 4.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                xAxis = barChart_t.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelCount(month_scores.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if(value >= 4.0) {
                            return "";
                        }
                        return xLabel.get((int)value);
                    }
                });

                if(month_scores != null) {
                    for (int i = 0; i < month_scores.size(); i++) {
                        mes_s.add(new BarEntry (i+1,month_scores.get(i)));
                        mes_p.add(new BarEntry (i+1,month_percent.get(i)));
                        mes_t.add(new BarEntry (i+1,month_times.get(i)));
                        //Toast.makeText(getContext(),month_scores.get(2).toString(), Toast.LENGTH_SHORT).show();
                        //Log.wtf("aa",i + ":" + day_scores.get(i).toString());
                    }
                    //Toast.makeText(getContext(),day_scores.get(0).toString(), Toast.LENGTH_SHORT).show();
                }

                barDataSet_s = new BarDataSet(mes_s, "Semana");
                barDataSet_p = new BarDataSet(mes_p, "Semana");
                barDataSet_t = new BarDataSet(mes_t, "Semana");

                barDataSet_s.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_s.setValueTextColor(Color.BLACK);
                barDataSet_s.setValueTextSize(16f);

                barDataSet_p.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_p.setValueTextColor(Color.BLACK);
                barDataSet_p.setValueTextSize(16f);

                barDataSet_t.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet_t.setValueTextColor(Color.BLACK);
                barDataSet_t.setValueTextSize(16f);

                barData_s = new BarData(barDataSet_s);
                barData_p = new BarData(barDataSet_p);
                barData_t = new BarData(barDataSet_t);

                barChart_s.setFitBars(true);
                barChart_s.setData(barData_s);
                barChart_s.getDescription().setText("Promedio Puntaje Semanal");
                barChart_s.animateY(2000);

                barChart_p.setFitBars(true);
                barChart_p.setData(barData_p);
                barChart_p.getDescription().setText("Porcentaje de Logro Semanal");
                barChart_p.animateY(2000);

                barChart_t.setFitBars(true);
                barChart_t.setData(barData_t);
                barChart_t.getDescription().setText("Tiempo de Entrenamiento Semanal");
                barChart_t.animateY(2000);

                break;
        }
    }

    private void resetChart(BarChart barChart) {
        if( barChart.getData() != null ) barChart.getData().clearValues();
        barChart.getXAxis().setValueFormatter(null);
        barChart.notifyDataSetChanged();
        barChart.clear();
        barChart.invalidate();
    }
}
