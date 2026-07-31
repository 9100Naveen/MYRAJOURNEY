package com.example.myrajourney.patient.medications.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MedicationReminder implements Parcelable {
    
    private int id;
    private int medicationId;
    private String medicationName;
    private String dosage;
    private String instructions;
    private long scheduledTime;
    private boolean isSnoozed;
    private int snoozeCount;
    private ReminderStatus status;
    
    public enum ReminderStatus {
        PENDING,
        DELIVERED,
        TAKEN,
        SNOOZED,
        SKIPPED,
        MISSED
    }
    
    public MedicationReminder() {
        this.status = ReminderStatus.PENDING;
        this.snoozeCount = 0;
        this.isSnoozed = false;
    }
    
    public MedicationReminder(int medicationId, String medicationName, String dosage, 
                            String instructions, long scheduledTime) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.instructions = instructions;
        this.scheduledTime = scheduledTime;
        this.status = ReminderStatus.PENDING;
        this.snoozeCount = 0;
        this.isSnoozed = false;
    }
    
    // Parcelable implementation
    protected MedicationReminder(Parcel in) {
        id = in.readInt();
        medicationId = in.readInt();
        medicationName = in.readString();
        dosage = in.readString();
        instructions = in.readString();
        scheduledTime = in.readLong();
        isSnoozed = in.readByte() != 0;
        snoozeCount = in.readInt();
        status = ReminderStatus.valueOf(in.readString());
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(medicationId);
        dest.writeString(medicationName);
        dest.writeString(dosage);
        dest.writeString(instructions);
        dest.writeLong(scheduledTime);
        dest.writeByte((byte) (isSnoozed ? 1 : 0));
        dest.writeInt(snoozeCount);
        dest.writeString(status.name());
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<MedicationReminder> CREATOR = new Creator<MedicationReminder>() {
        @Override
        public MedicationReminder createFromParcel(Parcel in) {
            return new MedicationReminder(in);
        }
        
        @Override
        public MedicationReminder[] newArray(int size) {
            return new MedicationReminder[size];
        }
    };
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getMedicationId() { return medicationId; }
    public void setMedicationId(int medicationId) { this.medicationId = medicationId; }
    
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    
    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public boolean isSnoozed() { return isSnoozed; }
    public void setSnoozed(boolean snoozed) { isSnoozed = snoozed; }
    
    public int getSnoozeCount() { return snoozeCount; }
    public void setSnoozeCount(int snoozeCount) { this.snoozeCount = snoozeCount; }
    
    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }
}
