package net.osmand.plus.views.mapwidgets;


import static net.osmand.plus.views.mapwidgets.WidgetParams.CURRENT_SPEED;
import static net.osmand.plus.views.mapwidgets.WidgetParams.CURRENT_TIME;
import static net.osmand.plus.views.mapwidgets.WidgetParams.MAX_SPEED;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.text.format.DateFormat;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.Location;
import net.osmand.binary.RouteDataObject;
import net.osmand.data.LatLon;
import net.osmand.plus.OsmAndLocationProvider;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.MapViewTrackingUtilities;
import net.osmand.plus.helpers.TargetPointsHelper;
import net.osmand.plus.helpers.TargetPointsHelper.TargetPoint;
import net.osmand.plus.routing.RouteCalculationResult.NextDirectionInfo;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.settings.backend.OsmandSettings;
import net.osmand.plus.utils.OsmAndFormatter;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.layers.base.OsmandMapLayer.DrawSettings;
import net.osmand.plus.views.mapwidgets.widgets.DistanceToPointWidget;
import net.osmand.plus.views.mapwidgets.widgets.NextTurnWidget;
import net.osmand.plus.views.mapwidgets.widgets.RightTextInfoWidget;
import net.osmand.plus.views.mapwidgets.widgets.TextInfoWidget;
import net.osmand.router.TurnType;

public class RouteInfoWidgetsFactory {

	private final OsmandApplication app;
	private final OsmandSettings settings;
	private final RoutingHelper routingHelper;
	private final OsmAndLocationProvider locationProvider;

	public RouteInfoWidgetsFactory(@NonNull OsmandApplication app) {
		this.app = app;
		this.settings = app.getSettings();
		this.routingHelper = app.getRoutingHelper();
		this.locationProvider = app.getLocationProvider();
	}

	public NextTurnWidget createNextInfoControl(@NonNull MapActivity mapActivity, boolean horizontalMini) {
		NextTurnWidget nextTurnInfo = new NextTurnWidget(mapActivity, horizontalMini) {
			final NextDirectionInfo calc1 = new NextDirectionInfo();

			@Override
			public void updateInfo(@Nullable DrawSettings drawSettings) {
				boolean followingMode = routingHelper.isFollowingMode() || locationProvider.getLocationSimulation().isRouteAnimating();
				TurnType turnType = null;
				boolean deviatedFromRoute = false;
				int turnImminent = 0;
				int nextTurnDistance = 0;
				if (routingHelper.isRouteCalculated() && followingMode) {
					deviatedFromRoute = routingHelper.isDeviatedFromRoute();
					
					if (deviatedFromRoute) {
						turnType = TurnType.valueOf(TurnType.OFFR, settings.DRIVING_REGION.get().leftHandDriving);
						setDeviatePath((int) routingHelper.getRouteDeviation());
					} else {
						NextDirectionInfo r = routingHelper.getNextRouteDirectionInfo(calc1, true);
						if (r != null && r.distanceTo > 0 && r.directionInfo != null) {
							turnType = r.directionInfo.getTurnType();
							nextTurnDistance = r.distanceTo;
							turnImminent = r.imminent;
						}
					}
				}
				setTurnType(turnType);
				setTurnImminent(turnImminent, deviatedFromRoute);
				setTurnDistance(nextTurnDistance);
			}
		};
		nextTurnInfo.setOnClickListener(new View.OnClickListener() {
//			int i = 0;
//			boolean leftSide = false;
			@Override
			public void onClick(View v) {
				// for test rendering purposes
//				final int l = TurnType.predefinedTypes.length;
//				final int exits = 5;
//				i++;
//				if (i % (l + exits) >= l ) {
//					nextTurnInfo.turnType = TurnType.valueOf("EXIT" + (i % (l + exits) - l + 1), leftSide);
//					float a = leftSide?  -180 + (i % (l + exits) - l + 1) * 50:  180 - (i % (l + exits) - l + 1) * 50;
//					nextTurnInfo.turnType.setTurnAngle(a < 0 ? a + 360 : a);
//					nextTurnInfo.exitOut = (i % (l + exits) - l + 1)+"";
//				} else {
//					nextTurnInfo.turnType = TurnType.valueOf(TurnType.predefinedTypes[i % (TurnType.predefinedTypes.length + exits)], leftSide);
//					nextTurnInfo.exitOut = "";
//				}
//				nextTurnInfo.turnImminent = (nextTurnInfo.turnImminent + 1) % 3;
//				nextTurnInfo.nextTurnDirection = 580;
//				TurnPathHelper.calcTurnPath(nextTurnInfo.pathForTurn, nextTurnInfo.turnType,nextTurnInfo.pathTransform);
				if (routingHelper.isRouteCalculated() && !routingHelper.isDeviatedFromRoute()) {
					routingHelper.getVoiceRouter().announceCurrentDirection(null);
				}
			}
		});
		return nextTurnInfo;
	}
	
