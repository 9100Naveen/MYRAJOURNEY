package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import android.os.Parcel;
import android.os.Parcelable;

public class Rehab implements Parcelable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("reps")
    private String reps;

    @SerializedName("frequency_per_week")
    private String frequency;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("is_selected")
    private boolean isSelected;

    public Rehab() {
    }

    public Rehab(String id, String name, String description, String reps, String frequency, String videoUrl,
            String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.reps = reps;
        this.frequency = frequency;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.isSelected = false;
    }

    // Legacy constructor compatibility
    public Rehab(String name, String description, String reps, String frequency, String videoUrl, String thumbnailUrl) {
        this("ex_" + Math.abs(name.hashCode()), name, description, reps, frequency, videoUrl, thumbnailUrl);
    }

    public String getId() {
        return id != null ? id : "ex_001";
    } // Fallback

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    protected Rehab(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        reps = in.readString();
        frequency = in.readString();
        videoUrl = in.readString();
        thumbnailUrl = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(reps);
        dest.writeString(frequency);
        dest.writeString(videoUrl);
        dest.writeString(thumbnailUrl);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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
}