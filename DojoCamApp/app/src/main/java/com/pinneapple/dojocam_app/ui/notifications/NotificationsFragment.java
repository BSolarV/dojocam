package com.pinneapple.dojocam_app.ui.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentNotificationsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BarChart barChart = (BarChart) getView().findViewById(R.id.barChart);
        ArrayList<BarEntry> dias = new ArrayList<>();
        dias.add(new BarEntry(2, 75));
        dias.add(new BarEntry(3, 22));
        dias.add(new BarEntry(1, 25));
        dias.add(new BarEntry(4, 31));
        dias.add(new BarEntry(5, 124));
        dias.add(new BarEntry(6, 25));
        dias.add(new BarEntry(7, 28));
        dias.add(new BarEntry(8, 29));
        dias.add(new BarEntry(9, 40));
        dias.add(new BarEntry(10, 21));
        dias.add(new BarEntry(11, 67));

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
                barChart.getDescription().setText("Puntajes Semanales");
                barChart.animateY(2000);
            }
        });
    }
}
