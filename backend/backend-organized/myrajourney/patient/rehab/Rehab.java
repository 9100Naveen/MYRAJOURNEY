package com.example.myrajourney.patient.rehab;

import android.os.Parcel;
import android.os.Parcelable;

public class Rehab implements Parcelable {
    private String name;
    private String description;
    private String reps;
    private String frequency;
    private String videoUrl;
    private String thumbnailUrl;
    private boolean selected;

    public Rehab(String name, String description, String reps, String frequency,
                 String videoUrl, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.reps = reps;
        this.frequency = frequency;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.selected = false;
    }

    protected Rehab(Parcel in) {
        name = in.readString();
        description = in.readString();
        reps = in.readString();
        frequency = in.readString();
        videoUrl = in.readString();
        thumbnailUrl = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<Rehab> CREATOR = new Creator<Rehab>() {
        @Override
        public Rehab createFromParcel(Parcel in) {
            return new Rehab(in);
        }

        @Override
        public Rehab[] newArray(int size) {
            return new Rehab[size];
        }
    };

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getReps() { return reps; }
    public String getFrequency() { return frequency; }
    public String getVideoUrl() { return videoUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(reps);
        parcel.writeString(frequency);
        parcel.writeString(videoUrl);
        parcel.writeString(thumbnailUrl);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }
}






