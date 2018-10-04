package org.smartregister.kip.viewstates;

import android.os.Parcel;

import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

/**
 * Created by vijay on 5/14/15.
 */
public class KipJsonFormFragmentViewState extends JsonFormFragmentViewState implements android.os.Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public KipJsonFormFragmentViewState() {
    }

    private KipJsonFormFragmentViewState(Parcel in) {
        super(in);
    }

    public static final Creator<KipJsonFormFragmentViewState> CREATOR = new Creator<KipJsonFormFragmentViewState>() {
        public KipJsonFormFragmentViewState createFromParcel(
                Parcel source) {
            return new KipJsonFormFragmentViewState(source);
        }

        public KipJsonFormFragmentViewState[] newArray(
                int size) {
            return new KipJsonFormFragmentViewState[size];
        }
    };
}
