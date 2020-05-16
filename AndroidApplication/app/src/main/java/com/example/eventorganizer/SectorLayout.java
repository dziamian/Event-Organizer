package com.example.eventorganizer;

public class SectorLayout {
    public String sectorName;
    public String sectorAddress;
    public int activeRooms;

    public SectorLayout(String sectorName, String sectorAddress, int activeRooms) {
        this.sectorName = sectorName;
        this.sectorAddress = sectorAddress;
        this.activeRooms = activeRooms;
    }
}
