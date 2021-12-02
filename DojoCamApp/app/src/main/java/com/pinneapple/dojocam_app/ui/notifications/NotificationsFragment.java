package com.pinneapple.dojocam_app.ui.notifications;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentNotificationsBinding;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    private LoadingDialog loadingDialog = new LoadingDialog(this);

    private List<Integer> day_scores = new ArrayList<Integer>();
    private List<Integer> week_scores= new ArrayList<Integer>();
    private List<Integer> month_scores= new ArrayList<Integer>();
    private List<String> exercises_done= new ArrayList<String>();
    private Integer index_key = 0;


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
        day_scores.clear();
        week_scores.clear();
        month_scores.clear();
        exercises_done.clear();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String today = formatter.format(date);

        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        //Consulta a BD por los Scores
        if (userReference != null) {
            userReference.get().addOnSuccessListener(command -> {
                UserData user = command.toObject(UserData.class);
                assert user != null;

                HashMap<String, HashMap<String, List<Integer>>> scores =  new HashMap<>();
                scores =  user.getScores();
                HashMap <String, List<Integer>> exercise_scores = new HashMap<>();

                //Parseo de los scores en las distintas listas
                for ( String key : scores.keySet() ) {
                    exercises_done.add(key);
                }



                //scores de el ejercicio
                exercise_scores = scores.get(exercises_done.get(index_key));

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

                int aux4 = date.getMonth();
                int aux5 = date.getYear();

                int daysInMonth = 0;

                List<String> month_days = printDatesInMonth(aux5, aux4, daysInMonth);

                for (int i = 0; i < 4; i++ ) {
                    int weekprom = 0;
                    int div = 0;
                    for (int j = 0; j < 7; j++ ){
                        List<Integer> day_s = exercise_scores.get(week_days.get(j));
                        int prom = 0 ;
                        if(day_s != null){
                            prom = day_s.stream().mapToInt(Integer::intValue).sum();
                            prom /= day_s.size();
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
                loadingDialog.dismissDialog();
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
                Log.wtf("aa",i + ":" + day_scores.get(i).toString());
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
        RadioButton radio_year = (RadioButton) getView().findViewById(R.id.radio_year);

        radio_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BarChart barChart = (BarChart) getView().findViewById(R.id.barChart);
                ArrayList<BarEntry> dias = new ArrayList<>();


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
                semana.add(new BarEntry(1, 61));
                semana.add(new BarEntry(2, 22));
                semana.add(new BarEntry(3, 25));
                semana.add(new BarEntry(4, 31));
                semana.add(new BarEntry(5, 58));
                semana.add(new BarEntry(6, 25));
                semana.add(new BarEntry(7, 28));


                BarDataSet barDataSet = new BarDataSet(semana, "Puntajes Semanales");

                barDataSet.setColor(Color.CYAN);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);

                BarData barDataweek = new BarData(barDataSet);
                barChart.setFitBars(true);
                barChart.setData(barDataweek);
                ArrayList<String> xLabel = new ArrayList<>();
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
                        return xLabel.get((int)value-1);
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
                ArrayList<String> xLabel = new ArrayList<>();
                xLabel.add("Enero");
                xLabel.add("Febrero");
                xLabel.add("Marzo");
                xLabel.add("Abril");
                xLabel.add("Mayo");
                xLabel.add("Junio");
                xLabel.add("Julio");
                xLabel.add("Agosto");
                xLabel.add("Septiembre");
                xLabel.add("Octubre");
                xLabel.add("Noviembre");
                xLabel.add("Diciembre");
                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        return xLabel.get((int)value-1);
                    }
                });
                mes.add(new BarEntry(1, 61));
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
                mes.add(new BarEntry(12, 30));



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
        radio_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<BarEntry> mes = new ArrayList<>();
                mes.add(new BarEntry(2014, 61));
                mes.add(new BarEntry(2015, 22));
                mes.add(new BarEntry(3, 25));

                BarDataSet barDataSet = new BarDataSet(mes, "Puntajes Mensuales");

                barDataSet.setColor(Color.RED);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);

                BarData barDataweek = new BarData(barDataSet);
                barChart.setFitBars(true);
                barChart.setData(barDataweek);
                barChart.animateY(2000);
            }
        });
    }


}