	public NextTurnWidget createNextNextInfoControl(@NonNull MapActivity activity, boolean horizontalMini) {
		NextTurnWidget nextTurnInfo = new NextTurnWidget(activity, horizontalMini) {
			final NextDirectionInfo calc1 = new NextDirectionInfo();
			@Override
			public void updateInfo(@Nullable DrawSettings drawSettings) {
				boolean followingMode = routingHelper.isFollowingMode() || locationProvider.getLocationSimulation().isRouteAnimating();
				TurnType turnType = null;
				boolean deviatedFromRoute = false;
				int turnImminent = 0;
				int nextTurnDistance = 0;
				if (routingHelper.isRouteCalculated() && followingMode) {
					deviatedFromRoute = routingHelper.isDeviatedFromRoute();
					NextDirectionInfo r = routingHelper.getNextRouteDirectionInfo(calc1, true);
					if (!deviatedFromRoute) {
						if (r != null) {
							r = routingHelper.getNextRouteDirectionInfoAfter(r, calc1, true);
						}
					}
					if (r != null && r.distanceTo > 0 && r.directionInfo != null) {
						turnType = r.directionInfo.getTurnType();
						turnImminent = r.imminent;
						nextTurnDistance = r.distanceTo;
					}
				}
				setTurnType(turnType);
				setTurnImminent(turnImminent, deviatedFromRoute);
				setTurnDistance(nextTurnDistance);
			}
		};
		// Do not delete listener to have pressed state
		nextTurnInfo.setOnClickListener(new View.OnClickListener() {
//			int i = 0;
			@Override
			public void onClick(View v) {
				// uncomment to test turn info rendering
//				final int l = TurnType.predefinedTypes.length;
//				final int exits = 5;
//				i++;
//				if (i % (l + exits) >= l ) {
//					nextTurnInfo.turnType = TurnType.valueOf("EXIT" + (i % (l + exits) - l + 1), true);
//					nextTurnInfo.exitOut = (i % (l + exits) - l + 1)+"";
//					float a = 180 - (i % (l + exits) - l + 1) * 50;
//					nextTurnInfo.turnType.setTurnAngle(a < 0 ? a + 360 : a);
//				} else {
//					nextTurnInfo.turnType = TurnType.valueOf(TurnType.predefinedTypes[i % (TurnType.predefinedTypes.length + exits)], true);
//					nextTurnInfo.exitOut = "";
//				}
//				nextTurnInfo.turnImminent = (nextTurnInfo.turnImminent + 1) % 3;
//				nextTurnInfo.nextTurnDirection = 580;
//				TurnPathHelper.calcTurnPath(nextTurnInfo.pathForTurn, nexsweepAngletTurnInfo.turnType,nextTurnInfo.pathTransform);
//				showMiniMap = true;
			}
		});
		return nextTurnInfo;
	}

	public TextInfoWidget createPlainTimeControl(@NonNull MapActivity mapActivity) {
		TextInfoWidget plainTimeControl = new RightTextInfoWidget(mapActivity) {
			private long cachedLeftTime = 0;
			
			@Override
			public void updateInfo(@Nullable DrawSettings drawSettings) {
				long time = System.currentTimeMillis();
				if (isUpdateNeeded() || time - cachedLeftTime > 5000) {
					cachedLeftTime = time;
					if (DateFormat.is24HourFormat(app)) {
						setText(DateFormat.format("k:mm", time).toString(), null);
					} else {
						setText(DateFormat.format("h:mm", time).toString(),
								DateFormat.format("aa", time).toString());
					}
				}
			}
		};
		plainTimeControl.setText(null, null);
		plainTimeControl.setIcons(CURRENT_TIME);
		return plainTimeControl;
	}

	public TextInfoWidget createBatteryControl(@NonNull MapActivity mapActivity) {
		int battery = R.drawable.widget_battery_day;
		int batteryN = R.drawable.widget_battery_night;
		int batteryCharging = R.drawable.widget_battery_charging_day;
		int batteryChargingN = R.drawable.widget_battery_charging_night;
		TextInfoWidget batteryControl = new RightTextInfoWidget(mapActivity) {
			private long cachedLeftTime = 0;

			@Override
			public void updateInfo(@Nullable DrawSettings drawSettings) {
				long time = System.currentTimeMillis();
				if (isUpdateNeeded() || time - cachedLeftTime > 1000) {
					cachedLeftTime = time;
					Intent batteryIntent = app.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
					int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
					int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

					if (level == -1 || scale == -1 || status == -1) {
						setText("?", null);
						setIcons(battery, batteryN);
					} else {
						boolean charging = ((status == BatteryManager.BATTERY_STATUS_CHARGING) ||
								(status == BatteryManager.BATTERY_STATUS_FULL));
						setText(String.format("%d%%", (level * 100) / scale), null);
						setIcons(charging ? batteryCharging : battery, charging ? batteryChargingN : batteryN);
					}
				}
			}
		};
		batteryControl.setText(null, null);
		batteryControl.setIcons(battery, batteryN);
		return batteryControl;
	}

