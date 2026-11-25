// Name                 Rishi Sahonta
// Student ID           S2533401
// Programme of Study   BSc (Hons) Software Development

package com.example.sahonta_rishi_s2533401;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private Button startButton;
    private TextView updateStatus;
    private ListView unplannedEventsList;
    private ListView currentRoadworksList;
    private EditText roadSearchInput;
    private Button searchButton;
    private Button clearButton;
    private Button nearbySearchButton;
    private Spinner distanceSpinner;
    private Button mapButton;

    private String currentSearch = "";
    private boolean nearbySearchActive = false;
    private double searchDistance = 0;
    private long nextUpdateTime;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int updateTime = 60 * 60 * 1000;
    private static final double currentLatitude = 55.861670;
    private static final double currentLongitude = -4.258335;
    private SharedPreferences preferences;

    private ArrayList<Situation> unplannedEvents;
    private ArrayList<Situation> currentRoadworks;
    private ArrayList<Situation> filteredUnplannedEvents = new ArrayList<>();
    private ArrayList<Situation> filteredCurrentRoadworks = new ArrayList<>();

    //DOWNLOADING FROM TRAFFIC SCOTLAND
    private String clientId;
    private String clientKey;
    private final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    private final String currentRoadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";


    private Runnable hourlyUpdate = new Runnable() {
        @Override
        public void run() {
            startProgress();
            nextUpdateTime = SystemClock.elapsedRealtime() + updateTime;
            handler.postDelayed(this, updateTime);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentSearch = savedInstanceState.getString("currentSearch", "");
            nearbySearchActive = savedInstanceState.getBoolean("nearbySearchActive", false);
            searchDistance = savedInstanceState.getDouble("searchDistance", 0);
        }
        preferences = getSharedPreferences("TrafficScotlandData", MODE_PRIVATE);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        updateStatus = findViewById(R.id.updateStatus);
        unplannedEventsList = findViewById(R.id.unplannedEventsList);
        currentRoadworksList = findViewById(R.id.currentRoadworksList);
        roadSearchInput = findViewById(R.id.roadSearchInput);
        searchButton = findViewById(R.id.searchButton);
        clearButton = findViewById(R.id.clearButton);
        nearbySearchButton = findViewById(R.id.nearbySearchButton);
        distanceSpinner = findViewById(R.id.distanceSpinner);
        mapButton = findViewById(R.id.mapButton);

        String[] distances = {"5 km", "10 km", "20 km", "50 km"};

        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distances);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        if (savedInstanceState != null) {
            distanceSpinner.setSelection(savedInstanceState.getInt("distancePosition", 0));
        }

        searchButton.setOnClickListener(v -> {
            currentSearch = roadSearchInput.getText().toString().trim();
            nearbySearchActive = false;
            displayTrafficData();
        });

        clearButton.setOnClickListener(v -> {
            roadSearchInput.setText("");
            currentSearch = "";
            nearbySearchActive = false;
            displayTrafficData();
        });

        nearbySearchButton.setOnClickListener(v -> {
            searchDistance = Double.parseDouble(distanceSpinner.getSelectedItem().toString().replace(" km", ""));
            searchDistance = searchDistance * 1000;
            nearbySearchActive = true;
            displayNearbyTrafficData(searchDistance);
        });

        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Map.class);
            intent.putExtra("unplannedEvents", filteredUnplannedEvents);
            intent.putExtra("currentRoadworks", filteredCurrentRoadworks);
            startActivity(intent);
        });

        unplannedEventsList.setOnItemClickListener((listView, listItem, position, id) -> {
            Situation situation = filteredUnplannedEvents.get(position);
            displaySituationDetails(situation);
        });

        currentRoadworksList.setOnItemClickListener((listView, listItem, position, id) -> {
            Situation situation = filteredCurrentRoadworks.get(position);
            displaySituationDetails(situation);
        });

        unplannedEvents = new ArrayList<>();
        currentRoadworks = new ArrayList<>();

        String savedRoadworks = preferences.getString("CurrentRoadworks", "");
        String savedEvents = preferences.getString("UnplannedEvents", "");

        if (!savedRoadworks.isEmpty()) {
            ArrayList<Situation> saved = parseSituations(savedRoadworks);

            if (saved != null) {
                currentRoadworks.addAll(saved);
            }
        }

        if (!savedEvents.isEmpty()) {
            ArrayList<Situation> saved = parseSituations(savedEvents);

            if (saved != null) {
                unplannedEvents.addAll(saved);
            }
        }

        if (nearbySearchActive) {
            displayNearbyTrafficData(searchDistance);

        } else {
            roadSearchInput.setText(currentSearch);
            displayTrafficData();
        }

        clientId = "";
        clientKey = "";

        Bundle metaData = null;

        try {
            metaData = getPackageManager().
                    getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;

            clientId = metaData.getString("com.example.sahonta_rishi_s2533401.CLIENT_ID");
            clientKey = metaData.getString("com.example.sahonta_rishi_s2533401.CLIENT_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (savedInstanceState == null) {
            startProgress();
            nextUpdateTime = SystemClock.elapsedRealtime() + updateTime;

        } else {
            nextUpdateTime = savedInstanceState.getLong("nextUpdateTime", SystemClock.elapsedRealtime() + updateTime);
        }

        long remainingTime = Math.max(0, nextUpdateTime - SystemClock.elapsedRealtime());
        handler.postDelayed(hourlyUpdate, remainingTime);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("currentSearch", currentSearch);
        savedInstanceState.putBoolean("nearbySearchActive", nearbySearchActive);
        savedInstanceState.putDouble("searchDistance", searchDistance);
        savedInstanceState.putInt("distancePosition", distanceSpinner.getSelectedItemPosition());
        savedInstanceState.putLong("nextUpdateTime", nextUpdateTime);
    }

    private Double calculateDistance(Situation situation) {

        Double fromDistance = null;
        Double toDistance = null;

        if (!situation.getFromLatitude().isEmpty() && !situation.getFromLongitude().isEmpty()) {
            float[] results = new float[1];
            Location.distanceBetween(currentLatitude, currentLongitude, Double.parseDouble(situation.getFromLatitude()), Double.parseDouble(situation.getFromLongitude()), results);
            fromDistance = (double) results[0];
        }

        if (!situation.getToLatitude().isEmpty() && !situation.getToLongitude().isEmpty()) {
            float[] results = new float[1];
            Location.distanceBetween(currentLatitude, currentLongitude, Double.parseDouble(situation.getToLatitude()), Double.parseDouble(situation.getToLongitude()), results);
            toDistance = (double) results[0];
        }

        if (fromDistance == null && toDistance == null) {
            return null;

        } else if (fromDistance == null) {
            return toDistance;

        } else if (toDistance == null) {
            return fromDistance;
        }

        if (fromDistance < toDistance) {
            return fromDistance;
        } else {
            return toDistance;
        }
    }

    private void displayNearbyTrafficData(double searchRadius) {
        filteredUnplannedEvents.clear();
        filteredCurrentRoadworks.clear();

        for (Situation situation : unplannedEvents) {
            Double distance = calculateDistance(situation);

            if (distance != null && distance <= searchRadius) {
                filteredUnplannedEvents.add(situation);
            }
        }

        SituationAdapter unplannedEventAdapter =  new SituationAdapter(this, filteredUnplannedEvents);
        unplannedEventsList.setAdapter(unplannedEventAdapter);

        for (Situation situation : currentRoadworks) {
            Double distance = calculateDistance(situation);

            if (distance != null && distance <= searchRadius) {
                filteredCurrentRoadworks.add(situation);
            }
        }

        SituationAdapter roadworkAdapter = new SituationAdapter(this, filteredCurrentRoadworks);
        currentRoadworksList.setAdapter(roadworkAdapter);
    }

    private ArrayList<Situation> parseSituations(String xml) {

        ArrayList<Situation> situations = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);

            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(xml));

            int eventType = xpp.getEventType();

            String type = "";
            String description = "";
            String location = "";
            String fromLocation = "";
            String fromLatitude = "";
            String fromLongitude = "";
            String toLatitude = "";
            String toLongitude = "";
            String startTime = "";
            String endTime = "";
            String delaysType = "";
            String roadManagementType = "";
            String abnormalTrafficType = "";
            String obstructionType = "";
            String vehicleObstructionType = "";
            String numberOfLanesRestricted = "";
            String numberOfOperationalLanes = "";
            String nameValue = "";
            String nameDescriptorType = "";
            String ilcValue = "";
            String ilcDescriptorType = "";

            boolean insideSituation = false;
            boolean insideGroupOfLocations = false;
            boolean insideGeneralPublicComment = false;
            boolean insideFrom = false;
            boolean insideTo = false;
            boolean insideName = false;
            boolean insideIlc = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {

                    String tagName = xpp.getName();

                    if (tagName.equals("situation")){
                        insideSituation = true;
                        type = "";
                        description = "";
                        location = "";
                        fromLocation = "";
                        fromLatitude = "";
                        fromLongitude = "";
                        toLatitude = "";
                        toLongitude = "";
                        startTime = "";
                        endTime = "";
                        delaysType = "";
                        roadManagementType = "";
                        abnormalTrafficType = "";
                        obstructionType = "";
                        vehicleObstructionType = "";
                        numberOfLanesRestricted = "";
                        numberOfOperationalLanes = "";

                    } else if (insideSituation && tagName.equals("situationRecord")) {

                        if (xpp.getAttributeCount() > 0){
                            type = xpp.getAttributeValue(0);
                        }
                    } else if (insideSituation && tagName.equals("overallStartTime")) {
                        startTime = xpp.nextText();

                    } else if (insideSituation && tagName.equals("overallEndTime")) {
                        endTime = xpp.nextText();

                    } else if (insideSituation && tagName.equals("delaysType")) {
                        delaysType = xpp.nextText();

                    } else if (insideSituation && tagName.equals("roadOrCarriagewayOrLaneManagementType")) {
                        roadManagementType = xpp.nextText();

                    } else if (insideSituation && tagName.equals("abnormalTrafficType")) {
                        abnormalTrafficType = xpp.nextText();

                    } else if (insideSituation && tagName.equals("obstructionType")) {
                        obstructionType = xpp.nextText();

                    } else if (insideSituation && tagName.equals("vehicleObstructionType")) {
                        vehicleObstructionType = xpp.nextText();

                    } else if (insideSituation && tagName.equals("numberOfLanesRestricted")) {
                        numberOfLanesRestricted = xpp.nextText();

                    } else if (insideSituation && tagName.equals("numberOfOperationalLanes")) {
                        numberOfOperationalLanes = xpp.nextText();

                    } else if (insideSituation && tagName.equals("generalPublicComment")) {
                        insideGeneralPublicComment = true;

                    } else if (insideGeneralPublicComment && tagName.equals("value")) {

                        if (!description.isEmpty()) {
                            description = description + " ";
                        }

                        description = description + xpp.nextText();

                    } else if (insideSituation && tagName.equals("groupOfLocations")) {
                        insideGroupOfLocations = true;

                    } else if (insideGroupOfLocations && tagName.equals("name")) {
                        insideName = true;
                        nameValue = "";
                        nameDescriptorType = "";

                    } else if (insideName && tagName.equals("value")) {
                        nameValue = xpp.nextText();

                    } else if (insideName && tagName.equals("tpegOtherPointDescriptorType")) {
                        nameDescriptorType = xpp.nextText();

                    } else if (insideGroupOfLocations && tagName.equals("ilc")) {
                        insideIlc = true;
                        ilcValue = "";
                        ilcDescriptorType = "";

                    } else if (insideIlc && tagName.equals("value")) {
                        ilcValue = xpp.nextText();

                    } else if (insideIlc && tagName.equals("tpegIlcPointDescriptorType")) {
                        ilcDescriptorType = xpp.nextText();

                    } else if (insideGroupOfLocations && tagName.equals("from")) {
                        insideFrom = true;

                    } else if (insideGroupOfLocations && tagName.equals("to")) {
                        insideTo = true;

                    } else if (insideFrom && tagName.equals("latitude")) {
                        fromLatitude = xpp.nextText();

                    } else if (insideFrom && tagName.equals("longitude")) {
                        fromLongitude = xpp.nextText();

                    } else if (insideTo && tagName.equals("latitude")) {
                        toLatitude = xpp.nextText();

                    } else if (insideTo && tagName.equals("longitude")) {
                        toLongitude = xpp.nextText();
                    }

                } else if (eventType == XmlPullParser.END_TAG) {

                    if (xpp.getName().equals("name")) {

                        if (insideFrom && nameDescriptorType.equals("linkName")) {
                            fromLocation = nameValue;
                        }

                        insideName = false;
                    }

                    if (xpp.getName().equals("ilc")) {

                        if (insideFrom && ilcDescriptorType.equals("tpegIlcName1")) {
                            fromLocation = ilcValue;
                        }

                        insideIlc = false;
                    }

                    if (xpp.getName().equals("from")) {
                        insideFrom = false;
                    }

                    if (xpp.getName().equals("to")) {
                        insideTo = false;
                    }

                    if (xpp.getName().equals("generalPublicComment")) {
                        insideGeneralPublicComment = false;
                    }

                    if (xpp.getName().equals("groupOfLocations")) {
                        location = fromLocation;
                        insideGroupOfLocations = false;
                    }

                    if (xpp.getName().equals("situation")) {
                        situations.add(new Situation(type, description, location, fromLatitude, fromLongitude, toLatitude, toLongitude, startTime, endTime, delaysType, roadManagementType, abnormalTrafficType, obstructionType, vehicleObstructionType, numberOfLanesRestricted, numberOfOperationalLanes));
                        insideSituation = false;
                    }
                }

                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e("Parsing", "Error parsing XML", e);
            return null;
        }

        return situations;
    }

    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        updateStatus.setTextColor(getResources().getColor(R.color.Blue));
        updateStatus.setText("Updating data");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Task roadworksTask = new Task(currentRoadworksURL, true);
                roadworksTask.run();

                Task unplannedEventsTask = new Task(unplannedEventsURL, false);
                unplannedEventsTask.run();

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (nearbySearchActive) {
                            displayNearbyTrafficData(searchDistance);

                        } else {
                            displayTrafficData();
                        }

                        if (roadworksTask.successful && unplannedEventsTask.successful) {
                            updateStatus.setTextColor(getResources().getColor(R.color.Green));
                            updateStatus.setText("Roadworks and unplanned events updated successfully.");

                        } else if (!roadworksTask.successful && !unplannedEventsTask.successful) {
                            updateStatus.setTextColor(getResources().getColor(R.color.Red));
                            updateStatus.setText("Unable to update data. Displaying saved results.");

                        } else if (roadworksTask.successful && !unplannedEventsTask.successful){
                            updateStatus.setTextColor(getResources().getColor(R.color.Orange));
                            updateStatus.setText("Roadworks updated successfully. Unable to update unplanned events.");

                        } else if (!roadworksTask.successful && unplannedEventsTask.successful){
                            updateStatus.setTextColor(getResources().getColor(R.color.Orange));
                            updateStatus.setText("Unplanned events updated successfully. Unable to update roadworks.");
                        }
                    }
                });
            }
        }).start();

        // Run network access on a separate thread;
    }

    private void displayTrafficData() {
        filteredUnplannedEvents.clear();
        filteredCurrentRoadworks.clear();

        for (Situation situation : unplannedEvents) {
            if (situation.getLocation() != null && situation.getLocation().toLowerCase().contains(currentSearch.toLowerCase())) {
                filteredUnplannedEvents.add(situation);
            }
        }

        SituationAdapter unplannedEventAdapter = new SituationAdapter(this, filteredUnplannedEvents);
        unplannedEventsList.setAdapter(unplannedEventAdapter);

        for (Situation situation : currentRoadworks) {
            if (situation.getLocation() != null && situation.getLocation().toLowerCase().contains(currentSearch.toLowerCase())) {
                filteredCurrentRoadworks.add(situation);
            }
        }

        SituationAdapter roadworkAdapter = new SituationAdapter(this, filteredCurrentRoadworks);
        currentRoadworksList.setAdapter(roadworkAdapter);
    }

    private void displaySituationDetails(Situation situation) {

        String details = "Type: " + situation.getType() + "\n\n" +
                "Description: " + situation.getDescription() + "\n\n" +
                "Road: " + situation.getLocation() + "\n\n" +
                "Starting Coordinates: " + situation.getFromLatitude() + ", " + situation.getFromLongitude() + "\n\n" +
                "Ending Coordinates: " + situation.getToLatitude() + ", " + situation.getToLongitude() + "\n\n" +
                "Start Time: " + formatDateTime(situation.getStartTime()) + "\n\n";

        if (!situation.getEndTime().isEmpty()) {
            details = details + "Expected End Time: " + formatDateTime(situation.getEndTime()) + "\n\n";
        }

        if (!situation.getRoadManagementType().isEmpty()) {
            details = details + "Road Management: " + situation.getRoadManagementType() + "\n\n";
        }

        if (!situation.getAbnormalTrafficType().isEmpty()) {
            details = details + "Abnormal Traffic: " + situation.getAbnormalTrafficType() + "\n\n";
        }

        if (!situation.getObstructionType().isEmpty()) {
            details = details + "Obstruction: " + situation.getObstructionType() + "\n\n";
        }

        if (!situation.getVehicleObstructionType().isEmpty()) {
            details = details + "Vehicle Obstruction: " + situation.getVehicleObstructionType() + "\n\n";
        }

        if (!situation.getNumberOfLanesRestricted().isEmpty()) {
            details = details + "Restricted Lanes: " + situation.getNumberOfLanesRestricted() + "\n\n";
        }

        if (!situation.getNumberOfOperationalLanes().isEmpty()) {
            details = details + "Operational Lanes: " + situation.getNumberOfOperationalLanes() + "\n\n";
        }

        if (situation.getType().equals("MaintenanceWorks")) {
            String delayText;

            if (situation.getDelaysType().equals("delays")) {
                delayText = "Short delays";

            } else if (situation.getDelaysType().equals("longDelays")) {
                delayText = "Long delays";

            } else {
                delayText = "Unspecified delays";
            }

            details = details + "Delay Level: " + delayText;

            SpannableString styledDetails = new SpannableString(details);
            int index = details.indexOf(delayText);

            if (index >= 0) {
                int colour;

                if (delayText.equals("Short delays")) {
                    colour = ContextCompat.getColor(this, R.color.Orange);

                } else if (delayText.equals("Long delays")) {
                    colour = ContextCompat.getColor(this, R.color.Red);

                } else {
                    colour = ContextCompat.getColor(this, R.color.Gray);
                }

                styledDetails.setSpan(new ForegroundColorSpan(colour), index, index + delayText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            new AlertDialog.Builder(this).setTitle("Situation Details").setMessage(styledDetails).setPositiveButton("Close", null).show();

        } else {
            new AlertDialog.Builder(this).setTitle("Situation Details").setMessage(details).setPositiveButton("Close", null).show();
        }
    }

    private String formatDateTime(String dateTime) {

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.UK);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.UK);
            return outputFormat.format(inputFormat.parse(dateTime));

        } catch (ParseException e) {
            return "Unavailable date";
        }
    }

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        private String result="";
        private String myurl;
        BufferedReader in = null;
        String inputLine = "";
        private boolean isRoadworks;
        private boolean successful = false;
        public Task(String aurl, boolean roadworks) {
            myurl = aurl;
            isRoadworks = roadworks;
        }
        @Override
        public void run() {
            Log.d("MyTask", "in run");

            try {
                URL url = new URL(myurl);
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();

                // Original authentication method
                urlConnection.setRequestProperty(
                        "Authorization",
                        Base64.getEncoder().encodeToString(
                                (clientId + ":" + clientKey).getBytes()
                        )
                );

                try {
                    in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream())
                    );

                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    result = response.toString();
                    in.close();

                } catch (IOException e) {
                    Log.e("urlConnection", "Download failed", e);
                    return;

                } finally {
                    urlConnection.disconnect();
                }

            } catch (IOException e) {
                Log.e("URL", "ioexception", e);
                return;
            }

            // Clean up any leading garbage characters
            int i = result.indexOf("<");

            if (i >= 0) {
                result = result.substring(i);
            } else {
                Log.e("XML", "Invalid XML response");
                return;
            }

            ArrayList<Situation> parsedSituations = parseSituations(result);

            if (parsedSituations == null || parsedSituations.isEmpty()) {
                Log.e("Test XML Data", "Xml parsing failed");
                return;

            } else {
                Log.d("Test XML Data", "Successfully parsed Current Roadworks and Unplanned Events XML");
            }

            if (isRoadworks) {
                currentRoadworks.clear();
                currentRoadworks.addAll(parsedSituations);
                preferences.edit().putString("CurrentRoadworks", result).apply();

            } else {
                unplannedEvents.clear();
                unplannedEvents.addAll(parsedSituations);
                preferences.edit().putString("UnplannedEvents", result).apply();
            }
            successful = true;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(hourlyUpdate);
    }
}
