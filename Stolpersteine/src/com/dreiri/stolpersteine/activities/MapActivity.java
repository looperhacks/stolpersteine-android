package com.dreiri.stolpersteine.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.dreiri.stolpersteine.R;
import com.dreiri.stolpersteine.api.NetworkService;
import com.dreiri.stolpersteine.api.NetworkService.Callback;
import com.dreiri.stolpersteine.api.SearchData;
import com.dreiri.stolpersteine.api.SynchronizationController;
import com.dreiri.stolpersteine.api.model.Stolperstein;
import com.dreiri.stolpersteine.clustering.MapClusterController;
import com.dreiri.stolpersteine.utils.LocationFinder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements OnInfoWindowClickListener {
	private static LatLng BERLIN_LAT_LNG = new LatLng(52.5191710, 13.40609120);
	private static int BERLIN_ZOOM = 12;

	private NetworkService networkService = new NetworkService();
	private SynchronizationController synchronizationController = new SynchronizationController(networkService);
	private MapClusterController<Stolperstein> mapClusterController;
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragmentMap)).getMap();
		CameraUpdate region = CameraUpdateFactory.newLatLngZoom(BERLIN_LAT_LNG, BERLIN_ZOOM);
		map.moveCamera(region);
		map.setOnInfoWindowClickListener(this);
		mapClusterController = new MapClusterController<Stolperstein>(map);

		synchronizationController.retrieveStolpersteine(new Callback() {

			@Override
			public void onStolpersteineRetrieved(List<Stolperstein> stolpersteine) {
				ArrayList<MarkerOptions> optionsList = new ArrayList<MarkerOptions>(stolpersteine.size());
				for (Stolperstein stolperstein : stolpersteine) {
					MarkerOptions markerOptions = new MarkerOptions().position(stolperstein.getLocation().getCoordinates())
					        .title(stolperstein.getPerson().getNameAsString())
					        .snippet(stolperstein.getLocation().getAddressAsString())
					        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stolpersteine_tile));
					optionsList.add(markerOptions);
				}
				mapClusterController.addMarkers(optionsList, stolpersteine);
			}
		});

		EditText editTextQuery = (EditText) findViewById(R.id.editTextQuery);
		editTextQuery.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				SearchData searchData = new SearchData();
				searchData.setKeyword(s.toString());
				networkService.retrieveStolpersteine(searchData, 0, 10, new Callback() {

					@Override
					public void onStolpersteineRetrieved(List<Stolperstein> stolpersteine) {
						for (Stolperstein stolperstein : stolpersteine) {
							String firstName = stolperstein.getPerson().getFirstName();
							Toast.makeText(MapActivity.this, firstName, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_positioning:
			LocationFinder locationFinder = new LocationFinder(MapActivity.this);
			LatLng currentLocation = new LatLng(locationFinder.getLat(), locationFinder.getLng());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
			map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		ArrayList<Stolperstein> stolpersteine = mapClusterController.getItems(marker);
		if (!stolpersteine.isEmpty()) {
			Intent intent = new Intent(MapActivity.this, InfoActivity.class);
			intent.putParcelableArrayListExtra("stolpersteine", stolpersteine);
			startActivity(intent);
		}
	}
}