	public TextInfoWidget createMaxSpeedControl(@NonNull MapActivity mapActivity) {
		MapViewTrackingUtilities trackingUtilities = mapActivity.getMapViewTrackingUtilities();
		TextInfoWidget speedControl = new RightTextInfoWidget(mapActivity) {
			private float cachedSpeed = 0;

			@Override
			public void updateInfo(@Nullable DrawSettings drawSettings) {
				float mx = 0;
				if ((!routingHelper.isFollowingMode()
						|| routingHelper.isDeviatedFromRoute()
						|| (routingHelper.getCurrentGPXRoute() != null && !routingHelper.isCurrentGPXRouteV2()))
						&& trackingUtilities.isMapLinkedToLocation()) {
					RouteDataObject ro = locationProvider.getLastKnownRouteSegment();
					if (ro != null) {
						mx = ro.getMaximumSpeed(ro.bearingVsRouteDirection(locationProvider.getLastKnownLocation()));
					}
				} else {
					mx = routingHelper.getCurrentMaxSpeed();
				}
				if (isUpdateNeeded() || cachedSpeed != mx) {
					cachedSpeed = mx;
					if (cachedSpeed == 0) {
						setText(null, null);
					} else if (cachedSpeed == RouteDataObject.NONE_MAX_SPEED) {
						setText(getString(R.string.max_speed_none), "");
					} else {
						String ds = OsmAndFormatter.getFormattedSpeed(cachedSpeed, app);
						int ls = ds.lastIndexOf(' ');
						if (ls == -1) {
							setText(ds, null);
						} else {
							setText(ds.substring(0, ls), ds.substring(ls + 1));
						}
					}
				}
			}

			@Override
			public boolean isMetricSystemDepended() {
				return true;
			}
		};
		speedControl.setIcons(MAX_SPEED);
		speedControl.setText(null, null);
		return speedControl;
	}

	public TextInfoWidget createSpeedControl(@NonNull MapActivity mapActivity) {
		TextInfoWidget speedControl = new RightTextInfoWidget(mapActivity) {
			private float cachedSpeed = 0;

			@Override
			public void updateInfo(@Nullable DrawSettings drawSettings) {
				Location loc = locationProvider.getLastKnownLocation();
				if (loc != null && loc.hasSpeed()) {
					// .1 mps == 0.36 kph
					float minDelta = .1f;
					// Update more often at walk/run speeds, since we give higher resolution
					// and use .02 instead of .03 to account for rounding effects.
					if (cachedSpeed < 6) {
						minDelta = .015f;
					}
					if (isUpdateNeeded() || Math.abs(loc.getSpeed() - cachedSpeed) > minDelta) {
						cachedSpeed = loc.getSpeed();
						String ds = OsmAndFormatter.getFormattedSpeed(cachedSpeed, app);
						int ls = ds.lastIndexOf(' ');
						if (ls == -1) {
							setText(ds, null);
						} else {
							setText(ds.substring(0, ls), ds.substring(ls + 1));
						}
					}
				} else if (cachedSpeed != 0) {
					cachedSpeed = 0;
					setText(null, null);
				}
			}

			@Override
			public boolean isMetricSystemDepended() {
				return true;
			}
		};
		speedControl.setIcons(CURRENT_SPEED);
		speedControl.setText(null, null);
		return speedControl;
	}

	public TextInfoWidget createDistanceControl(@NonNull MapActivity mapActivity) {
		DistanceToPointWidget distanceControl = new DistanceToPointWidget(mapActivity, R.drawable.widget_target_day,
				R.drawable.widget_target_night) {
			@Override
			public LatLon getPointToNavigate() {
				TargetPoint p = mapActivity.getPointToNavigate();
				return p == null ? null : p.point;
			}

			@Override
			public int getDistance() {
				return routingHelper.isRouteCalculated()
						? routingHelper.getLeftDistance()
						: super.getDistance();
			}
		};
		return distanceControl;
	}
	
	public TextInfoWidget createIntermediateDistanceControl(@NonNull MapActivity mapActivity) {
		TargetPointsHelper targets = app.getTargetPointsHelper();
		DistanceToPointWidget distanceControl = new DistanceToPointWidget(mapActivity, R.drawable.widget_intermediate_day,
				R.drawable.widget_intermediate_night) {

			@Override
			protected void click(OsmandMapTileView view) {
				if (targets.getIntermediatePoints().size() > 1) {
					mapActivity.getMapActions().openIntermediatePointsDialog();
				} else {
					super.click(view);
				}
			}

			@Override
			public LatLon getPointToNavigate() {
				TargetPoint p = targets.getFirstIntermediatePoint();
				return p == null ? null : p.point;
			}

			@Override
			public int getDistance() {
				return getPointToNavigate() != null && routingHelper.isRouteCalculated()
						? routingHelper.getLeftDistanceNextIntermediate()
						: super.getDistance();
			}
		};
		return distanceControl;
	}

	public static boolean distChanged(int oldDist, int dist) {
		return oldDist == 0 || Math.abs(oldDist - dist) >= 10;
	}
}