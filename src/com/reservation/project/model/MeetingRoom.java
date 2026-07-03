package com.reservation.project.model;

public class MeetingRoom {
    private long roomID;
    private String roomCode;
    private String roomName;
    private String Location;
    private int capacity;
    private int hasProjector;
    private int hasAudio;

    public long getRoomID() {
        return roomID;
    }

    public void setRoomID(long roomID) {
        this.roomID = roomID;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getHasProjector() {
        return hasProjector;
    }

    public void setHasProjector(int hasProjector) {
        this.hasProjector = hasProjector;
    }

    public int getHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(int hasAudio) {
        this.hasAudio = hasAudio;
    }
}
