package net.scrumplex.sprummlbot.manager.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import net.scrumplex.sprummlbot.manager.R;
import net.scrumplex.sprummlbot.manager.api.APICallback;
import net.scrumplex.sprummlbot.manager.api.APIData;
import net.scrumplex.sprummlbot.manager.api.APIRequest;
import net.scrumplex.sprummlbot.manager.helpers.BansAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class BansList extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private final ArrayList<JSONObject> bans = new ArrayList<>();
    private BansAdapter adapter;
    private String apiKey;
    private String baseUrl;

    public BansList() {
        // Required empty public constructor
    }

    public void setData(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_clients_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) mainView.findViewById(R.id.swipe_refresh);
        listView = (ListView) mainView.findViewById(R.id.clients);
        listView.setEmptyView(mainView.findViewById(android.R.id.empty));
        adapter = new BansAdapter(getContext(), bans, this, apiKey, baseUrl);

        try {
            gatherInformation();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (listView != null && listView.getChildCount() > 0) {
                    boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = listView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                    @Override
                                                    public void onRefresh() {
                                                        try {
                                                            gatherInformation();
                                                        } catch (MalformedURLException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
        );
        return mainView;
    }

    public void gatherInformation() throws MalformedURLException {
        swipeRefreshLayout.setRefreshing(true);
        new APIRequest(new APICallback() {
            @Override
            public void handle(JSONObject response) throws JSONException {
                bans.clear();
                JSONArray cl = response.getJSONArray("bans");
                for (int i = 0; i < cl.length(); i++) {
                    bans.add(cl.getJSONObject(i));
                }
                if(bans.size() == 0) {
                    JSONObject dummy = new JSONObject();
                    dummy.put("lastnickname", "There are no bans at the moment");
                    dummy.put("ip", "");
                    dummy.put("invokername", "");
                    bans.add(dummy);
                }
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }).execute(new APIData(apiKey, new URL(baseUrl + "/bans")));
    }
}
