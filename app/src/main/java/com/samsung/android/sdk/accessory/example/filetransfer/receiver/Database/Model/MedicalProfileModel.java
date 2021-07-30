
package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model;

public class MedicalProfileModel {
    public static final String TABLE_NAME = "medical_profile_table";

    public static final String COL_ID = "id";
    public static final String COL_REGI_ID = "registration_id";
    public static final String COL_HEIGHT = "height";
    public static final String COL_WEIGHT = "weight";
    public static final String COL_NAME = "name";
    public static final String COL_CONTACT = "contact";
    public static final String COL_DOB = "dob";
    public static final String COL_HAS_HEART_DISEASE = "has_heart_disease";
    public static final String COL_HAS_PARENT_HEART_DISEASE = "has_parent_heart_disease";
    public static final String COL_HAS_HYPER_TENSION = "has_hyper_tension";
    public static final String COL_HAS_COVID = "has_covid";
    public static final String COL_HAS_SMOKING = "has_smoking";
    public static final String COL_HAS_EATING_OUTSIDE = "has_eating_outside";
    public static final String COL_MIN_HR = "min_hr";
    public static final String COL_MAX_HR = "max_hr";



    String registration_id, height, weight, name, contact, dob ,  has_heart_disease , has_parent_heart_disease , has_hyper_tension , has_covid, has_smoking, has_eating_outside;
    double min_hr, max_hr;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_REGI_ID + " TEXT," +
                    COL_HEIGHT + " TEXT," +
                    COL_WEIGHT + " TEXT," +
                    COL_NAME + " TEXT," +
                    COL_CONTACT + " TEXT," +
                    COL_DOB + " TEXT," +
                    COL_HAS_HEART_DISEASE + " TEXT," +
                    COL_HAS_PARENT_HEART_DISEASE + " TEXT," +
                    COL_HAS_HYPER_TENSION + " TEXT," +
                    COL_HAS_COVID + " TEXT," +
                    COL_HAS_SMOKING + " TEXT," +
                    COL_MIN_HR + " REAL DEFAULT 60," +
                    COL_MAX_HR + " REAL DEFAULT 100," +
                    COL_HAS_EATING_OUTSIDE + " TEXT" +
                    ")";

    public MedicalProfileModel() {
    }


    public String getRegistration_id() {
        return registration_id;
    }

    public void setRegistration_id(String registration_id) {
        this.registration_id = registration_id;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getHas_heart_disease() {
        return has_heart_disease;
    }

    public void setHas_heart_disease(String has_heart_disease) {
        this.has_heart_disease = has_heart_disease;
    }

    public String getHas_parent_heart_disease() {
        return has_parent_heart_disease;
    }

    public void setHas_parent_heart_disease(String has_parent_heart_disease) {
        this.has_parent_heart_disease = has_parent_heart_disease;
    }

    public String getHas_hyper_tension() {
        return has_hyper_tension;
    }

    public void setHas_hyper_tension(String has_hyper_tension) {
        this.has_hyper_tension = has_hyper_tension;
    }

    public String getHas_covid() {
        return has_covid;
    }

    public void setHas_covid(String has_covid) {
        this.has_covid = has_covid;
    }

    public String getHas_smoking() {
        return has_smoking;
    }

    public void setHas_smoking(String has_smoking) {
        this.has_smoking = has_smoking;
    }

    public String getHas_eating_outside() {
        return has_eating_outside;
    }

    public void setHas_eating_outside(String has_eating_outside) {
        this.has_eating_outside = has_eating_outside;
    }

    public double getMin_hr() {
        return min_hr;
    }

    public void setMin_hr(double min_hr) {
        this.min_hr = min_hr;
    }

    public double getMax_hr() {
        return max_hr;
    }

    public void setMax_hr(double max_hr) {
        this.max_hr = max_hr;
    }
}
