package com.example.tripscheduler.Schedule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripscheduler.R;
import com.example.tripscheduler.Server.IAppService;
import com.example.tripscheduler.Server.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.Collections;
import retrofit2.Retrofit;

public class ScheduleViewPagerFragment extends Fragment {

  IAppService apiService;
  private final String SERVER = "http://143.248.36.205:3000";
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private IAppService iAppService;

  ScheduleListViewAdapter adapter;
  ArrayList<Schedule> scheduleList;

  int sectionNumber;
  String title;
  String email;

  public ScheduleViewPagerFragment(int sectionNumber, String title, String email) {
    this.sectionNumber = sectionNumber;
    this.title = title;
    this.email = email;
  }

  public static ScheduleViewPagerFragment newInstance(int sectionNumber, String title,
      String email) {
    ScheduleViewPagerFragment fragment = new ScheduleViewPagerFragment(sectionNumber, title, email);

//        Bundle args = new Bundle();
//        args.putInt("section_number", sectionNumber);
//        fragment.setArguments(args);

    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.layout_schedule_view_pager, container, false);

    Retrofit retrofitClient = RetrofitClient.getInstance();
    iAppService = retrofitClient.create(IAppService.class);

    ListView scheduleListView = rootView.findViewById(R.id.scheduleListView);
    scheduleList = new ArrayList<>();

    // guess, should add Schedule items here <- currently it's just travel, but should manage later :P

//        scheduleList.add(new Schedule(email, title, "Place Name", "Location", "Label", "Short Memo", "6 00", "30"));
//        scheduleList.add(new Schedule(email, title, "Place Name2", "Location", "Label", "Short Memo", "7 00", "30"));
//        scheduleList.add(new Schedule(email, title, "Place Name3", "Location", "Label", "Short Memo", "8 00", "30"));
//        scheduleList.add(new Schedule(email, title, "Place Name4", "Location", "Label", "Short Memo", "15 00", "30"));

    compositeDisposable.add(iAppService.schedule_get_one(email.replace("\"", ""), title)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .retry()
        .subscribe(new Consumer<String>() {
          @Override
          public void accept(String data) throws Exception {
            Log.e("schedule_get_one", data);

            if (data.equals("0")) {
              System.out.println("No data existed");
            } else {

              JsonParser jsonParser = new JsonParser();
              JsonArray jsonArray = (JsonArray) jsonParser.parse(data);

              for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject object = (JsonObject) jsonArray.get(i);
                System.out.println(object.get("date").toString().replace("-", "."));
                System.out.println(object.get("schedule"));

                JsonArray array = (JsonArray) object.get("schedule");
                for (int j = 0; j < array.size(); j++) {
                  JsonObject object2 = (JsonObject) array.get(j);
                  System.out.println(object2.get("name"));
                  System.out.println(object2.get("location"));
                  System.out.println(object2.get("label"));
                  System.out.println(object2.get("memo"));
                  System.out.println(object2.get("start"));
                  System.out.println(object2.get("duration"));

                  JsonArray location = (JsonArray) object2.get("location");
//                                    System.out.println(location.get(0));
//                                    System.out.println(location.get(1));

                  if (sectionNumber == i) {
                    scheduleList.add(new Schedule(email, title, object2.get("name").toString(),
                        object2.get("location").toString(),
                        object2.get("label").toString(), object2.get("memo").toString(),
                        object2.get("start").toString(),
                        object2.get("duration").toString()));
                    Collections.sort(scheduleList);

                    adapter.notifyDataSetChanged();
                  }
//                                    adapter = new ScheduleListViewAdapter(scheduleList);
                }
              }
            }
          }
        }));

    adapter = new ScheduleListViewAdapter(scheduleList);
    scheduleListView.setAdapter(adapter);
    scheduleListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView parent, View v, int position, long id) {

        Schedule schedule = (Schedule) parent.getItemAtPosition(position);
        if (schedule.getData("label").replace("\"","").equals("이동")) {
          Schedule schedule1 = (Schedule) parent.getItemAtPosition(position - 1);
          Schedule schedule2 = (Schedule) parent.getItemAtPosition(position + 1);

          Intent transportIntent = new Intent(getActivity(),ScheduleEdgeMapActivity.class);
          transportIntent.putExtra("schedule1",schedule1);
          transportIntent.putExtra("schedule2",schedule2);
          startActivity(transportIntent);

        } else {

          Intent scheduleMapIntent = new Intent(getActivity(),
              ScheduleVertexMapActivity.class);
          scheduleMapIntent.putExtra("schedule", schedule);
          startActivity(scheduleMapIntent);
        }

      }
    });

    return rootView;
  }

}
