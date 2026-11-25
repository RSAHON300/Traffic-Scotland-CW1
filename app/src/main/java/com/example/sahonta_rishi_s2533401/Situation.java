// Name                 Rishi Sahonta
// Student ID           S2533401
// Programme of Study   BSc (Hons) Software Development

package com.example.sahonta_rishi_s2533401;

import java.io.Serializable;

public class Situation implements Serializable {

    private String type;
    private String description;
    private String location;
    private String fromLatitude;
    private String fromLongitude;
    private String toLatitude;
    private String toLongitude;
    private String startTime;
    private String endTime;
    private String delaysType;
    private String roadManagementType;
    private String abnormalTrafficType;
    private String obstructionType;
    private String vehicleObstructionType;
    private String numberOfLanesRestricted;
    private String numberOfOperationalLanes;

    public Situation(String type, String description, String location, String fromLatitude, String fromLongitude, String toLatitude, String toLongitude, String startTime, String endTime, String delaysType, String roadManagementType, String abnormalTrafficType, String obstructionType, String vehicleObstructionType, String numberOfLanesRestricted, String numberOfOperationalLanes) {
        this.type = type;
        this.description = description;
        this.location = location;
        this.fromLatitude = fromLatitude;
        this.fromLongitude = fromLongitude;
        this.toLatitude = toLatitude;
        this.toLongitude = toLongitude;
        this.startTime = startTime;
        this.endTime = endTime;
        this.delaysType = delaysType;
        this.roadManagementType = roadManagementType;
        this.abnormalTrafficType = abnormalTrafficType;
        this.obstructionType = obstructionType;
        this.vehicleObstructionType = vehicleObstructionType;
        this.numberOfLanesRestricted = numberOfLanesRestricted;
        this.numberOfOperationalLanes = numberOfOperationalLanes;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getFromLatitude() {
        return fromLatitude;
    }

    public String getFromLongitude() {
        return fromLongitude;
    }

    public String getToLatitude() {
        return toLatitude;
    }

    public String getToLongitude() {
        return toLongitude;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDelaysType() {
        return delaysType;
    }

    public String getRoadManagementType() {
        return roadManagementType;
    }

    public String getAbnormalTrafficType() {
        return abnormalTrafficType;
    }

    public String getObstructionType() {
        return obstructionType;
    }

    public String getVehicleObstructionType() {
        return vehicleObstructionType;
    }

    public String getNumberOfLanesRestricted() {
        return numberOfLanesRestricted;
    }

    public String getNumberOfOperationalLanes() {
        return numberOfOperationalLanes;
    }
}
