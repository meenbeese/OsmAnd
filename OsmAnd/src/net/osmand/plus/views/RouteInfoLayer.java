package net.osmand.plus.views;


import net.osmand.osm.LatLon;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.ShowRouteInfoActivity;
import net.osmand.plus.routing.RouteDirectionInfo;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.routing.RoutingHelper.IRouteInformationListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;


public class RouteInfoLayer extends OsmandMapLayer implements IRouteInformationListener {

	private OsmandMapTileView view;
	private final RoutingHelper routingHelper;
	private Button next;
	private Button prev;
	private Button info;
	private int directionInfo = -1;

	private DisplayMetrics dm;
	private final ContextMenuLayer contextMenu;
	
	
	
	public RouteInfoLayer(RoutingHelper routingHelper, MapActivity activity, ContextMenuLayer contextMenu){
		final LinearLayout layout = (LinearLayout) activity.findViewById(R.id.RouteLayout);
		this.routingHelper = routingHelper;
		this.contextMenu = contextMenu;
		prev = (Button) layout.findViewById(R.id.PreviousButton);
		next = (Button) layout.findViewById(R.id.NextButton);
		info = (Button) layout.findViewById(R.id.InfoButton);
		routingHelper.addListener(this);
		attachListeners();
		updateVisibility();

		activity.accessibleContent.add(prev);
		activity.accessibleContent.add(next);
		activity.accessibleContent.add(info);
	}
	
	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;
		dm = new DisplayMetrics();
		WindowManager wmgr = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
		wmgr.getDefaultDisplay().getMetrics(dm);
	}
	
	private void attachListeners() {
		prev.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(routingHelper.getRouteDirections() != null && directionInfo > 0){
					directionInfo--;
					if(routingHelper.getRouteDirections().size() > directionInfo){
						RouteDirectionInfo info = routingHelper.getRouteDirections().get(directionInfo);
						net.osmand.Location l = routingHelper.getLocationFromRouteDirection(info);
						if(info.getDescriptionRoute() != null) {
							contextMenu.setLocation(new LatLon(l.getLatitude(), l.getLongitude()), info.getDescriptionRoute());
						}
						view.getAnimatedDraggingThread().startMoving(l.getLatitude(), l.getLongitude(), view.getFloatZoom(), true);
					}
				}
				view.refreshMap();
			}
			
		});
		next.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(routingHelper.getRouteDirections() != null && directionInfo < routingHelper.getRouteDirections().size() - 1){
					directionInfo++;
					RouteDirectionInfo info = routingHelper.getRouteDirections().get(directionInfo);
					net.osmand.Location l = routingHelper.getLocationFromRouteDirection(info);
					if(info.getDescriptionRoute() != null){
						contextMenu.setLocation(new LatLon(l.getLatitude(), l.getLongitude()), info.getDescriptionRoute());
					}
					view.getAnimatedDraggingThread().startMoving(l.getLatitude(), l.getLongitude(), view.getFloatZoom(), true);
				}
				view.refreshMap();
			}
			
		});
		info.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(view.getContext(), ShowRouteInfoActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				view.getContext().startActivity(intent);
			}
		});
	}

	public boolean isVisible(){
		return routingHelper.isRouteCalculated() && !routingHelper.isFollowingMode();
	}
	
	private void updateVisibility(){
		int vis = isVisible() ? View.VISIBLE : View.INVISIBLE; 
		prev.setVisibility(vis);
		next.setVisibility(vis);
		info.setVisibility(vis);
	}


	@Override
	public void destroyLayer() {
	}

	@Override
	public boolean drawInScreenPixels() {
		return true;
	}

	@Override
	public boolean onLongPressEvent(PointF point) {
		return false;
	}

	@Override
	public boolean onSingleTap(PointF point) {
		return false;
	}

	@Override
	public void newRouteIsCalculated(boolean newRoute) {
		directionInfo = -1;
		updateVisibility();
		view.refreshMap();
	}

	public int getDirectionInfo() {
		return directionInfo;
	}
	
	@Override
	public void routeWasCancelled() {
		directionInfo = -1;
		updateVisibility();
	}

	@Override
	public void onDraw(Canvas canvas, RectF latlonRect, RectF tilesRect, DrawSettings nightMode) {
		
	}
	
	
}
