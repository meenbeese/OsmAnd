package net.osmand.plus.mapcontextmenu.editors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import net.osmand.data.FavouritePoint;
import net.osmand.data.LatLon;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.mapcontextmenu.MapContextMenu;
import net.osmand.plus.mapcontextmenu.other.SelectFavouriteBottomSheet;
import net.osmand.plus.myplaces.FavouritesHelper;
import net.osmand.plus.utils.UiUtilities;

import static net.osmand.plus.dialogs.FavoriteDialogs.KEY_FAVORITE;

public class SelectFavouriteToReplaceBottomSheet extends SelectFavouriteBottomSheet {

	@Override
	protected void onFavouriteSelected(@NonNull FavouritePoint favourite) {
		showConfirmationDialog(favourite);
	}

	private void showConfirmationDialog(@NonNull FavouritePoint fp) {
		boolean nightMode = isNightMode(mApp);
		Context themedContext = UiUtilities.getThemedContext(getContext(), nightMode);
		AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
		builder.setTitle(R.string.update_existing);
		builder.setMessage(getString(R.string.replace_favorite_confirmation, fp.getName()));
		builder.setNegativeButton(R.string.shared_string_no, null);
		builder.setPositiveButton(R.string.shared_string_yes, (dialog, which) -> {
			onApplyReplacement(fp);
		});
		builder.show();
	}

	private void onApplyReplacement(@NonNull FavouritePoint fp) {
		FavouritePoint point = (FavouritePoint) getArguments().getSerializable(KEY_FAVORITE);
		FavouritesHelper helper = mApp.getFavoritesHelper();
		if (point != null && helper.editFavourite(fp, point.getLatitude(), point.getLongitude())) {
			helper.deleteFavourite(point);
			Activity activity = getActivity();
			if (activity instanceof MapActivity) {
				MapActivity mapActivity = (MapActivity) activity;
				FragmentManager fm = mapActivity.getSupportFragmentManager();
				Fragment fragment = fm.findFragmentByTag(FavoritePointEditor.TAG);
				if (fragment instanceof FavoritePointEditorFragment) {
					((FavoritePointEditorFragment) fragment).exitEditing();
				}
				dismiss();
				MapContextMenu contextMenu = mapActivity.getContextMenu();
				contextMenu.show(new LatLon(point.getLatitude(), point.getLongitude()), fp.getPointDescription(activity), fp);
				mapActivity.refreshMap();
			}
		}
	}

	public static void showInstance(@NonNull Activity activity, @Nullable Bundle args) {
		SelectFavouriteToReplaceBottomSheet fragment = new SelectFavouriteToReplaceBottomSheet();
		fragment.setArguments(args);
		showFragment((FragmentActivity) activity, fragment);
	}

}
